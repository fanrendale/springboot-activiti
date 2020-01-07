package com.xjf.act.util;

import java.util.UUID;

public class UUIDUtil {
    public static String getUUID(){
        //转化为String对象
        String uuid = UUID.randomUUID().toString();
        //因为UUID本身为32位只是生成时多了“-”，所以将它们去掉就可以
        return uuid.replace("-", "");
    }
}
