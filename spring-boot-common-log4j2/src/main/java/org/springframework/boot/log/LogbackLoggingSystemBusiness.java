package org.springframework.boot.log;

import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.*;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.log4j2.Log4J2LoggingSystem;

import java.nio.charset.Charset;

/**
 * @author zhouyi
 * @date 2021/5/11 15:23
 */
public class LogbackLoggingSystemBusiness extends Log4J2LoggingSystem {
    public LogbackLoggingSystemBusiness(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public void initialize(LoggingInitializationContext initializationContext, String configLocation, LogFile logFile) {
        super.initialize(initializationContext, configLocation, logFile);
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        createAppender(context, new LogAppender("rocketmq", "rocketmq_client", "RocketmqClient", Level.INFO));
    }

    private void createAppender(LoggerContext context, LogAppender logAppender) {
        final Configuration configuration = context.getConfiguration();

        // patternLayout
        PatternLayout.Builder patternLayoutBuilder = PatternLayout.newBuilder();
        patternLayoutBuilder.withCharset(logAppender.appenderCharset);
        patternLayoutBuilder.withPattern(logAppender.appenderPatternLayout);
        patternLayoutBuilder.withConfiguration(configuration);
        Layout<String> patternLayout = patternLayoutBuilder.build();

        // 文件大小超过10MB，就新建一个归档文件
        SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = SizeBasedTriggeringPolicy.createPolicy("10MB");
        sizeBasedTriggeringPolicy.start();

        // 文件个数超过5个，就会删除旧的归档文件
        DefaultRolloverStrategy.Builder strategyBuilder = DefaultRolloverStrategy.newBuilder();
        strategyBuilder.withMax("5");
        // ifFileName
        IfFileName ifFileName = IfFileName.createNameCondition("*.log.gz", "*.log.gz");
        // 归档文件的有效期为5d
        Duration age = Duration.parse("P5DT0H0M");
        // ifLastModified
        IfLastModified ifLastModified = IfLastModified.createAgeCondition(age);
        PathCondition[] pathConditions = new PathCondition[]{ifFileName, ifLastModified};
        // delete
        DeleteAction deleteAction = DeleteAction.createDeleteAction(logAppender.logHome + "/", false, 5, false, null, pathConditions, null, configuration);
        DeleteAction[] deleteActions = new DeleteAction[]{deleteAction};
        strategyBuilder.withCustomActions(deleteActions);
        DefaultRolloverStrategy defaultRolloverStrategy = strategyBuilder.build();

        // rollingFile
        RollingFileAppender.Builder<?> rollingFileAppenderBuilder = RollingFileAppender.newBuilder();
        rollingFileAppenderBuilder.setName(logAppender.appenderName);
        rollingFileAppenderBuilder.withFileName(logAppender.getAppenderFileName());
        rollingFileAppenderBuilder.withFilePattern(logAppender.getAppenderFilePattern());
        rollingFileAppenderBuilder.withAppend(true);
        rollingFileAppenderBuilder.setLayout(patternLayout);
        rollingFileAppenderBuilder.withPolicy(sizeBasedTriggeringPolicy);
        rollingFileAppenderBuilder.withStrategy(defaultRolloverStrategy);
        RollingFileAppender rollingFileAppender = rollingFileAppenderBuilder.build();
        rollingFileAppender.start();
        configuration.addAppender(rollingFileAppender);

        // appender-ref
        AppenderRef appenderRef = AppenderRef.createAppenderRef(logAppender.getAppenderName(), logAppender.getLoggerLevel(), null);
        AppenderRef[] appenderRefs = new AppenderRef[]{appenderRef};

        // logger
        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, logAppender.getLoggerLevel(), logAppender.getLoggerName(), "false", appenderRefs, null,
                configuration, null);
        loggerConfig.addAppender(rollingFileAppender, logAppender.loggerLevel, null);
        loggerConfig.start();

        configuration.addLogger(logAppender.getLoggerName(), loggerConfig);
        configuration.start();
        context.updateLoggers();
    }

    @Getter
    class LogAppender {
        private final String logHome = "./log";

        // appender
        private String appenderName;
        private String appenderFileName;
        private String appenderFilePattern;
        private String appenderPatternLayout = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n";
        private Charset appenderCharset = Charset.forName("UTF-8");

        // logger
        private String loggerName;
        private Level loggerLevel;

        public LogAppender(String appenderName, String appenderFileName, String loggerName, Level loggerLevel) {
            this.appenderName = appenderName;
            this.appenderFileName = this.logHome + "/" + appenderFileName + ".log";
            this.appenderFilePattern = this.logHome + "/" + appenderFileName + ".%d{yyyyMMdd}.%i.log.gz";
            this.loggerName = loggerName;
            this.loggerLevel = loggerLevel;
        }
    }
}

