package com.smthe.money.bot;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import com.smthe.money.config.BotConfig;
import com.smthe.money.series.BithumSeries;

public class FirstBot extends Bot {

    BithumSeries bithumSeries = new BithumSeries();
    BaseTradingRecord tradingRecord = new BaseTradingRecord();
    ClosePriceIndicator closePrice = null;
    RSIIndicator rsi = null;
    MACDIndicator macd = null;
    EMAIndicator signalLine = null;
    SMAIndicator sma = null;
    StandardDeviationIndicator stdDev = null;
    BollingerBandsMiddleIndicator bbMiddle = null;
    BollingerBandsUpperIndicator bbUpper = null;
    BollingerBandsLowerIndicator bbLower = null;
    VolumeIndicator volume = null;
    SMAIndicator avgVolume = null;

    int rsiBarCount = 7;
    double rsiBottomValue = 30;
    double rsiTopValue = 70;
    int macdShortBarCount = 6;
    int macdLongBarCount = 13;
    int signalBarCount = 5;
    int smaBarCount = 20;
    int stdDevBarCount = 20;
    int volumeBarCount = 20;
    int avgVolumeBarCount = 20;
    
    double bbLowerMultiplier = 1.05; // 하단 밴드 조정값
    double bbUpperMultiplier = 0.95; // 상단 밴드 조정값
    double avgVolumeMultiplier = 1.5; // 거래량 평균 조정값
    String barInterval = BithumSeries.INTERVAL_5M; // 5분 간격

    long botDelay = 60 * 1000; // 1분 간격 실행

    public FirstBot() {
        super();
        this.name = "FirstBot";
        this.description = "A bot that uses RSI, MACD, and Bollinger Bands for trading.";
    }

