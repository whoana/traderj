package com.smthe.money.bot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot2 {
    private static final String BITHUMB_API = "https://api.bithumb.com/public/candlestick/BTC_KRW/1m";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static BarSeries series;
    private static Strategy strategy;
    private static TradingRecord tradingRecord;

    public static void main(String[] args) throws IOException {
        // 초기 캔들 데이터 로드
        List<Bar> initialBars = fetchCandlestickData();
        series = new BaseBarSeriesBuilder()
                .withName("BTC_KRW")
                .withNumTypeOf(DecimalNum.class)
                .withBars(initialBars)
                .build();

        // 전략 초기화
        strategy = buildStrategy(series);
        tradingRecord = new BaseTradingRecord();

        // 1분마다 캔들 데이터 갱신 및 트레이딩 수행
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateAndTrade();
            } catch (IOException e) {
                System.err.println("캔들 데이터 갱신 중 오류: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);

        // 프로그램 종료 방지 (테스트용)
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            scheduler.shutdown();
        }
    }

    // Bithumb API로 최신 1분 캔들 데이터 가져오기
    private static List<Bar> fetchCandlestickData() throws IOException {
        Request request = new Request.Builder().url(BITHUMB_API).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            JsonArray data = json.getAsJsonArray("data");
            ZoneId zoneId = ZoneId.of("Asia/Seoul");
            List<Bar> bars = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JsonArray candle = data.get(i).getAsJsonArray();
                // ZonedDateTime timestamp = ZonedDateTime.parse(candle.get(0).getAsString() + "+0900",
                //         DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ"));
                long epochMillis = candle.get(0).getAsLong();
                // ZonedDateTime으로 변환
                ZonedDateTime timestamp = Instant.ofEpochMilli(epochMillis).atZone(zoneId);


                double open = candle.get(1).getAsDouble();
                double high = candle.get(2).getAsDouble();
                double low = candle.get(3).getAsDouble();
                double close = candle.get(4).getAsDouble();
                double volume = candle.get(5).getAsDouble();

                bars.add(new BaseBar(Duration.ofMinutes(1), timestamp, DecimalNum.valueOf(open),
                        DecimalNum.valueOf(high), DecimalNum.valueOf(low), DecimalNum.valueOf(close),
                        DecimalNum.valueOf(volume), DecimalNum.valueOf(0)));
            }
            return bars;
        }
    }

    // 최신 캔들 추가 및 트레이딩
    private static void updateAndTrade() throws IOException {
        // 최신 캔들 데이터 가져오기
        List<Bar> newBars = fetchCandlestickData();
        if (newBars.isEmpty()) return;

        // 최신 캔들만 추가 (중복 방지)
        Bar latestBar = newBars.get(newBars.size() - 1); // 최신 캔들
        ZonedDateTime latestBarTime = latestBar.getEndTime();
        boolean isNew = series.getBarCount() == 0 ||
                series.getLastBar().getEndTime().isBefore(latestBarTime);

        if (isNew) {
            series.addBar(latestBar);
            System.out.println("새 캔들 추가: " + latestBarTime);


            // 전략 평가
            int endIndex = series.getEndIndex();
            // if (strategy.shouldEnter(endIndex)) {
            //     tradingRecord.enter(endIndex, latestBar.getClosePrice(), DecimalNum.valueOf(1));
            //     System.out.println("매수 시그널: 가격=" + latestBar.getClosePrice() + ", 시간=" + latestBarTime);
            // } else if (strategy.shouldExit(endIndex)) {
            //     tradingRecord.exit(endIndex, latestBar.getClosePrice(), DecimalNum.valueOf(1));
            //     System.out.println("매도 시그널: 가격=" + latestBar.getClosePrice() + ", 시간=" + latestBarTime);
            // } else {
            //     System.out.println("신호 없음: 가격=" + latestBar.getClosePrice() + " 시간=" + latestBarTime);
            // }


            // 지표 값 계산
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            RSIIndicator rsi = new RSIIndicator(closePrice, 14);
            MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
            EMAIndicator signalLine = new EMAIndicator(macd, 9);
            Num rsiValue = rsi.getValue(endIndex);
            Num macdValue = macd.getValue(endIndex);
            Num signalValue = signalLine.getValue(endIndex);



            // 로그 출력
            System.out.printf("지표 값 - RSI: %.2f, MACD: %.2f, Signal Line: %.2f%n",
                    rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue());

            if (strategy.shouldEnter(endIndex)) {
                tradingRecord.enter(endIndex, latestBar.getClosePrice(), DecimalNum.valueOf(1));
                System.out.printf("매수 시그널: 가격=%.2f, 시간=%s, RSI=%.2f, MACD=%.2f, Signal=%.2f%n",
                        latestBar.getClosePrice().doubleValue(), latestBarTime,
                        rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue());
            } else if (strategy.shouldExit(endIndex)) {
                tradingRecord.exit(endIndex, latestBar.getClosePrice(), DecimalNum.valueOf(1));
                System.out.printf("매도 시그널: 가격=%.2f, 시간=%s, RSI=%.2f, MACD=%.2f, Signal=%.2f%n",
                        latestBar.getClosePrice().doubleValue(), latestBarTime,
                        rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue());
            } else {
                System.out.printf("시그널 없음: 가격=%.2f, 시간=%s, RSI=%.2f, MACD=%.2f, Signal=%.2f%n",
                        latestBar.getClosePrice().doubleValue(), latestBarTime,
                        rsiValue.doubleValue(), macdValue.doubleValue(), signalValue.doubleValue());
            }
        }
    }

    // RSI + MACD 전략 설정
    private static Strategy buildStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator signalLine = new EMAIndicator(macd, 9);

        // 매수 조건: RSI < 30 (과매도) && MACD > Signal (골든 크로스)
        Rule entryRule = new UnderIndicatorRule(rsi, DecimalNum.valueOf(35))
                .and(new CrossedUpIndicatorRule(macd, signalLine));

        // 매도 조건: RSI > 70 (과매수) && MACD < Signal (데드 크로스)
        Rule exitRule = new OverIndicatorRule(rsi, DecimalNum.valueOf(65))
                .and(new CrossedDownIndicatorRule(macd, signalLine));

        return new BaseStrategy("RSI_MACD_Strategy", entryRule, exitRule);
    }
}
