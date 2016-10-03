package com.tts.app.tmaso.binding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TMAUtil {

    private static Logger logger = LoggerFactory.getLogger(TMAUtil.class);
    private static ExecutorService POOL;

    static {
        POOL = Executors.newFixedThreadPool(50);
    }

    public static void destroy() {
        POOL.shutdownNow();
    }

    public static String toString(Object... objects) {
        StringBuilder sb = new StringBuilder();
        String concat = "";
        for (Object obj : objects) {
            sb.append(concat).append(obj);
            concat = ",";
        }
        return sb.toString();
    }

    public static void runThreadSafe(final Runnable t) {
        POOL.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    t.run();
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        });
    }

}
