package com.ljx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public class DateUtil {
    public static Date get(String pattern){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
