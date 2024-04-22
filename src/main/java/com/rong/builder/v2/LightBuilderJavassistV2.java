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
    private int threads;
    private BaseWorkerService baseWorkerService;

    public LightBuilderJavassistV2(Class<?> clazz, long warmupTestTimes, long testTimes, TestTimeUnit unit, int threads, BaseWorkerService baseWorkerService) {
        this.clazz = clazz;
        this.warmupTestTimes = warmupTestTimes;
        this.testTimes = testTimes;
        this.unit = unit;
        this.threads = threads;
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

    private void makeClassAsync(Class<?> clazz) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClassReal = classPool.get(clazz.getName());
        StringBuilder stb = new StringBuilder();

        List<String> ctMethodTaskNameList = new ArrayList<>();

        String proxyClassName = clazz.getPackage().getName() + ".$Light$Proxy$" + clazz.getSimpleName();
        CtClass ctClassProxy = classPool.makeClass(proxyClassName);
        CtField ctFieldRunning = new CtField(CtClass.booleanType, "running", ctClassProxy);
        ctFieldRunning.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.VOLATILE);
        ctClassProxy.addField(ctFieldRunning, CtField.Initializer.constant(true));

        for (CtMethod m : ctClassReal.getDeclaredMethods()) {
            if (m.hasAnnotation(LightBenchmark.class)) {
                CtMethod ctMethodHas = new CtMethod(m, ctClassProxy, null);
                ctMethodHas.setModifiers(Modifier.PUBLIC);
                ctClassProxy.addMethod(ctMethodHas);

                String taskClassName = clazz.getPackage().getName() + ".$BaseWorkerServiceTask$" + clazz.getSimpleName() + "$" + m.getName();

                CtClass ctClassBaseTask = classPool.makeClass(taskClassName);

                ctClassBaseTask.addInterface(classPool.get("java.util.function.Supplier"));

                CtField ctFieldWorker = new CtField(ctClassProxy, "worker", ctClassBaseTask);
                ctClassBaseTask.addField(ctFieldWorker);

                CtConstructor ctConstructorBaseTaskWorker = new CtConstructor(new CtClass[]{ctClassProxy}, ctClassBaseTask);
                ctConstructorBaseTaskWorker.setBody("{this.worker = $1;}");
                ctClassBaseTask.addConstructor(ctConstructorBaseTaskWorker);

                CtMethod ctMethodGet = new CtMethod(classPool.get("java.lang.Object"), "get", new CtClass[0], ctClassBaseTask);
                ctMethodGet.setModifiers(Modifier.PUBLIC);
                ctMethodGet.setBody("{" + baseWorkerService.taskBodyAsync(m.getName()) + "}");
                ctClassBaseTask.addMethod(ctMethodGet);
                ctClassBaseTask.toClass();

                ctMethodTaskNameList.add(taskClassName);

//                ctClassBaseTask.writeFile();
            }
        }

        //新建allOf类
        String taskAllOfClassName = clazz.getPackage().getName() + ".$BaseWorkerServiceTaskAllOf$" + clazz.getSimpleName();
        CtClass ctClassTaskAllOf = classPool.makeClass(taskAllOfClassName);
        ctClassTaskAllOf.addInterface(classPool.get("java.lang.Runnable"));
        CtClass ctClassCompletableFuture = classPool.get("java.util.concurrent.CompletableFuture");
        CtClass[] ctConstructorTaskAllOfArray = new CtClass[threads];
        StringBuilder stbTaskAllOfConstructor = new StringBuilder();


        StringBuilder stbTaskAllOfLong = new StringBuilder();
        StringBuilder stbTaskAllTotal = new StringBuilder("java.math.BigDecimal total = new java.math.BigDecimal(0)");
        stbTaskAllOfLong.append("java.math.BigDecimal unit = new java.math.BigDecimal(" + unit.getUnit() + ");");
        for (int i = 0; i < threads; i++) {
            CtField ctFieldFuture = new CtField(ctClassCompletableFuture, "future" + i, ctClassTaskAllOf);
            ctClassTaskAllOf.addField(ctFieldFuture);
            ctConstructorTaskAllOfArray[i] = ctClassCompletableFuture;
            stbTaskAllOfConstructor.append("this.future").append(i).append(" = $").append(i + 1).append(";\n");

            stbTaskAllOfLong.append("java.lang.Long result").append(i).append(" = (java.lang.Long)future").append(i).append(".get();\n");
            stbTaskAllOfLong.append("java.math.BigDecimal long").append(i).append(" = ").append("new java.math.BigDecimal(result").append(i).append(".longValue()).divide(unit,2, java.math.RoundingMode.HALF_UP);\n");
//                stbTaskAllOfLong.append("System.out.println(").append("result").append(i).append(");\n");
            stbTaskAllTotal.append(".add(").append("long").append(i).append(")");
            if (i == threads - 1) {
                stbTaskAllTotal.append(";\n");
            }

        }

        //allOf构造函数
        CtConstructor ctConstructorTaskAllOf = new CtConstructor(ctConstructorTaskAllOfArray, ctClassTaskAllOf);
        ctConstructorTaskAllOf.setBody("{" + stbTaskAllOfConstructor.toString() + "}");
        ctClassTaskAllOf.addConstructor(ctConstructorTaskAllOf);

        //allOf重写run方法
        CtMethod ctMethodTaskAllOfGet = new CtMethod(CtClass.voidType, "run", new CtClass[0], ctClassTaskAllOf);
        StringBuilder stbTaskAllOfEnd = new StringBuilder();
        stbTaskAllOfEnd.append("System.out.println(\"Total：\" + total + \" ").append(unit.getUnitStr()).append("\");").append(proxyClassName)
                .append(".running = true;} catch (Exception e) {throw new RuntimeException(e);}");
        ctMethodTaskAllOfGet.setBody("{" +
                "        try {\n" +
                stbTaskAllOfLong.toString() +
                stbTaskAllTotal.toString() +
                stbTaskAllOfEnd.toString() +
                "}");
        ctClassTaskAllOf.addMethod(ctMethodTaskAllOfGet);
        ctClassTaskAllOf.toClass();

        //execute方法
        stb.append(baseWorkerService.proxyBodyAsync(threads, testTimes, warmupTestTimes, taskAllOfClassName, ctMethodTaskNameList));

        CtMethod ctMethodExecute = new CtMethod(CtClass.voidType, "execute", new CtClass[0], ctClassProxy);
        ctMethodExecute.setBody("{" + stb.toString() + "}");
        ctClassProxy.addMethod(ctMethodExecute);


//        ctClassTaskAllOf.writeFile();
//        ctClassProxy.writeFile();

    }

    public void executeProxyObj() throws Exception {
        if (threads > 1) {
            makeClassAsync(clazz);
        } else {
            makeClass(clazz);
        }

        ClassPool classPool = ClassPool.getDefault();
        CtClass ctProxyClass = classPool.get(clazz.getPackage().getName() + ".$Light$Proxy$" + clazz.getSimpleName());
        Class<?> poxyClazz = ctProxyClass.toClass();
        Object proxyObj = poxyClazz.newInstance();
        Method method = poxyClazz.getDeclaredMethod("execute");
        method.invoke(proxyObj);
    }
}

