package com.rong.worker;

import com.rong.annotation.LightBenchmark;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class BaseWorkerService {
    private Class<?> clazz;
    public long warmupTestTimes = 500_000;
    public long testTimes = 1_000_000;

    public abstract void work(Method method, Object realObj, Object[] args) throws InvocationTargetException, IllegalAccessException;

    public abstract void warmup(Method method, Object realObj, Object[] args) throws InvocationTargetException, IllegalAccessException;

    public BaseWorkerService clazz(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }

    public BaseWorkerService warmupTestTimes(long warmupTestTimes) {
        this.warmupTestTimes = warmupTestTimes;
        return this;
    }

    public BaseWorkerService testTimes(long testTimes) {
        this.testTimes = testTimes;
        return this;
    }

    private <T> T createProxyObject() throws InstantiationException, IllegalAccessException {
        T realObj = (T) clazz.newInstance();
        T proxyObj;

        if (clazz.getInterfaces().length > 0) {
            //有接口就用JDK.Proxy
            proxyObj = (T) Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    clazz.getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.isAnnotationPresent(LightBenchmark.class)) {
                                warmup(method, realObj, args);
                                work(method, realObj, args);
                            }
                            return null;
                        }
                    }
            );
        } else {
            //没有接口就用Cglib
            proxyObj = (T) Enhancer.create(clazz, new MethodInterceptor() {
                @Override
                public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                    if (method.isAnnotationPresent(LightBenchmark.class)) {
                        warmup(method, realObj, objects);
                        work(method, realObj, objects);
                    }
                    return null;
                }
            });
        }
        return proxyObj;
    }

    public void startTest() {
        Object proxyObject = null;
        try {
            proxyObject = createProxyObject();
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(LightBenchmark.class)) {
                method.invoke(proxyObject);
            }
        }
        } catch (Exception e) {
            throw new RuntimeException("出错啦："+e);
        }
    }
}
