package com.Mike12138210.SmartNote.service.impl;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    // 存储每个用户当天调用次数
    private final Map<Long, Integer> callCounts = new ConcurrentHashMap<>(); // ConcurrentHashMap允许多线程安全地读写，适合做计数器缓存
    // 存储每个用户最后一次调用的日期（用于跨天重置）
    private final Map<Long, LocalDate> lastCallDate = new ConcurrentHashMap<>(); // 一旦final修饰的变量被引用，就不能再赋给其他对象，但值依然可以修改
    private final int MAX_CALLS_PER_DAY = 20;

    public void check(Long userID){
        LocalDate today = LocalDate.now();
        LocalDate lastDay = lastCallDate.get(userID);

        if(lastDay == null || !lastDay.equals(today)){
            callCounts.put(userID,0);
            lastCallDate.put(userID,today);
        }
        int count = callCounts.getOrDefault(userID,0);
        if(count >= MAX_CALLS_PER_DAY){
            throw new RuntimeException("今日调用次数已达上限（20次），请明日再试");
        }
        callCounts.put(userID,count + 1);
    }
}