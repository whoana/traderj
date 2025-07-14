package com.smthe.money.series;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.ta4j.core.BarSeries;

public class BithumSeriesTest {
    
    Logger logger = org.slf4j.LoggerFactory.getLogger(BithumSeriesTest.class);

    private final BithumSeries bithumSeries = new BithumSeries();

    @Test
    public void testMakeSeries() throws IOException {
        // BithumSeries의 makeSeries 메서드를 호출하여 시리즈를 생성합니다.
        {
            BarSeries series = bithumSeries.makeSeries(BithumSeries.MARKET_BTC_KRW, BithumSeries.INTERVAL_1M);
            logger.info("Market: {}, interval : {} ,  BarSeries: bar count: {}",BithumSeries.MARKET_BTC_KRW, BithumSeries.INTERVAL_1M,  series.getBarCount());
            logger.info("Last Bar info : {}", series.getLastBar().toString());    
        }
        {
            BarSeries series = bithumSeries.makeSeries(BithumSeries.MARKET_BTC_KRW, BithumSeries.INTERVAL_1H);
            logger.info("Market: {}, interval : {} ,  BarSeries: bar count: {}",BithumSeries.MARKET_BTC_KRW, BithumSeries.INTERVAL_1H,  series.getBarCount());
            logger.info("Last Bar info : {}", series.getLastBar().toString());
        }


        
    }
}
