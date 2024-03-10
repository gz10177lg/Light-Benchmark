package com.rong.builder;

import com.rong.annotation.LightBenchmark;
import com.rong.model.TestTimeUnit;
import com.rong.worker.BaseWorkerService;
import com.rong.worker.impl.LightWorkerOpsModel;
import com.rong.worker.impl.LightWorkerTimesModel;

import java.lang.reflect.Method;

public class LightBuilder {
    private BaseWorkerService service;

    public static LightBuilder build() {
        return new LightBuilder();
    }

    public LightBuilder model(int model) {
        switch (model) {
            case 1:
                this.service = new LightWorkerOpsModel();
                break;
            case 2:
                this.service = new LightWorkerTimesModel();
                break;
            default:
                return null;
        }
        return this;
    }


    public LightBuilder clazz(Class<?> clazz) {
        this.service.clazz = clazz;
        return this;
    }

    public LightBuilder unit(TestTimeUnit unit) {
        this.service.unit = unit;
        return this;
    }

    public LightBuilder warmupTestTimes(long warmupTestTimes) {
        this.service.warmupTestTimes = warmupTestTimes;
        return this;
    }

    public LightBuilder testTimes(long testTimes) {
        this.service.testTimes = testTimes;
        return this;
    }

    public void startTest() {
        Object proxyObject = null;
        try {
            proxyObject = this.service.createProxyObject();
            for (Method method : this.service.clazz.getMethods()) {
                if (method.isAnnotationPresent(LightBenchmark.class)) {
                    method.invoke(proxyObject);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("出错啦：" + e);
        }
    }

}
