package com.scraping;

import java.util.Random;

/**
 * Created by Trevor on 5/5/2016.
 */
public abstract class VenueScraper {

    protected static void sleepBetween(int minSeconds, int maxSeconds) {
        Random random = new Random();
        int sleepMillis = random.nextInt(((maxSeconds * 1000) - (minSeconds * 1000)) + minSeconds * 1000);
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
