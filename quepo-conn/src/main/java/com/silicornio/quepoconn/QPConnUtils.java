package com.silicornio.quepoconn;

import com.silicornio.quepotranslator.QPCodeTranslation;
import com.silicornio.quepotranslator.QPCustomTranslation;
import com.silicornio.quepotranslator.QPTransConf;
import com.silicornio.quepotranslator.QPTransManager;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by SilicorniO
 */
public class QPConnUtils {

    /**
     * Generate a translator manager with custom translations and code translations
     * Custom translations: Calendar-Data
     * Code translations: Double-0
     * @return QPTransManager instance
     */
    public static QPTransManager generateTypicalTranslatorManager(QPTransConf conf){

        QPTransManager transManager = new QPTransManager(conf);
        transManager.setCheckTranslationsFirst(false);
        transManager.addCustomTranslation(new QPCustomTranslation<Calendar, Date>() {
            @Override
            public Date onTranslation(Calendar calendar) {
                return calendar.getTime();
            }

            @Override
            public Calendar onTranslationInverse(Date date) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return calendar;
            }
        });
        transManager.addCustomTranslation(new QPCustomTranslation<Double, String>(){
            @Override
            public String onTranslation(Double aDouble) {
                if(aDouble%1 == 0) {
                    return String.valueOf(aDouble.intValue());
                }else{
                    return String.valueOf(aDouble);
                }
            }

            @Override
            public Double onTranslationInverse(String s) {
                try {
                    return Double.parseDouble(s);
                }catch(NumberFormatException nfe){
                    return new Double(0d);
                }
            }
        });
        transManager.addCodeTranslation(new Double0QPCodeTranslation());

        return transManager;
    }

    private static class Double0QPCodeTranslation extends QPCodeTranslation<Double> {

        public Double0QPCodeTranslation(){
        }

        @Override
        public boolean match(Double d) {
            return d%1 == 0;
        }

        @Override
        public Object translate(Double d) {
            return Integer.valueOf(d.intValue());
        }
    }
}
