package com.smthe.money.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
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

import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Strategy001 
 * RSI + MACD + Bollinger Bands 전략 설정
 */
public class StrategyBuilder001 extends StrategyBuilder {

    @Override
    public Strategy buildStrategy(BarSeries series) {
       ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 7);
        MACDIndicator macd = new MACDIndicator(closePrice, 6, 13);
        EMAIndicator signalLine = new EMAIndicator(macd, 5);
        SMAIndicator sma = new SMAIndicator(closePrice, 20);
        StandardDeviationIndicator stdDev = new StandardDeviationIndicator(closePrice, 20);
        BollingerBandsMiddleIndicator bbMiddle = new BollingerBandsMiddleIndicator(sma);
        BollingerBandsUpperIndicator bbUpper = new BollingerBandsUpperIndicator(bbMiddle, stdDev, DecimalNum.valueOf(2));
        BollingerBandsLowerIndicator bbLower = new BollingerBandsLowerIndicator(bbMiddle, stdDev, DecimalNum.valueOf(2));
        VolumeIndicator volume = new VolumeIndicator(series, 20);
        SMAIndicator avgVolume = new SMAIndicator(volume, 20);

        // 매수 조건: RSI < 30 && MACD > Signal && 가격이 하단 밴드 근처 && 거래량 > 평균 1.5배
        Rule entryRule = new UnderIndicatorRule(rsi, DecimalNum.valueOf(30))
                .and(new CrossedUpIndicatorRule(macd, signalLine))
                .and(new UnderIndicatorRule(closePrice, bbLower))
                .and(new OverIndicatorRule(volume, avgVolume));

        // 매도 조건: RSI > 70 && MACD < Signal && 가격이 상단 밴드 근처 && 거래량 > 평균 1.5배
        Rule exitRule = new OverIndicatorRule(rsi, DecimalNum.valueOf(70))
                .and(new CrossedDownIndicatorRule(macd, signalLine))
                .and(new OverIndicatorRule(closePrice, bbUpper))
                .and(new OverIndicatorRule(volume, avgVolume));

        // 손절 조건: 가격이 진입가 대비 0.5% 하락
        Rule stopLossRule = new StopLossRule(closePrice, DecimalNum.valueOf(0.5));

        return new BaseStrategy("RSI_MACD_BB_Strategy", entryRule, exitRule.and(stopLossRule));
    }
    
}
