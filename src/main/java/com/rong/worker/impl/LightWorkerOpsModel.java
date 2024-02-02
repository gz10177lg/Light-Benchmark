package com.rong.worker.impl;

import com.rong.worker.BaseWorkerService;
import com.rong.worker.LightWorkerModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class LightWorkerOpsModel extends BaseWorkerService implements LightWorkerModel {

    @Override
    public void warmup(Method method, Object realObj, Object[] args) throws InvocationTargetException, IllegalAccessException {
        System.out.println("========" + method.getName() + "预热阶段" + "========");
        long count = 0;
        long TIMES = super.warmupTestTimes;
        while (count <= TIMES) {
            method.invoke(realObj, args);
            count++;
        }
    }


    @Override
    public void work(Method method, Object realObj, Object[] args) throws InvocationTargetException, IllegalAccessException {
        System.out.println("========" + method.getName() + "正式开始" + "========");

        long start = System.nanoTime();
        long count = 0;
        long TIMES = super.testTimes;
        while (count <= TIMES) {
            method.invoke(realObj, args);
            count++;
        }
        System.out.println(method.getName() + ":" + new BigDecimal(TIMES / ((System.nanoTime() - start) / 1e3)).setScale(2, RoundingMode.HALF_UP) + "ops/us");
    }

}
