package com.rong.worker.impl;

import com.rong.builder.v2.LightBuilderJavassistV2;
import com.rong.pojo.TestTimeUnit;
import com.rong.worker.BaseWorkerService;

import java.util.List;

public class LightWorkerOpsModel implements BaseWorkerService {
    private LightBuilderJavassistV2 service;

    public LightWorkerOpsModel(Class<?> clazz, long warmupTestTimes, long testTimes, TestTimeUnit unit) {
        this.service = new LightBuilderJavassistV2(clazz, warmupTestTimes, testTimes, unit, this);
    }

    @Override
    public void workV2() throws Exception {
        this.service.executeProxyObj();
    }

    @Override
    public String taskBody(String methodName, TestTimeUnit unit) {
        String body = "        long count = 0;\n" +
                "        while (!Thread.currentThread().isInterrupted()) {\n" +
                "        worker." + methodName + "();" +
                "            count++;\n" +
                "        }\n" +
                "System.out.println(new java.math.BigDecimal(count / " + unit.getUnit() + ").setScale(3, java.math.RoundingMode.HALF_UP) + \"" + unit.getUnitStr() + "\");";
        return body;
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

}
