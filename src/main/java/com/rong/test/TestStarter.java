package com.rong.test;

import com.rong.builder.LightBuilder;
import com.rong.builder.LightBuilderConstant;
import com.rong.model.TestTimeUnit;
import com.rong.test.model.UserService;

public class TestStarter {
    public static void main(String[] args) {
        System.out.println("开始");
        LightBuilder.build()
                .model(LightBuilderConstant.OPS)
                .clazz(UserService.class)
                .warmupTestTimes(300_000)
                .testTimes(1_000_000)
                .unit(TestTimeUnit.US)
                .startTest();
    }
}