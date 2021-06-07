# 日志测试
## log4j2测试
1. 添加依赖
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-common-log4j2</artifactId>
       <version>0.0.1</version>
   </dependency>
   ```

2. 启动类添加环境变量
   ```java
   System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LogbackLoggingSystemBusiness.class.getName());
   ```
## logback测试
1. 添加依赖
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-common-logback</artifactId>
       <version>0.0.1</version>
   </dependency>
   ```

2. 启动类添加环境变量
   ```java
   System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LogbackLoggingSystemBusiness.class.getName());
   ```
