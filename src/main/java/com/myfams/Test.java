package com.myfams;

import java.util.Calendar;
import java.util.Date;

/**
 * @author stan.c
 * @description
 * @date Created in 2019/1/25 10:05
 */
public class Test {
    public static void main(String[] args){
        System.out.println(compareDate(new Date(),18,0,0));

    }

    public static boolean compareDate(Date date,int hour_of_day,int minute,int second){
        Calendar c = Calendar.getInstance();
        try {
            c.set(Calendar.HOUR_OF_DAY, hour_of_day);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, second);
            return date.getTime() > c.getTime().getTime();
        }catch(Exception ex){
            return false;
        }
    }
}
