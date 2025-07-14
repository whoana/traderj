package com.smthe.money.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

public abstract class StrategyBuilder {
    
    /**
     * Builds a trading strategy based on the provided parameters.
     *
     * @param series The parameters for the strategy.
     * @return A Strategy object representing the trading strategy.
     */
    public abstract Strategy buildStrategy(BarSeries series);

}
