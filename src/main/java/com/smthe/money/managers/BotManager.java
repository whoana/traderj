package com.smthe.money.managers;

import java.io.IOException;

import com.smthe.money.bot.FirstBot;

public class BotManager {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BotManager.class);

    // BotManager는 봇의 생성, 관리 및 실행을 담당합니다.
    // 이 클래스는 봇의 상태를 추적하고, 봇을 시작하거나 중지하는 기능을 포함할 수 있습니다.

    public void startBot() throws IOException {        
        FirstBot bot = new FirstBot();
        bot.start();
        logger.info("봇이 시작되었습니다.");
    }

    public void stopBot() {
        // 봇을 중지하는 로직을 구현합니다.
        logger.info("봇이 중지되었습니다.");
    }



    public static void main(String[] args) {
        BotManager botManager = new BotManager();
        try {
            botManager.startBot();
        } catch (IOException e) {
            logger.error("exception occured.", e);
        }

        // 봇의 실행 로직을 여기에 추가합니다.
        // 예: 특정 전략을 실행하거나, 시장 데이터를 모니터링하는 등의 작업

        // botManager.stopBot();
    }

}
