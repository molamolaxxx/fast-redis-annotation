package com.mola.redis.enmu;

public enum CacheType {
//    正常读取缓存，没有则将返回值存入缓存
    NORMAL,
//    不读取，直接更新缓存
    UPDATE
}
