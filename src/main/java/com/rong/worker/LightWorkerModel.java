package com.rong.worker;

import java.lang.reflect.InvocationTargetException;

public interface LightWorkerModel {
    BaseWorkerService clazz(Class<?> clazz);
    void startTest()throws InstantiationException, IllegalAccessException, InvocationTargetException;
}
