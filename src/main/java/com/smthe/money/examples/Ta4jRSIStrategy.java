package com.smthe.money.examples;
import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Ta4jRSIStrategy {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Ta4jRSIStrategy.class);

    // 캔들 데이터를 나타내는 클래스
    static class Candle {
        String market;
        String candleDateTimeKst;
        double openingPrice;
        double highPrice;
        double lowPrice;
        double tradePrice;
        long timestamp;
        double candleAccTradePrice;
        double candleAccTradeVolume;
        int unit;

        // 생성자 (JSON 데이터를 기반으로)
        Candle(String market, String candleDateTimeKst, double openingPrice, double highPrice,
               double lowPrice, double tradePrice, long timestamp, double candleAccTradePrice,
               double candleAccTradeVolume, int unit) {
            this.market = market;
            this.candleDateTimeKst = candleDateTimeKst;
            this.openingPrice = openingPrice;
            this.highPrice = highPrice;
            this.lowPrice = lowPrice;
            this.tradePrice = tradePrice;
            this.timestamp = timestamp;
            this.candleAccTradePrice = candleAccTradePrice;
            this.candleAccTradeVolume = candleAccTradeVolume;
            this.unit = unit;
        }
    }

    // BarSeries에 캔들 데이터를 추가하는 메서드
    private static void addCandleToSeries(BarSeries series, Candle candle) {
        ZonedDateTime time = ZonedDateTime.parse(
            candle.candleDateTimeKst, 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.of("Asia/Seoul")));
        series.addBar(time, candle.openingPrice, candle.highPrice, 
                      candle.lowPrice, candle.tradePrice, candle.candleAccTradeVolume);
    }

    // RSI 전략 실행
    private static void runRSIStrategy(List<Candle> candles) {
        // BarSeries 초기화
        BarSeries series = new BaseBarSeriesBuilder().withName("KRW-BTC").build();

        // 초기 24개 캔들 데이터 추가
        for (Candle candle : candles) {
            addCandleToSeries(series, candle);
        }

        // RSI 지표 생성 (14기간)
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);

        // 매수/매도 규칙 정의
        Rule buyingRule = new UnderIndicatorRule(rsiIndicator, 30); // RSI <= 30
        Rule sellingRule = new OverIndicatorRule(rsiIndicator, 70); // RSI >= 70

        // 전략 생성
        Strategy strategy = new BaseStrategy(buyingRule, sellingRule);
        logger.debug("RSI 전략 : {}", strategy.toString());

        // 최신 데이터에 대해 전략 실행
        int endIndex = series.getEndIndex();
        if (endIndex >= 14) { // RSI 계산에 최소 14개 데이터 필요
            double rsiValue = rsiIndicator.getValue(endIndex).doubleValue();
            System.out.printf("Latest RSI: %.2f%n", rsiValue);
            if (buyingRule.isSatisfied(endIndex)) {
                logger.debug("매수 신호: RSI <= 30");
            } else if (sellingRule.isSatisfied(endIndex)) {
                logger.debug("매도 신호: RSI >= 70");
            } else {
                logger.debug("신호 없음");
            }
        } else {
            logger.debug("데이터 부족: RSI 계산을 위해 최소 14개 캔들 필요");
        }
    }

    // 롤링 윈도우 시뮬레이션
    private static void simulateRollingWindow(List<Candle> initialCandles, Candle newCandle) {
        List<Candle> currentCandles = new ArrayList<>(initialCandles);
        
        // 새 캔들 추가 및 가장 오래된 캔들 제거
        currentCandles.add(newCandle); // 마지막 추가
        if (currentCandles.size() > 24) {
            currentCandles.remove(0); // 맨 앞 제거
        }

        // RSI 전략 실행
        logger.debug("=== 새로운 1시간 데이터 처리 ===");
        runRSIStrategy(currentCandles);
    }

    public static void main(String[] args) {
        // 초기 24개 캔들 데이터 (예시로 1개만 제공되었으므로 더미 데이터 생성)
        List<Candle> candles = new ArrayList<>();
        Candle sampleCandle = new Candle(
            "KRW-BTC", "2025-07-05T20:00:00", 147888000, 148000000, 
            147720000, 147953000, 1751727597656L, 3316375829.51702, 22.42559069, 240
        );

        // 더미 데이터로 24개 채우기 (단순화를 위해 동일한 캔들 복사)
        for (int i = 0; i < 24; i++) {
            candles.add(new Candle(
                sampleCandle.market,
                "2025-07-05T" + String.format("%02d:00:00", 0 + i),
                sampleCandle.openingPrice + i * 100000,
                sampleCandle.highPrice + i * 100000,
                sampleCandle.lowPrice + i * 100000,
                sampleCandle.tradePrice + i * 100000,
                sampleCandle.timestamp + i * 3600000,
                sampleCandle.candleAccTradePrice,
                sampleCandle.candleAccTradeVolume,
                sampleCandle.unit
            ));
        }

        // 초기 RSI 전략 실행
        logger.debug("=== 초기 24개 데이터로 RSI 전략 실행 ===");
        runRSIStrategy(candles);

        // 새로운 캔들 데이터로 롤링 윈도우 시뮬레이션
        Candle newCandle = new Candle(
            "KRW-BTC", "2025-07-06T00:00:00", 148000000, 148200000, 
            147900000, 148050000, 1751731197656L, 3400000000.0, 23.0, 240
        );
        simulateRollingWindow(candles, newCandle);
    }
}