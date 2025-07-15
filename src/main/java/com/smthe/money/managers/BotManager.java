package com.smthe.money.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.smthe.money.bot.Bot;
import com.smthe.money.config.Configuration;

public class BotManager {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BotManager.class);

    // BotManager는 봇의 생성, 관리 및 실행을 담당합니다.
    // 이 클래스는 봇의 상태를 추적하고, 봇을 시작하거나 중지하는 기능을 포함할 수 있습니다.

    public void init() throws StreamReadException, DatabindException, IOException {
        Configuration configuration = ConfigManager.getInstance().getConfiguration();
        configuration.getBots().forEach(botConfig -> {
            logger.info("봇 이름: {}", botConfig.getName());
            logger.info("봇 설명: {}", botConfig.getDescription());
            // 추가적인 봇 설정을 로깅하거나 초기화할 수 있습니다.

            buildBot(botConfig);
        });
        logger.info("봇 매니저가 초기화되었습니다.");
    }
    
    List<Bot> bots = new ArrayList<>();

    private void buildBot(com.smthe.money.config.BotConfig botConfig) {
        // 봇을 생성하고 초기화하는 로직을 구현합니다.
        // 예: 봇의 이름, 설명, 전략 등을 설정합니다.
        logger.info("봇을 빌드 중: {}", botConfig.getName());
        // 실제 봇 객체를 생성하고 필요한 설정을 적용합니다.
        String name = botConfig.getName();
        String description = botConfig.getDescription();
        String className = botConfig.getClassName();
        logger.info("봇 이름: {}, 설명: {}, 클래스:{}", name, description , className);

        try {
            Class<?> botClass = Class.forName(className);
            
            Bot bot = (Bot)botClass.getDeclaredConstructor().newInstance();
            bot.setName(name);
            bot.setDescription(description);
            bot.ready(botConfig);
            bots.add(bot);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException e) {
            logger.error("봇 클래스 생성 중 오류 발생: {}", className, e);
        } catch (Exception e) {
            logger.error("알 수 없는 오류 발생: {}", className, e);     
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("봇 매니저가 종료됩니다. 모든 봇을 중지합니다.");
            stop();
        }));
    }

    public void start() throws IOException {        
        // FirstBot bot = new FirstBot();
        // bot.start();
        // logger.info("봇이 시작되었습니다.");
        for (Bot bot : bots) {
            try {
                bot.start();
                logger.info("봇이 시작되었습니다: {}", bot.getName());
            } catch (IOException e) {
                logger.error("봇 시작 중 오류 발생: {}", bot.getName(), e);
            }
        }
        logger.info("모든 봇이 시작되었습니다.");
    }

    public void stop() {
        for (Bot bot : bots) {
            try {
                bot.stop();
            } catch (Exception e) {
                logger.error("봇 중지 중 오류 발생: {}", bot.getName(), e);
            }
        }
        logger.info("모든 봇이 중지되었습니다.");
    }



    public static void main(String[] args) {
        BotManager botManager = new BotManager();
        try {
            botManager.init();
            botManager.start();
        } catch (IOException e) {
            logger.error("exception occured.", e);
        }

        // 봇의 실행 로직을 여기에 추가합니다.
        // 예: 특정 전략을 실행하거나, 시장 데이터를 모니터링하는 등의 작업

        // botManager.stopBot();
    }

}