    @Override
    public void ready(BotConfig config) throws IOException {

        Map<String, Object> params = config.getParams();
        if (params != null) {
            if (params.containsKey("rsiBarCount")) {
                rsiBarCount = (int) params.get("rsiBarCount");
                logger.info("{} RSI Bar Count: {}", name, rsiBarCount);
            }
            
            if (params.containsKey("rsiBottomValue")) {
                rsiBottomValue = (int) params.get("rsiBottomValue");
                logger.info("{} RSI Bottom Value: {}", name, rsiBottomValue);
            }
            
            if (params.containsKey("rsiTopValue")) {
                rsiTopValue = (int) params.get("rsiTopValue");
                logger.info("{} RSI Top Value: {}", name, rsiTopValue);
            }

            if (params.containsKey("macdShortBarCount")) {
                macdShortBarCount = (int) params.get("macdShortBarCount");
                logger.info("{} MACD Short Bar Count: {}", name, macdShortBarCount);
            }
            if (params.containsKey("macdLongBarCount")) {
                macdLongBarCount = (int) params.get("macdLongBarCount");
                logger.info("{} MACD Long Bar Count: {}", name, macdLongBarCount);
            }
            if (params.containsKey("signalBarCount")) {
                signalBarCount = (int) params.get("signalBarCount");
                logger.info("{} Signal Bar Count: {}", name, signalBarCount);
            }
            if (params.containsKey("smaBarCount")) {
                smaBarCount = (int) params.get("smaBarCount");
                logger.info("{} SMA Bar Count: {}", name, smaBarCount);
            }
            if (params.containsKey("stdDevBarCount")) {
                stdDevBarCount = (int) params.get("stdDevBarCount");
                logger.info("{} Standard Deviation Bar Count: {}", name, stdDevBarCount);
            }
            if (params.containsKey("volumeBarCount")) {
                volumeBarCount = (int) params.get("volumeBarCount");
                logger.info("{} Volume Bar Count: {}", name, volumeBarCount);
            }
            if (params.containsKey("avgVolumeBarCount")) {
                avgVolumeBarCount = (int) params.get("avgVolumeBarCount");
                logger.info("{} Average Volume Bar Count: {}", name, avgVolumeBarCount);
            }
            if (params.containsKey("bbLowerMultiplier")) {
                bbLowerMultiplier = (double) params.get("bbLowerMultiplier");
                logger.info("{} Bollinger Bands Lower Multiplier: {}", name, bbLowerMultiplier);
            }
            if (params.containsKey("bbUpperMultiplier")) {
                bbUpperMultiplier = (double) params.get("bbUpperMultiplier");
                logger.info("{} Bollinger Bands Upper Multiplier: {}", name, bbUpperMultiplier);
            }
            if (params.containsKey("avgVolumeMultiplier")) {
                avgVolumeMultiplier = (double) params.get("avgVolumeMultiplier");
                logger.info("{} Average Volume Multiplier: {}", name, avgVolumeMultiplier);
            }
            if( params.containsKey("botDelay")) {
                botDelay = (int) params.get("botDelay");
                logger.info("{} Bot Delay: {} ms", name, botDelay);
            }
            if (params.containsKey("barInterval")) {
                barInterval = (String) params.get("barInterval");
                logger.info("{} Bar Interval: {}", name, barInterval);
            }            
        }

        
        series = bithumSeries.makeSeries(BithumSeries.MARKET_BTC_KRW, barInterval);
        series.setMaximumBarCount(5000);
        logger.info("FirstBot is ready with series: {}", series.getName());

        

        closePrice = new ClosePriceIndicator(series);
        rsi = new RSIIndicator(closePrice, rsiBarCount);
        macd = new MACDIndicator(closePrice, macdShortBarCount, macdLongBarCount);
        signalLine = new EMAIndicator(macd, signalBarCount);
        sma = new SMAIndicator(closePrice, smaBarCount);
        stdDev = new StandardDeviationIndicator(closePrice, stdDevBarCount);
        bbMiddle = new BollingerBandsMiddleIndicator(sma);
        bbUpper = new BollingerBandsUpperIndicator(bbMiddle, stdDev, DecimalNum.valueOf(2));
        bbLower = new BollingerBandsLowerIndicator(bbMiddle, stdDev, DecimalNum.valueOf(2));
        volume = new VolumeIndicator(series, volumeBarCount);
        avgVolume = new SMAIndicator(volume, avgVolumeBarCount);

        // Bollinger Bands 조정값 계산
        Indicator<Num> bbLowerAdjusted = new AbstractIndicator<Num>(series) {
            @Override
            public Num getValue(int index) {
                Num value = bbLower.getValue(index).multipliedBy(DecimalNum.valueOf(bbLowerMultiplier));
                logger.info("Adjusted BB Lower Value at index {}: {}", index, value);
                return value;
            }

            @Override
            public int getUnstableBars() {
                return 20; // Bollinger Bands는 20기간 SMA와 표준편차 기반
            }
        };
        Indicator<Num> bbUpperAdjusted = new AbstractIndicator<Num>(series) {
            @Override
            public Num getValue(int index) {
                Num value = bbUpper.getValue(index).multipliedBy(DecimalNum.valueOf(bbUpperMultiplier));
                logger.info("Adjusted BB Upper Value at index {}: {}", index, value);
                return value;
            }

            @Override
            public int getUnstableBars() {
                return 20; // Bollinger Bands는 20기간 SMA와 표준편차 기반
            }
        };
        Indicator<Num> avgVolumeAdjusted = new AbstractIndicator<Num>(series) {
            @Override
            public Num getValue(int index) {
                Num value = avgVolume.getValue(index).multipliedBy(DecimalNum.valueOf(avgVolumeMultiplier));
                logger.info("Adjusted Average Volume Value at index {}: {}", index, value);
                return value;
            }

            @Override
            public int getUnstableBars() {
                return 20; // avgVolume은 20기간 SMA 기반
            }
        };



        // 매수 조건: RSI < 30 && MACD > Signal && 가격이 하단 밴드 근처 && 거래량 > 평균 1.5배
        Rule entryRule = new UnderIndicatorRule(rsi, DecimalNum.valueOf(rsiBottomValue))
                // .and(new CrossedUpIndicatorRule(macd, signalLine))
                .and(new UnderIndicatorRule(closePrice, bbLowerAdjusted));
                // .and(new OverIndicatorRule(volume, avgVolumeAdjusted));

        // 매도 조건: RSI > 70 && MACD < Signal && 가격이 상단 밴드 근처 && 거래량 > 평균 1.5배
        Rule exitRule = new OverIndicatorRule(rsi, DecimalNum.valueOf(rsiTopValue))
                // .and(new CrossedDownIndicatorRule(macd, signalLine))
                .and(new OverIndicatorRule(closePrice, bbUpperAdjusted));
                // .and(new OverIndicatorRule(volume, avgVolumeAdjusted));

        // 손절 조건: 가격이 진입가 대비 0.5% 하락
        Rule stopLossRule = new StopLossRule(closePrice, DecimalNum.valueOf(0.5));


        // strategy = new BaseStrategy("RSI_MACD_BB_Strategy", entryRule, exitRule.and(stopLossRule));
        strategy = new BaseStrategy("RSI_MACD_BB_Strategy", entryRule, exitRule.or(stopLossRule));

        this.delay = botDelay; //60 * 1000; // 5초 간격으로 업데이트
    }
 

