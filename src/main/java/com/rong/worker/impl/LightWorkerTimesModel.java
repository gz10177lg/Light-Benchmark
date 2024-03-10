package com.rong.worker.impl;

import com.rong.model.TestTimeUnit;
import com.rong.worker.BaseWorkerService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

public class LightWorkerTimesModel extends BaseWorkerService {
    //禁止外部直接找到成员变量
    private Class<?> clazz;
    private long warmupTestTimes;
    private long testTimes;
    private TestTimeUnit unit;

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
        StringBuilder stb = new StringBuilder();
        stb.append(method.getName());
        stb.append(":");

        long times = super.testTimes;
        long i = 0;
        long start = System.nanoTime();
        while (i <= times) {
            method.invoke(realObj, args);
            i++;
        }
        long end = System.nanoTime();//start和end时间得挨在执行方法前后，避免其他因素影响精确时间.

        stb.append(new BigDecimal((end - start) / super.unit.getUnit()).setScale(2, BigDecimal.ROUND_HALF_UP));
        stb.append(" ");
        stb.append(super.unit.getUnitStr().split("/")[1]);
        stb.append("/");
        stb.append(times);
        stb.append(" times");

        System.out.println(stb.toString());

    }
}
