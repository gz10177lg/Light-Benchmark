# Light-Benchmark

Latest Version：v1.0.3

这是一款简单、轻量级别的基准测试工具。

## 特点：

1. 使用简单，一个LightBuilder即可构建简单测试

2. 仅需引入一个依赖，在需要执行的方法地方加上@LightBenchmark注解即可

3. 支持启用异步线程执行任务，最后将数据归总
4. jar包大小仅14.5kb

## 使用案例demo：

1. 对Light-Benchmark项目使用maven进行打包。

2. 在项目引入maven依赖。

   ```xml
   <dependency>
     <groupId>com.Light-Benchmark</groupId>
     <artifactId>Light-Benchmark</artifactId>
     <version>1.0.3</version>
   </dependency>
   ```

3. 在需要测试的方法加上@LightBenchmark注解。

   1. 方法不限任意权限操作符，可以private、protected等。
   2. 方法不限任意返回类型，可以void、String等。
   3. **方法目前暂不支持有参形式**。

   ```java
   public class UserService {
       @LightBenchmark
       public void createUUID() {	
           String uuid = UUID.randomUUID().toString();
       }
     
       @LightBenchmark
       private String createUUID2() {	
           return UUID.randomUUID().toString();
       }
     
     /*  暂不支持有参方法，否则会报错。
         @LightBenchmark
         private String createUUID3(String name) {	
             return UUID.randomUUID().toString() + name;
         }
     */
   }
   ```

4. 构建启动方法，传入参数，并启动。

   ```java
   public static void main(String[] args) {
           LightBuilder.build()
                   .model(LightBuilderConstant.OPS)
                   .clazz(UserService.class)
                   .warmupTestTimes(5)
                   .testTimes(5)
                   .unit(TestTimeUnit.US)
                   .threads(4)
                   .startTest();
   }
   ```

5. 参数介绍：

   | 参数方法        | 说明           | 可选值                      | 值说明                     | 必填 |
      | --------------- | -------------- |--------------------------| -------------------------- | ---- |
   | model           | 测试模式       | LightBuilderConstant.OPS | 吞吐量（如ops/us）         | 是   |
   | clazz           | 测试类Class    | 自行传入                     |                            | 是   |
   | warmupTestTimes | 预热执行次数   | Long类型的值，默认值为5           | 进行5次预热执行            | 否   |
   | testTimes       | 正式执行次数   | Long类型的值，默认值为5           | 进行5次正式执行            | 否   |
   | unit            | 执行结果单位   | TestTimeUnit.SECOND      | 秒（ops/s）                | 否   |
   |                 |                | TestTimeUnit.MS          | 毫秒（ops/ms）             | 否   |
   |                 |                | TestTimeUnit.US（默认值）     | 微秒（ops/us）             | 否   |
   | threads         | 异步执行线程数 | 4                        | 一个任务同时由几个线程执行 | 否   |

6. 测试结果：

   1. 单线程

      ```txt
      预热阶段——【com.rong.test.model.UserService$createUUID】
      ========start========
      1.463 ops/us
      2.071 ops/us
      2.247 ops/us
      2.266 ops/us
      2.241 ops/us
      ========end========
      
      正式阶段——【com.rong.test.model.UserService$createUUID】
      ========start========
      2.243 ops/us
      2.242 ops/us
      2.237 ops/us
      2.219 ops/us
      2.242 ops/us
      ========end========
      ```

   2. 异步多线程（4线程）

      ```txt
      预热阶段——【com.rong.test.model.UserService$createUUID】线程数：4
      ========start========
      Total：1.31  ops/us
      Total：2.04  ops/us
      Total：2.12  ops/us
      Total：2.14  ops/us
      Total：2.08  ops/us
      ========end========
      正式阶段——【com.rong.test.model.UserService$createUUID】线程数：4
      ========start========
      Total：2.08  ops/us
      Total：2.10  ops/us
      Total：2.09  ops/us
      Total：2.09  ops/us
      Total：2.08  ops/us
      ========end========
      预热阶段——【com.rong.test.model.UserService$createSystemTime】线程数：4
      ========start========
      Total：1064.46  ops/us
      Total：1014.71  ops/us
      Total：1079.31  ops/us
      Total：1039.64  ops/us
      Total：995.41  ops/us
      ========end========
      正式阶段——【com.rong.test.model.UserService$createSystemTime】线程数：4
      ========start========
      Total：1090.53  ops/us
      Total：1015.10  ops/us
      Total：983.70  ops/us
      Total：1059.62  ops/us
      Total：1036.90  ops/us
      ========end========
      ```

## 注意事项：

1. 如果多次执行数据偏差较大，则可以在**预热执行次数**适当提高执行次数。譬如下情况：

   ```java
   LightBuilder.build()
       .model(LightBuilderConstant.OPS)
       .clazz(UserService.class)
       .warmupTestTimes(2)	//次数太低，可增加执行次数，譬如设置到 5 及以上。
       .testTimes(2)
       .unit(TestTimeUnit.US)
       .startTest();
   ```

   ```txt
   预热阶段——【com.rong.test.model.UserService$createUUID】
   ========start========
   1.481 ops/us
   2.083 ops/us
   ========end========
   ```

   Tips：原因是执行次数过低，JIT还没有介入，没有被标记为热点代码，导致执行效率不高。