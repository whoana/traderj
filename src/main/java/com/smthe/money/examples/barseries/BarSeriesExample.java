package com.smthe.money.examples.barseries;

import java.time.Duration;
import java.time.ZonedDateTime;


import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;

public class BarSeriesExample {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BarSeriesExample.class);

    public static void main(String[] args) {
        // 여기에 BarSeries 예제 코드를 작성하세요.
        // 예를 들어, BarSeries를 생성하고 데이터를 추가하는 등의 작업을 수행할 수 있습니다.
        System.out.println("BarSeries 예제 실행 중...");

        BarSeries hoursSeries = new BaseBarSeries("HoursSeriesExample");
        
        ZonedDateTime now = ZonedDateTime.now();
        hoursSeries.addBar(Duration.ofHours(1), now, 100, 101, 99, 100.05, 1000);
        hoursSeries.addBar(Duration.ofHours(1), now.plusHours(1), 100.05, 102, 99.5, 101, 1200);
        hoursSeries.addBar(Duration.ofHours(1), now.plusHours(2), 101, 103, 100, 102.5, 1500);
        hoursSeries.addBar(Duration.ofHours(1), now.plusHours(3), 102.5, 104, 101, 103.5, 1800);
        hoursSeries.addBar(Duration.ofHours(1), now.plusHours(4), 103.5, 105, 102, 104.5, 2000);
        hoursSeries.addBar(Duration.ofHours(1), now.plusHours(5), 104.5, 106, 103, 105.5, 2200);
        hoursSeries.addBar(Duration.ofHours(1), now.plusHours(6), 105.5, 107, 104, 106.5, 2400);
        hoursSeries.addBar(Duration.ofHours(1), now.plusHours(7), 106.5, 108, 105, 107.5, 2600);
        hoursSeries.addBar(Duration.ofHours(1), now.plusHours(8), 107.5, 109, 106, 108.5, 2800);
        hoursSeries.addBar(Duration.ofHours(1), now.plusHours(9), 108.5, 110, 107, 109.5, 3000);

        BaseBar anotherBar = BaseBar.builder((num) -> DecimalNum.valueOf(num), Number.class)
                .timePeriod(Duration.ofHours(1))
                .endTime(now.plusHours(10))
                .openPrice(109.5)
                .highPrice(111)
                .lowPrice(108)
                .closePrice(110.5)
                .volume(3200)
                .amount(1000)
                .build();
        hoursSeries.addBar(anotherBar);


        for (int i = 0; i < hoursSeries.getBarCount(); i++) {
            BaseBar bar = (BaseBar)hoursSeries.getBar(i);
            logger.debug("bar[{}]: duration:{}, {}", i, bar.getTimePeriod(), bar.toString());
        }
        logger.debug("마지막 바 가격 변경 ");
        hoursSeries.addPrice(108); // 마지막 바에 가격 변경 close price를 108로 변경

        
        BaseBar bar = (BaseBar)hoursSeries.getLastBar();
        logger.debug("last bar: {}",  bar.toString());

        hoursSeries.addTrade(900, 101); // 거래 추가, 900 단위의 거래량과 101의 가격으로
        logger.debug("last bar: {}",  hoursSeries.getLastBar().toString());

    }

}
