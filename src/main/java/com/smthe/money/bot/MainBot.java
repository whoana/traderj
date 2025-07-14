package com.smthe.money.bot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
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

public class MainBot {
    private static final String BITHUMB_API = "https://api.bithumb.com/public/candlestick/BTC_KRW/1h";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();


    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MainBot.class);

    public static void main(String[] args) {
        try {
            // Bithumb API로 캔들 데이터 가져오기
            List<Bar> bars = fetchCandlestickData();

            // TA4J 시리즈 생성
            BarSeries series = new BaseBarSeriesBuilder()
                    .withName("BTC_KRW")
                    .withBars(bars)
                    .build();

            // 전략 설정
            Strategy strategy = buildStrategy(series);


            while (true) {
                
            }

        } catch (IOException e) {
            logger.error("Error fetching candles from Bithumb API", e);
        }
    }

    public static void main2(String[] args) throws IOException {
        // 캔들 데이터 가져오기
        List<Bar> bars = fetchCandlestickData();

        // TA4J 시리즈 생성
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("BTC_KRW")
                .withBars(bars)
                .build();

        // 전략 설정
        Strategy strategy = buildStrategy(series);


        



        // 백테스팅 실행
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord tradingRecord = manager.run(strategy);
        
        // 결과 출력
        System.out.println("총 거래 횟수: " + tradingRecord.getTrades().size());
        // tradingRecord.getTrades().forEach(trade ->
        //         System.out.println("진입: " + trade.getEntry() + ", 종료: " + trade.getExit()));
        tradingRecord.getTrades().forEach(trade -> {
            System.out.println("trade: " + trade.toString());
        });

        
    }

    // Bithumb API로 캔들 데이터 가져오기
    private static List<Bar> fetchCandlestickData() throws IOException {
        Request request = new Request.Builder().url(BITHUMB_API).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            JsonArray data = json.getAsJsonArray("data");
            // 원하는 타임존 (예: UTC 또는 Asia/Seoul 등)
            ZoneId zoneId = ZoneId.of("Asia/Seoul");

            List<Bar> bars = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JsonArray candle = data.get(i).getAsJsonArray();
                // ZonedDateTime timestamp = ZonedDateTime.parse(candle.get(0).getAsString() + "+0900",
                //         DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ"));
                // 변환
                long epochMillis = candle.get(0).getAsLong();
                // ZonedDateTime으로 변환
                ZonedDateTime timestamp = Instant.ofEpochMilli(epochMillis).atZone(zoneId);

                double open = candle.get(1).getAsDouble();
                double high = candle.get(2).getAsDouble();
                double low = candle.get(3).getAsDouble();
                double close = candle.get(4).getAsDouble();
                double volume = candle.get(5).getAsDouble();

                bars.add(new BaseBar(Duration.ofHours(1), timestamp, DecimalNum.valueOf(open),
                        DecimalNum.valueOf(high), DecimalNum.valueOf(low), DecimalNum.valueOf(close),
                        DecimalNum.valueOf(volume), DecimalNum.valueOf(0)));
            }
            return bars;
        }
    }

    // RSI + MACD 전략 설정
    private static Strategy buildStrategy(BarSeries series) {
        // 지표 설정
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        Indicator<Num> signalLine = macd.getSignalLine(9);

        // 매수 조건: RSI < 30 (과매도) && MACD > Signal (골든 크로스)
        Rule entryRule = new UnderIndicatorRule(rsi, DecimalNum.valueOf(35))
                .and(new CrossedUpIndicatorRule(macd, signalLine));

        // 매도 조건: RSI > 70 (과매수) && MACD < Signal (데드 크로스)
        Rule exitRule = new OverIndicatorRule(rsi, DecimalNum.valueOf(65))
                .and(new CrossedDownIndicatorRule(macd, signalLine));

        return new BaseStrategy(entryRule, exitRule);
    }
}