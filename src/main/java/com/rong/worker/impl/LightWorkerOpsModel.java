package com.rong.worker.impl;

import com.rong.model.TestTimeUnit;
import com.rong.worker.BaseWorkerService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class LightWorkerOpsModel extends BaseWorkerService {
    //禁止外部直接找到成员变量
    private Class<?> clazz;
    private long warmupTestTimes;
    private long testTimes;
    private TestTimeUnit unit;

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
        StringBuilder stb = new StringBuilder();
        stb.append(method.getName());
        stb.append(":");

        long count = 0;
        long TIMES = super.testTimes;
        long start = System.nanoTime();
        while (count <= TIMES) {
            method.invoke(realObj, args);
            count++;
        }
        long end = System.nanoTime();//start和end时间得挨在执行方法前后，避免其他因素影响精确时间.

        stb.append(new BigDecimal(TIMES / ((end - start) / super.unit.getUnit())).setScale(2, RoundingMode.HALF_UP));
        stb.append(super.unit.getUnitStr());

        System.out.println(stb.toString());
    }

}
