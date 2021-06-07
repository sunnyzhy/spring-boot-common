package org.springframework.boot.test.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;

/**
 * 消费者服务
 */
@Service
@Slf4j
public class RocketMqConsumer {
    private final RocketMqConfig config;
    private final RocketMqListener rocketMqListener;
    private DefaultMQPushConsumer consumer;

    public RocketMqConsumer(RocketMqConfig config, RocketMqListener rocketMqListener) {
        this.config = config;
        this.rocketMqListener = rocketMqListener;
    }

    /**
     * 开始订阅消息
     */
    @PostConstruct
    public void init() {
        consumer = new DefaultMQPushConsumer(config.consumerGroup);
        consumer.setNamesrvAddr(config.namesrvAddr);
        consumer.setConsumeMessageBatchMaxSize(128);
        consumer.setConsumeThreadMin(config.threadMin);
        consumer.setConsumeThreadMax(config.threadMax);
        consumer.setInstanceName(UUID.randomUUID().toString());
        consumer.registerMessageListener(rocketMqListener);
        try {
            consumer.subscribe(config.consumerDevTopic, config.consumerTags);
            consumer.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 关闭并释放消费者实例
     */
    @PreDestroy
    public void destroy() {
        consumer.shutdown();
    }
}
