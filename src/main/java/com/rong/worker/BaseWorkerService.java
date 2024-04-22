package com.rong.worker;

import com.rong.pojo.TestTimeUnit;

import java.util.List;

public interface BaseWorkerService {
    void workV2() throws Exception;

    String taskBody(String methodName, TestTimeUnit unit);

    String proxyBody(List<String> baseWorkerNameCtClassList, long warmupTestTimes, long testTimes);

    String taskBodyAsync(String methodName);

    String proxyBodyAsync(int threads, long testTimes, long warmUpTimes, String ctClassTaskAllOf, List<String> workerClassName);

}
