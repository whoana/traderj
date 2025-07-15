package com.smthe.money.bot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import com.smthe.money.config.BotConfig;

public abstract class Bot implements Runnable {

    Logger logger = LoggerFactory.getLogger(Bot.class.getName());

    protected String name;
    protected String description;

    Thread botThread;

    protected BarSeries series;
    protected Strategy strategy;
    protected long delay = 1000; // 1초
    protected long exceptionDelay = 1000; // 예외 발생 시 1초 대기

    public abstract void ready(BotConfig config) throws IOException;

    public abstract boolean update() throws IOException;

    public abstract void trade();

    /**
     * Starts the bot with the given parameters.
     *
     * @param args The parameters for starting the bot.
     * @throws IOException
     */
    public void start() throws IOException {
        if (botThread != null && botThread.isAlive()) {
            // throw new IllegalStateException("Bot is already running");
            logger.info("Bot is already running. Stopping the current instance before starting a new one.");
            stop();
        }
        botThread = new Thread(this);
        botThread.start();
        logger.info("Bot started successfully.");
    }

    /**
     * Stops the bot gracefully.
     */
    public void stop() {
        logger.info("Stopping the bot...");
        if (botThread != null && botThread.isAlive()) {
            botThread.interrupt();
            try {
                botThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }
    }

    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Bot의 주 작업을 수행합니다.
                boolean isUpdate = update();
                if (isUpdate)
                    trade();
                Thread.sleep(delay); // 1초 대기
            } catch (IOException e) {
                logger.error("Error during bot execution: {}", e);
                try {
                    Thread.sleep(exceptionDelay);
                } catch (InterruptedException ex) {
                }
            } catch (InterruptedException e) {
                logger.info("Bot thread interrupted, stopping execution.");
                break; // 스레드가 인터럽트되면 루프 종료
            }
        }
        logger.info("Bot execution stopped.");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
