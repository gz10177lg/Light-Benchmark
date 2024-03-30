package com.rong.test.model;

import com.rong.annotation.LightBenchmark;

import java.util.UUID;

public class UserService {
    @LightBenchmark
    public void createUUID() {
        String uuid = UUID.randomUUID().toString();
    }

    @LightBenchmark
    public void createSystemTime() {
        long time = System.currentTimeMillis();
    }
}

