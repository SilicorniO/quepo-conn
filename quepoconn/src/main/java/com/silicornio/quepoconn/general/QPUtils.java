package com.silicornio.quepoconn.general;


import java.util.HashMap;
import java.util.Map;

public class QPUtils {

    //------ COUNTERS ------

    private static Map<String,Long> counters = new HashMap<String, Long>();

    public static void startCounter(String tag){
        long currentTime = System.currentTimeMillis();
        counters.put(tag, currentTime);
    }

    public static long endCounter(String tag){
        if(counters.containsKey(tag)){
            return System.currentTimeMillis()-counters.get(tag);
        }else{
            return 0;
        }
    }

    public static void endCounter(String tag, long minValue){
        endCounter(tag, minValue, null);
    }

    public static void endCounter(String tag, long minValue, String textToAdd){
        long currentTime = System.currentTimeMillis();
        if(counters.containsKey(tag)){
            long value = currentTime-counters.get(tag);
            if(value>minValue) {
                QPL.i("COUNTER " + tag + ": " + value + (textToAdd!=null? " - " + textToAdd : ""));
            }
        }
    }
}
