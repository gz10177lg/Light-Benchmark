package com.rong.builder.v2;

import com.rong.annotation.LightBenchmark;
import com.rong.pojo.TestTimeUnit;
import com.rong.worker.BaseWorkerService;
import javassist.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LightBuilderJavassistV2 {
    private Class<?> clazz;
    private long warmupTestTimes;
    private long testTimes;
    private TestTimeUnit unit;
    private BaseWorkerService baseWorkerService;

    public LightBuilderJavassistV2(Class<?> clazz, long warmupTestTimes, long testTimes, TestTimeUnit unit, BaseWorkerService baseWorkerService) {
        this.clazz = clazz;
        this.warmupTestTimes = warmupTestTimes;
        this.testTimes = testTimes;
        this.unit = unit;
        this.baseWorkerService = baseWorkerService;
    }

    private void makeClass(Class<?> clazz) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        List<String> baseWorkerNameCtClassList = new ArrayList<>();
        StringBuilder stb = new StringBuilder();

        CtClass ctClassReal = classPool.get(clazz.getName());

        String proxyClassName = clazz.getPackage().getName() + ".$Light$Proxy$" + clazz.getSimpleName();
        CtClass ctClassProxy = classPool.makeClass(proxyClassName);

        for (CtMethod m : ctClassReal.getDeclaredMethods()) {
            if (m.hasAnnotation(LightBenchmark.class)) {
                CtMethod ctMethodHas = new CtMethod(m, ctClassProxy, null);
                ctMethodHas.setModifiers(Modifier.PUBLIC);
                ctClassProxy.addMethod(ctMethodHas);

                String taskClassName = clazz.getPackage().getName() + ".$BaseWorkerServiceTask$" + clazz.getSimpleName() + "$" + m.getName();

                CtClass ctClassBaseTask = classPool.makeClass(taskClassName);

                ctClassBaseTask.addInterface(classPool.get("java.lang.Runnable"));

                CtField ctFieldWorker = new CtField(ctClassProxy, "worker", ctClassBaseTask);
                ctClassBaseTask.addField(ctFieldWorker);

                CtConstructor ctConstructorBaseTaskWorker = new CtConstructor(new CtClass[]{ctClassProxy}, ctClassBaseTask);
                ctConstructorBaseTaskWorker.setBody("{this.worker = $1;}");
                ctClassBaseTask.addConstructor(ctConstructorBaseTaskWorker);

                CtMethod ctMethodRun = new CtMethod(CtClass.voidType, "run", new CtClass[0], ctClassBaseTask);
                ctMethodRun.setModifiers(Modifier.PUBLIC);
                ctMethodRun.setBody("{" + baseWorkerService.taskBody(m.getName(), unit) + "}");
                ctClassBaseTask.addMethod(ctMethodRun);
                baseWorkerNameCtClassList.add(taskClassName);
                ctClassBaseTask.toClass();

//                ctClassBaseTask.writeFile();  //task落盘
            }
        }

        stb.append("{");
        stb.append(baseWorkerService.proxyBody(baseWorkerNameCtClassList, warmupTestTimes, testTimes));
        stb.append("}");

        CtMethod ctMethodExecute = new CtMethod(CtClass.voidType, "execute", new CtClass[0], ctClassProxy);
        ctMethodExecute.setBody(stb.toString());
        ctClassProxy.addMethod(ctMethodExecute);
//        ctClassProxy.writeFile(); //proxy落盘

    }

    public void executeProxyObj() throws Exception {
        makeClass(clazz);

        ClassPool classPool = ClassPool.getDefault();
        CtClass ctProxyClass = classPool.get(clazz.getPackage().getName() + ".$Light$Proxy$" + clazz.getSimpleName());
        Class<?> poxyClazz = ctProxyClass.toClass();
        Object proxyObj = poxyClazz.newInstance();
        Method method = poxyClazz.getDeclaredMethod("execute");
        method.invoke(proxyObj);
    }
}

