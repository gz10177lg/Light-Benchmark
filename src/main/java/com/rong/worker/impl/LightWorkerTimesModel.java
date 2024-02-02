package com.rong.worker.impl;

import com.rong.worker.BaseWorkerService;
import com.rong.worker.LightWorkerModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LightWorkerTimesModel extends BaseWorkerService implements LightWorkerModel {
    @Override
    public void warmup(Method method, Object realObj, Object[] args) throws InvocationTargetException, IllegalAccessException {
        System.out.println("========" + method.getName() + "预热阶段" + "========");
        long times = super.warmupTestTimes;
        long i = 0;

        while (i <= times) {
            method.invoke(realObj, args);
            i++;
        }

    }

    @Override
    public void work(Method method, Object realObj, Object[] args) throws InvocationTargetException, IllegalAccessException {
        System.out.println("========" + method.getName() + "正式开始" + "========");
        long start = System.nanoTime();
        long times = super.testTimes;
        long i = 0;

        while (i <= times) {
            method.invoke(realObj, args);
            i++;
        }

        System.out.println(method.getName() + ":" + (System.nanoTime() - start) / 1e3 + " us/ " + times + " times");
    }
}
