package com.rong.test;

import com.rong.builder.LightBuilder;
import com.rong.constants.LightBuilderConstant;
import com.rong.pojo.TestTimeUnit;
import com.rong.test.model.UserService;

public class TestStarter {
    public static void main(String[] args) {
        System.out.println("开始");
        LightBuilder.build()
                .model(LightBuilderConstant.OPS)
                .clazz(UserService.class)
                .warmupTestTimes(5)
                .testTimes(5)
                .unit(TestTimeUnit.US)
                .startTest();
    }
}