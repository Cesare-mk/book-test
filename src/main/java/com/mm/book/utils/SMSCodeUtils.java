package com.mm.book.utils;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class SMSCodeUtils {
     private  String[] num = {"00000","0000","000","00","0",""};
    public String generator(String telephone){
        int hash = telephone.hashCode();
        int encryption = 46842314;
        long result = hash ^ encryption;
        long time = System.currentTimeMillis();
        result = result ^ time;
        long code = result % 1000000;
        code = code < 0 ? -code: code;
        return num[num.length-1] + code+"";
    }
    @Cacheable(value = "SMSCode",key = "#telephone")
    public String getCacheCode(String telephone){
        return null;
    }

}
