package com.smthe.money.bot;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Rule;
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

    @Override
    public void ready() throws IOException {
        
        series = bithumSeries.makeSeries(BithumSeries.MARKET_BTC_KRW, BithumSeries.INTERVAL_5M);
        logger.info("FirstBot is ready with series: {}", series.getName());



        closePrice = new ClosePriceIndicator(series);
        rsi = new RSIIndicator(closePrice, 7);
        macd = new MACDIndicator(closePrice, 6, 13);
        signalLine = new EMAIndicator(macd, 5);
        sma = new SMAIndicator(closePrice, 20);
        stdDev = new StandardDeviationIndicator(closePrice, 20);
        bbMiddle = new BollingerBandsMiddleIndicator(sma);
        bbUpper = new BollingerBandsUpperIndicator(bbMiddle, stdDev, DecimalNum.valueOf(2));
        bbLower = new BollingerBandsLowerIndicator(bbMiddle, stdDev, DecimalNum.valueOf(2));
        volume = new VolumeIndicator(series, 20);
        avgVolume = new SMAIndicator(volume, 20);


        // 매수 조건: RSI < 30 && MACD > Signal && 가격이 하단 밴드 근처 && 거래량 > 평균 1.5배
        Rule entryRule = new UnderIndicatorRule(rsi, DecimalNum.valueOf(35))
                .and(new CrossedUpIndicatorRule(macd, signalLine))
                .and(new UnderIndicatorRule(closePrice, bbLower));
                // .and(new OverIndicatorRule(volume, avgVolume));

        // 매도 조건: RSI > 70 && MACD < Signal && 가격이 상단 밴드 근처 && 거래량 > 평균 1.5배
        Rule exitRule = new OverIndicatorRule(rsi, DecimalNum.valueOf(65))
                .and(new CrossedDownIndicatorRule(macd, signalLine))
                .and(new OverIndicatorRule(closePrice, bbUpper));
                // .and(new OverIndicatorRule(volume, avgVolume));

        // 손절 조건: 가격이 진입가 대비 0.5% 하락
        Rule stopLossRule = new StopLossRule(closePrice, DecimalNum.valueOf(0.5));


        strategy = new BaseStrategy("RSI_MACD_BB_Strategy", entryRule, exitRule.and(stopLossRule));

        this.delay = 60 * 1000; // 5초 간격으로 업데이트
    }
 

    @Override
    public boolean update() throws IOException {
        boolean isUpdated = false;
        List<Bar> newBars = bithumSeries.fetchCandlestickData(BithumSeries.MARKET_BTC_KRW, BithumSeries.INTERVAL_5M);
        if (!newBars.isEmpty()) {
            // 최신 캔들만 추가 (중복 방지)
            Bar latestBar = newBars.get(newBars.size() - 1); // 최신 캔들
            ZonedDateTime latestBarTime = latestBar.getEndTime();
            boolean isNew = series.getBarCount() == 0 || series.getLastBar().getEndTime().isBefore(latestBarTime);
            if (isNew) {
                series.addBar(latestBar);
                isUpdated = true;
                logger.info("새 캔들 추가: {}", latestBarTime);
            }
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
            String.format("지표 값 - RSI: %.2f, MACD: %.2f, Signal Line: %.2f, Lower Band: %.2f, Upper Band: %.2f, Volume: %.2f (Avg: %.2f)%s%n",
                rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue(),
                lowerBand.doubleValue(), upperBand.doubleValue(),
                volumeValue.doubleValue(), avgVolumeValue.doubleValue(),
                macdValue.isNegative() ? " (약세 구간)" : ""));

        if (strategy.shouldEnter(endIndex)) {
            Num entryPrice = latestBar.getClosePrice();
            tradingRecord.enter(endIndex, entryPrice, DecimalNum.valueOf(1));
            logger.info(String.format("매수 시그널: 가격=%.2f, 시간=%s, RSI=%.2f, MACD=%.2f, Signal=%.2f, Volume=%.2f%n",
                    entryPrice.doubleValue(), latestBarTime,
                    rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue(), volumeValue.doubleValue()));
        } else if (strategy.shouldExit(endIndex)) {
            Num exitPrice = latestBar.getClosePrice();
            tradingRecord.exit(endIndex, exitPrice, DecimalNum.valueOf(1));
            logger.info(String.format("매도 시그널: 가격=%.2f, 시간=%s, RSI=%.2f, MACD=%.2f, Signal=%.2f, Volume=%.2f%n",
                    exitPrice.doubleValue(), latestBarTime,
                    rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue(), volumeValue.doubleValue()));

            // 수익률 계산 (수수료 0.5% 감안)
            if (!tradingRecord.getCurrentPosition().isNew()) {
                Num profit = exitPrice.minus(tradingRecord.getLastEntry().getPricePerAsset())
                        .dividedBy(tradingRecord.getLastEntry().getPricePerAsset())
                        .multipliedBy(DecimalNum.valueOf(100))
                        .minus(DecimalNum.valueOf(0.5)); // 수수료 차감
                logger.info(String.format("거래 수익률 (수수료 후): %.2f%%%n", profit.doubleValue()));
            }
        } else {
            logger.info(String.format("시그널 없음: 가격=%.2f, 시간=%s, RSI=%.2f, MACD=%.2f, Signal=%.2f, Volume=%.2f%n",
                    latestBar.getClosePrice().doubleValue(), latestBarTime,
                    rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue(), volumeValue.doubleValue()));
        }
    }

    
}
