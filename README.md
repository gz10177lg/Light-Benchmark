# Light-Benchmark

### 一款简单、轻量级别的基准测试工具

## 特点：

1. ### 使用简单，一个LightBuilder即可构建简单测试

2. ### 仅需引入一个依赖，在需要执行的方法地方加上@Light-Benchmark注解即可

3. ### jar包大小仅11.5kb

## 使用案例demo：

1. ### 引入maven依赖。

   ```xml
   <dependency>
     <groupId>com.Light-Benchmark</groupId>
     <artifactId>Light-Benchmark</artifactId>
     <version>1.0.0</version>
   </dependency>
   ```

2. ### 在需要测试的方法加上@Light-Benchmark注解。

   ```java
   @LightBenchmark
   public void getUser() {
     UUID.randomUUID().toString();
   }
   ```

3. ### 构建启动方法，传入参数，并启动（参数简易版）。

   ```java
   public static void main(String[] args) {
     LightBuilder.build()
       .model(LightBuilderConstant.OPS)
       .clazz(UserService.class)
       .startTest();
   }
   ```

4. ### 结果:

   ```txt
   ========getUser预热阶段========
   ========getUser正式开始========
   getUser:1.95ops/us
   ```

## 参数介绍：

### LightBuilder.*build*()生成的work采用建造者模式。

```java
LightBuilder.build()
        .model(LightBuilderConstant.OPS)// 指定模式为OPS，必填
        .clazz(UserService.class)// 指定测试方法所在类，必填
        .warmupTestTimes(300_000)//	预热次数,默认为500_000,可不填
        .testTimes(1_000_000)// 测试次数，默认为1_000_000,可不填
        .unit(TestTimeUnit.US)// 测试结果单位，秒、毫秒、微秒、纳秒
        .startTest();//开始测试
```

| 参数方法        | 说明            | 可选值                           | 值说明                        |
| --------------- | --------------- | -------------------------------- | ----------------------------- |
| model           | 指定测试模式    | LightBuilderConstant.OPS         | 测试微秒级别的OPS             |
|                 |                 | LightBuilderConstant.TIMES       | 测试执行testTimes次数所耗微秒 |
| clazz           | 指定测试类class | UserService.class                |                               |
| warmupTestTimes | 预热次数        | 建议不低于500_000次，即50万次    |                               |
| testTimes       | 正式测试次数    | 建议不低于1_000_000次，即100万次 |                               |
| unit            | 测试结果单位    | TestTimeUnit.SECOND              | 秒                            |
|                 |                 | TestTimeUnit.MS                  | 毫秒                          |
|                 |                 | TestTimeUnit.US                  | 微秒                          |




## 注意：

1. ### 为达到更准确的数据，暂且可以关闭其他不必要资源。

2. ### 测试方法需要以public修饰的无参方法。

3. ### 若测试有参方法，会引起错误。反例如下。

   ```java
   public void getUser(int id){
     // 逻辑..
   }
   ```