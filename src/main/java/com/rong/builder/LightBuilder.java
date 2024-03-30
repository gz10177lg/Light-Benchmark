package com.rong.builder;

import com.rong.constants.LightBuilderConstant;
import com.rong.pojo.TestTimeUnit;
import com.rong.worker.BaseWorkerService;
import com.rong.worker.impl.LightWorkerOpsModel;

public class LightBuilder {
    private int model = LightBuilderConstant.OPS;
    private Class<?> clazz;
    private long warmupTestTimes = 5;
    private long testTimes = 5;
    private TestTimeUnit unit = TestTimeUnit.US;
    private BaseWorkerService service;

    public static LightBuilder build() {
        return new LightBuilder();
    }

    public LightBuilder model(int model) {
        this.model = model;
        return this;
    }

    public LightBuilder clazz(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }

    public LightBuilder unit(TestTimeUnit unit) {
        this.unit = unit;
        return this;
    }

    public LightBuilder warmupTestTimes(long warmupTestTimes) {
        this.warmupTestTimes = warmupTestTimes;
        return this;
    }

    public LightBuilder testTimes(long testTimes) {
        this.testTimes = testTimes;
        return this;
    }

    public void startTest() {
        //判空
        checkIsNull();
        try {
            //构建干活对象
            buildServiceWork();

            //干活
            this.service.workV2();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkIsNull() {
        if (clazz == null) {
            throw new RuntimeException("运行失败，没有填写测试类的class");
        }
    }

    private void buildServiceWork() {
        switch (this.model) {
            case 1:
                this.service = new LightWorkerOpsModel(clazz, warmupTestTimes, testTimes, unit);
                break;
            case 2:
                break;
            default:
                this.service = new LightWorkerOpsModel(clazz, warmupTestTimes, testTimes, unit);
        }
    }

}
