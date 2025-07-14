package com.smthe.money.strategy;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import com.smthe.money.series.BithumSeries;

public class StrategyBuilder001Test {
    
    private final StrategyBuilder001 strategyBuilder = new StrategyBuilder001();

    @Test
    public void testBuildStrategy() throws IOException {
        
        BarSeries series = new BithumSeries().makeSeries(BithumSeries.MARKET_BTC_KRW, BithumSeries.INTERVAL_1M);
        
        Strategy strategy = strategyBuilder.buildStrategy(series);

        assertNotNull(strategy);

    }
}
