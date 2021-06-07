# 通用日志模块

本示例基于 Spring Boot 2.4.5

## 配置与读取公共模块的文件

1. 在 src/main/resources 目录下，添加配置文件 log4j2.xml 或者 log4j2-spring.xml
   - 不需要在公共模块和业务模块的 application.properties 中添加任何配置项
   - 配置文件路径: src/main/resources/log4j2.xml
   - log4j2.xml 和 log4j2-spring.xml 的区别在于加载的顺序不一样，优先加载 log4j2.xml，在后续会详细介绍

2. 添加 log4j2 依赖
   - 全局排除 logback 依赖
   - 添加 log4j2 依赖

## 日志配置参数参考

[官方日志格式的参数描述](https://logging.apache.org/log4j/2.x/manual/layouts.html 'apache-log4j')

## log4j2 配置文件模板

[log4j2-template.xml](./src/main/resources/log4j2-template.xml 'log4j2-template.xml')

## 自定义 log4j2 配置文件

[log4j2.xml](./src/main/resources/log4j2.xml 'log4j2.xml')

## 自定义 log4j2 代码

1. [LogbackLoggingSystemBusiness](./src/main/java/org/springframework/boot/log/LogbackLoggingSystemBusiness.java 'LogbackLoggingSystemBusiness.java')

2. 在业务的启动类里添加如下环境变量
   ```java
   System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LogbackLoggingSystemBusiness.class.getName());
   ```

## log4j2 加载配置的流程

核心类有 AbstractLoggingSystem 和 Log4J2LoggingSystem

### AbstractLoggingSystem

```java
// 源码路径: org\springframework\boot\spring-boot\2.4.5\spring-boot-2.4.5.jar!\org\springframework\boot\logging\AbstractLoggingSystem.class

/**
 * 初始化配置信息
 * 
 * 初始化的流程:
 * 1. 先查找默认的配置文件
 * 2. 如果默认的配置文件不存在，就查找 spring 的配置文件
 * 3. 如果默认的配置文件和  spring 的配置文件都不存在，就构造一个默认的配置信息
 *
 * @param initializationContext
 * @param logFile
 */
private void initializeWithConventions(LoggingInitializationContext initializationContext, LogFile logFile) {
    // 1. 查找默认的配置文件
    String config = this.getSelfInitializationConfig();
    // 2. 存在默认的配置文件，就用默认的配置文件初始化日志配置信息
    if (config != null && logFile == null) {
        this.reinitialize(initializationContext);
    } else {
        // 3. 不存在默认的配置文件，就查找 spring 的配置文件
        if (config == null) {
            config = this.getSpringInitializationConfig();
        }

        if (config != null) { // 4. 存在 spring 的配置文件，就用 spring 的配置文件初始化日志配置信息
            this.loadConfiguration(initializationContext, config, logFile);
        } else { // 5. 不存在 spring 的配置文件，就构造一个默认的配置信息
            this.loadDefaults(initializationContext, logFile);
        }
    }
}

/**
 * 查找默认的物理配置文件
 *
 * @return
 */
protected String getSelfInitializationConfig() {
    return this.findConfig(this.getStandardConfigLocations());
}

/**
 * 查找 spring 的物理配置文件
 *
 * @return
 */
protected String getSpringInitializationConfig() {
    return this.findConfig(this.getSpringConfigLocations());
}

/**
 * 遍历常量列表里的配置文件是否存在相应的物理文件
 *
 * @param locations 常量列表里的配置文件
 * @return
 */
private String findConfig(String[] locations) {
    String[] var2 = locations;
    int var3 = locations.length;

    for (int var4 = 0; var4 < var3; ++var4) {
        String location = var2[var4];
        ClassPathResource resource = new ClassPathResource(location, this.classLoader);
        if (resource.exists()) {
            return "classpath:" + location;
        }
    }

    return null;
}

/**
 * 初始化 spring 的配置文件列表
 *
 * @return
 */
protected String[] getSpringConfigLocations() {
    String[] locations = this.getStandardConfigLocations();

    for (int i = 0; i < locations.length; ++i) {
        String extension = StringUtils.getFilenameExtension(locations[i]);
        locations[i] = locations[i].substring(0, locations[i].length() - extension.length() - 1) + "-spring." + extension;
    }

    return locations;
}
```

### Log4J2LoggingSystem

```java
// 源码路径: org\springframework\boot\spring-boot\2.4.5\spring-boot-2.4.5.jar!\org\springframework\boot\logging\log4j2\Log4J2LoggingSystem.class

/**
 * 初始化默认的配置文件列表
 *
 * 配置文件列表:
 * 1. log4j2-test.properties
 * 2. log4j2-test.json
 * 3. log4j2-test.jsn
 * 4. log4j2-test.xml
 * 5. log4j2.properties
 * 6. log4j2.json
 * 7. log4j2.jsn
 * 8. log4j2.xml
 *
 * @return
 */
protected String[] getStandardConfigLocations() {
    return this.getCurrentlySupportedConfigLocations();
}
```