    @Override
    public boolean update() throws IOException {
        
        // logger.info("Updating series for {}", name);

        boolean isUpdated = false;
        List<Bar> newBars = bithumSeries.fetchCandlestickData(BithumSeries.MARKET_BTC_KRW, barInterval);
        if (!newBars.isEmpty()) {
            // 최신 캔들만 추가 (중복 방지)
            Bar latestBar = newBars.get(newBars.size() - 1); // 최신 캔들
            ZonedDateTime latestBarTime = latestBar.getEndTime();
            boolean isNew = series.getBarCount() == 0 || series.getLastBar().getEndTime().isBefore(latestBarTime);
            if (isNew) {
                series.addBar(latestBar);
                logger.info("캔들 추가: {}, 총 바 수: {}", latestBarTime, series.getBarCount());

                int endIndex = series.getEndIndex();
                if (endIndex < 20) {
                    logger.warn("불충분한 바 수: {}. 최소 20개 바 필요", endIndex + 1);
                    return false;
                }
                
                isUpdated = true;                
            }
        }
        if(isUpdated) {
            logger.info("Series updated for {}", name);
        } else {
            // logger.info("No new data to update for {}", name);
        }
        return isUpdated;
    }

    @Override
    public void trade() {
        
        Bar latestBar = series.getLastBar();
        ZonedDateTime latestBarTime = latestBar.getEndTime();

        int endIndex = series.getEndIndex();
        Num rsiValue = rsi.getValue(endIndex);
        Num macdValue = macd.getValue(endIndex);
        Num signalValue = signalLine.getValue(endIndex);
        Num lowerBand = bbLower.getValue(endIndex);
        Num upperBand = bbUpper.getValue(endIndex);
        Num volumeValue = volume.getValue(endIndex);
        Num avgVolumeValue = avgVolume.getValue(endIndex);

        logger.info(
            String.format("지표 값 - RSI: %.2f, MACD: %.2f, Signal Line: %.2f, Lower Band: %.2f, Upper Band: %.2f, Volume: %.2f (Avg: %.2f)%s",
                rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue(),
                lowerBand.doubleValue(), upperBand.doubleValue(),
                volumeValue.doubleValue(), avgVolumeValue.doubleValue(),
                macdValue.isNegative() ? " (약세 구간)" : ""));

        boolean isEntry = strategy.shouldEnter(endIndex);
        boolean isExit = strategy.shouldExit(endIndex);
        if (strategy.shouldEnter(endIndex)) {
            Num entryPrice = latestBar.getClosePrice();
            tradingRecord.enter(endIndex, entryPrice, DecimalNum.valueOf(1));
            logger.info(String.format("매수 시그널(인덱스:%d): 가격=%.2f, 시간=%s, RSI=%.2f, MACD=%.2f, Signal=%.2f, Volume=%.2f",
                    endIndex,
                    entryPrice.doubleValue(), latestBarTime,
                    rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue(), volumeValue.doubleValue()));
        } else if (strategy.shouldExit(endIndex)) {
            Num exitPrice = latestBar.getClosePrice();
            tradingRecord.exit(endIndex, exitPrice, DecimalNum.valueOf(1));
            logger.info(String.format("매도 시그널(인덱스:%d): 가격=%.2f, 시간=%s, RSI=%.2f, MACD=%.2f, Signal=%.2f, Volume=%.2f",
                    endIndex,
                    exitPrice.doubleValue(), latestBarTime,
                    rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue(), volumeValue.doubleValue()));

            // 수익률 계산 (수수료 0.5% 감안)
            if (!tradingRecord.getCurrentPosition().isNew()) {
                Num profit = exitPrice.minus(tradingRecord.getLastEntry().getPricePerAsset())
                        .dividedBy(tradingRecord.getLastEntry().getPricePerAsset())
                        .multipliedBy(DecimalNum.valueOf(100))
                        .minus(DecimalNum.valueOf(0.5)); // 수수료 차감
                logger.info(String.format("거래 수익률 (수수료 후): %.2f%", profit.doubleValue()));
            }
        } else {
            logger.info(String.format("시그널 없음(인덱스:%d): 가격=%.2f, 시간=%s, RSI=%.2f, MACD=%.2f, Signal=%.2f, Volume=%.2f",
                    endIndex,
                    latestBar.getClosePrice().doubleValue(), latestBarTime,
                    rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue(), volumeValue.doubleValue()));
        }
    }

    
}
