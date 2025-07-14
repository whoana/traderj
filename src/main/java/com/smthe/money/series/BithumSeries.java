package com.smthe.money.series;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DecimalNum;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BithumSeries {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BithumSeries.class);

    public static final String BITHUMB_API = "https://api.bithumb.com/public/candlestick/";
    public static final String MARKET_BTC_KRW = "BTC_KRW";
    public static final String INTERVAL_1M = "1m"; // 1분 간격
    public static final String INTERVAL_5M = "5m"; // 5분 간격
    public static final String INTERVAL_1H = "1h"; // 1분 간격

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    BarSeries series;

    public BarSeries makeSeries(String market, String interval) throws IOException {
        List<Bar> initialBars = fetchCandlestickData(market, interval);
        series = new BaseBarSeriesBuilder()
                .withName("BTC_KRW")
                .withNumTypeOf(DecimalNum.class)
                .withBars(initialBars)
                .build();
        return series;
    }

    public List<Bar> fetchCandlestickData(String market, String interval) throws IOException {
        String url = BITHUMB_API + market + "/" + interval;
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            JsonArray data = json.getAsJsonArray("data");
            ZoneId zoneId = ZoneId.of("Asia/Seoul");
            List<Bar> bars = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JsonArray candle = data.get(i).getAsJsonArray();
                long epochMillis = candle.get(0).getAsLong();
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
}
