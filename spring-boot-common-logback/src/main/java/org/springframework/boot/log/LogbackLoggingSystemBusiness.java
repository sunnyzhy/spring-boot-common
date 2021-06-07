package org.springframework.boot.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;

import java.nio.charset.Charset;

/**
 * @author zhouyi
 * @date 2021/5/11 15:23
 */
public class LogbackLoggingSystemBusiness extends LogbackLoggingSystem {
    public LogbackLoggingSystemBusiness(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public void initialize(LoggingInitializationContext initializationContext, String configLocation, LogFile logFile) {
        super.initialize(initializationContext, configLocation, logFile);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//        context.reset();
//        // 初始化 ConsoleAppender，注意 logFile 为 null
//        super.loadDefaults(initializationContext, logFile);
//        createAppender(context, new LogAppender("file", "run", Logger.ROOT_LOGGER_NAME, Level.INFO));
        createAppender(context, new LogAppender("rocketmq", "rocketmq_client", "RocketmqClient", Level.INFO));
    }

    private void createAppender(LoggerContext context, LogAppender logAppender) {
        Logger logger = context.getLogger(logAppender.getLoggerName());
        logger.setAdditive(false);
        logger.setLevel(logAppender.getLoggerLevel());

        RollingFileAppender appender = new RollingFileAppender();
        appender.setName(logAppender.getAppenderName());
        appender.setFile(logAppender.getAppenderFileName());
        appender.setAppend(true);
        appender.setPrudent(false);
        appender.setContext(context);

        SizeAndTimeBasedRollingPolicy policy = new SizeAndTimeBasedRollingPolicy();
        // 归档日志的文件名
        policy.setFileNamePattern(logAppender.getAppenderFilePattern());
        // 日志文件最大尺寸
        policy.setMaxFileSize(FileSize.valueOf("10MB"));
        // 日志文件保留天数
        policy.setMaxHistory(5);
        // 超过50M就清理日志
        policy.setTotalSizeCap(FileSize.valueOf("50MB"));
        policy.setParent(appender);
        policy.setContext(context);
        policy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setCharset(logAppender.appenderCharset);
        encoder.setPattern(logAppender.appenderPatternLayout);
        encoder.setContext(context);
        encoder.start();

        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();

        logger.addAppender(appender);
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
