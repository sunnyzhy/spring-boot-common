# 通用日志模块

本示例基于 Spring Boot 2.4.5

## 配置与读取公共模块的文件

1. 在 src/main/resources 目录下，添加配置文件 logback.xml 或者 logback-spring.xml
   - 不需要在公共模块和业务模块的 application.properties 中添加任何配置项
   - 配置文件路径: src/main/resources/logback.xml
   - logback.xml 和 logback-spring.xml 的区别在于加载的顺序不一样，优先加载 logback.xml，在后续会详细介绍

## 日志配置参数参考

[官方日志格式的参数描述](https://logging.apache.org/log4j/2.x/manual/layouts.html 'apache-log4j')

## logback 配置文件模板

[logback-template.xml](./src/main/resources/logback-template.xml 'logback-template.xml')

## 自定义 logback 配置文件

[logback.xml](./src/main/resources/logback.xml 'logback.xml')

## 自定义 logback 代码

1. [LogbackLoggingSystemBusiness](./src/main/java/org/springframework/boot/log/LogbackLoggingSystemBusiness.java 'LogbackLoggingSystemBusiness.java')

2. 在业务的启动类里添加如下环境变量
   ```java
   System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LogbackLoggingSystemBusiness.class.getName());
   ```

## logback 加载配置的流程

核心类有 AbstractLoggingSystem 和 LogbackLoggingSystem

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
 * <p>
 * 遍历默认的配置文件的流程:
 * 1. 查找 logback-test.groovy 文件
 * 2. 查找 logback-test.xml 文件
 * 3. 查找 logback.groovy 文件
 * 4. 查找 logback-template.xml 文件
 * <p>
 * 遍历 spring 的配置文件的流程:
 * 1. 查找 logback-test-spring.groovy 文件
 * 2. 查找 logback-test-spring.xml 文件
 * 3. 查找 logback-spring.groovy 文件
 * 4. 查找 logback-spring.xml 文件
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

### LogbackLoggingSystem

```java
// 源码路径: org\springframework\boot\spring-boot\2.4.5\spring-boot-2.4.5.jar!\org\springframework\boot\logging\logback\LogbackLoggingSystem.class

/**
 * 初始化默认的配置文件列表
 *
 * 配置文件列表: 
 * 1. logback-test.groovy
 * 2. logback-test.xml
 * 3. logback.groovy
 * 4. logback.xml
 *
 * @return
 */
protected String[] getStandardConfigLocations() {
    return new String[]{"logback-test.groovy", "logback-test.xml", "logback.groovy", "logback.xml"};
}
```

### DefaultLogbackConfiguration

```java
// 源码路径: org\springframework\boot\spring-boot\2.4.5\spring-boot-2.4.5.jar!\org\springframework\boot\logging\logback\DefaultLogbackConfiguration.class

private void defaults(LogbackConfigurator config) {
    // 控制台的日志格式和颜色渲染
    config.conversionRule("clr", ColorConverter.class);
    config.conversionRule("wex", WhitespaceThrowableProxyConverter.class);
    config.conversionRule("wEx", ExtendedWhitespaceThrowableProxyConverter.class);
    config.getContext().putProperty("CONSOLE_LOG_PATTERN", this.resolve(config, "${CONSOLE_LOG_PATTERN:-%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}"));
    config.getContext().putProperty("CONSOLE_LOG_CHARSET", this.resolve(config, "${CONSOLE_LOG_CHARSET:-default}"));

    // 日志的日志格式
    config.getContext().putProperty("FILE_LOG_PATTERN", this.resolve(config, "${FILE_LOG_PATTERN:-%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}} ${LOG_LEVEL_PATTERN:-%5p} ${PID:- } --- [%t] %-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}"));
    config.getContext().putProperty("FILE_LOG_CHARSET", this.resolve(config, "${FILE_LOG_CHARSET:-default}"));
    // ... ...
}
```
