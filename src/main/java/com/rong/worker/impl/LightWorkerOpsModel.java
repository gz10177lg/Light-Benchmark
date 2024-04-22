package com.rong.worker.impl;

import com.rong.builder.v2.LightBuilderJavassistV2;
import com.rong.pojo.TestTimeUnit;
import com.rong.worker.BaseWorkerService;

import java.util.List;

public class LightWorkerOpsModel implements BaseWorkerService {
    private LightBuilderJavassistV2 service;

    public LightWorkerOpsModel(Class<?> clazz, long warmupTestTimes, long testTimes, TestTimeUnit unit, int threads) {
        this.service = new LightBuilderJavassistV2(clazz, warmupTestTimes, testTimes, unit, threads, this);
    }

    @Override
    public void workV2() throws Exception {
        this.service.executeProxyObj();
    }

    @Override
    public String taskBody(String methodName, TestTimeUnit unit) {
        StringBuilder stb = new StringBuilder("long count = 0;")
                .append("while (!Thread.currentThread().isInterrupted()) {")
                .append(" worker.").append(methodName).append("();")
                .append("count++;").append("}")
                .append("System.out.println(new java.math.BigDecimal(count / ").append(unit.getUnit()).append(").setScale(3, java.math.RoundingMode.HALF_UP) + \"").append(unit.getUnitStr()).append("\");");
        return stb.toString();
    }

    @Override
    public String proxyBody(List<String> baseWorkerNameCtClassList, long warmupTestTimes, long testTimes) {
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < baseWorkerNameCtClassList.size(); i++) {
            stb.append("System.out.println(\"预热阶段——【").append(baseWorkerNameCtClassList.get(i).replace("$BaseWorkerServiceTask$", "")).append("】\");\n")
                    .append("System.out.println(\"========start========\");\n")
                    .append("for(int i=0;i<" + warmupTestTimes + ";i++) {\n")
                    .append("Thread t").append(i)
                    .append(" = new Thread(new ")
                    .append(baseWorkerNameCtClassList.get(i))
                    .append("(this));\n")
                    .append("t").append(i).append(".start();")
                    .append("try {Thread.sleep(1000l);} catch (InterruptedException e) {throw new RuntimeException(e);}\n")
                    .append("t").append(i).append(".interrupt();}\n")
                    .append("Thread.sleep(10l);")
                    .append("System.out.println(\"========end========\");\n\n")
                    .append("System.out.println();");


            stb.append("System.out.println(\"正式阶段——【").append(baseWorkerNameCtClassList.get(i).replace("$BaseWorkerServiceTask$", "")).append("】\");\n")
                    .append("System.out.println(\"========start========\");\n")
                    .append("for(int i=0;i<" + testTimes + ";i++) {\n")
                    .append("Thread t").append(i)
                    .append(" = new Thread(new ")
                    .append(baseWorkerNameCtClassList.get(i))
                    .append("(this));\n")
                    .append("t").append(i).append(".start();")
                    .append("try {Thread.sleep(1000l);} catch (InterruptedException e) {throw new RuntimeException(e);}\n")
                    .append("t").append(i).append(".interrupt();}\n")
                    .append("Thread.sleep(10l);")
                    .append("System.out.println(\"========end========\");\n\n")
                    .append("System.out.println();");
        }
        return stb.toString();
    }

    @Override
    public String taskBodyAsync(String methodName) {
        StringBuilder stb = new StringBuilder("long count = 0;");
        stb.append("while (worker.running) {")
                .append("worker.")
                .append(methodName)
                .append("();count++;").append("}")
                .append("return Long.valueOf(count);");
        return stb.toString();
    }

    @Override
    public String proxyBodyAsync(int threads, long testTimes, long warmUpTimes, String taskAllOfClassName, List<String> workerClassName) {
        StringBuilder stbBody = new StringBuilder("java.util.concurrent.ThreadPoolExecutor pool = new java.util.concurrent.ThreadPoolExecutor(")
                .append(threads).append(",").append(threads).append(", 30l, java.util.concurrent.TimeUnit.SECONDS, new java.util.concurrent.ArrayBlockingQueue(").append(threads).append("));");
        for (int i = 0; i < workerClassName.size(); i++) {
            StringBuilder stbFuture = new StringBuilder();
            StringBuilder stbAllOf = new StringBuilder();
            for (int j = 0; j < threads; j++) {
                stbFuture.append("java.util.concurrent.CompletableFuture future").append(j).append(" = java.util.concurrent.CompletableFuture.supplyAsync(new ").append(workerClassName.get(i)).append("(this), pool);\n");
                stbAllOf.append("future").append(j);
                if (j != threads - 1) {
                    stbAllOf.append(",");
                }
            }
            stbBody.append("System.out.println(\"预热阶段——【")
                    .append(workerClassName.get(i).replace("$BaseWorkerServiceTask$", ""))
                    .append("】线程数：").append(threads).append("\");").append("System.out.println(\"========start========\");")
                    .append("for (int i = 0; i < ").append(warmUpTimes).append("; i++){")
                    .append(stbFuture.toString())
                    .append("Thread.sleep(1000l);").append("running = false;")
                    .append("java.util.concurrent.CompletableFuture[] completableFutures = new java.util.concurrent.CompletableFuture[]{")
                    .append(stbAllOf.toString())
                    .append("};")
                    .append("java.util.concurrent.CompletableFuture.allOf(completableFutures).thenRun(new ")
                    .append(taskAllOfClassName)
                    .append("(").append(stbAllOf.toString()).append("));").append("}")
                    .append("System.out.println(\"========end========\");");

            //正式阶段
            StringBuilder stbFuture2 = new StringBuilder();
            StringBuilder stbAllOf2 = new StringBuilder();
            for (int j = 0; j < threads; j++) {
                stbFuture2.append("java.util.concurrent.CompletableFuture future").append(j).append(" = java.util.concurrent.CompletableFuture.supplyAsync(new ").append(workerClassName.get(i)).append("(this), pool);\n");
                stbAllOf2.append("future").append(j);
                if (j != threads - 1) {
                    stbAllOf2.append(",");
                }
            }
            stbBody.append("System.out.println(\"正式阶段——【")
                    .append(workerClassName.get(i).replace("$BaseWorkerServiceTask$", ""))
                    .append("】线程数：").append(threads).append("\");").append("System.out.println(\"========start========\");")
                    .append("for (int i = 0; i < ").append(warmUpTimes).append("; i++){")
                    .append(stbFuture2.toString())
                    .append("Thread.sleep(1000l);").append("running = false;")
                    .append("java.util.concurrent.CompletableFuture[] completableFutures = new java.util.concurrent.CompletableFuture[]{")
                    .append(stbAllOf2.toString())
                    .append("};")
                    .append("java.util.concurrent.CompletableFuture.allOf(completableFutures).thenRun(new ")
                    .append(taskAllOfClassName)
                    .append("(").append(stbAllOf2.toString()).append("));").append("}")
                    .append("System.out.println(\"========end========\");");


        }
        return stbBody.append("pool.shutdown();").toString();
    }
}
