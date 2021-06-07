package org.springframework.boot.test.mq;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 应用程序配置文件里的基本配置信息
 */
@Configuration
public class RocketMqConfig {
    @Value("${rocketmq.nameserver}")
    public String namesrvAddr;

    @Value("${rocketmq.consumer.groupname:st-alarm}")
    public String consumerGroup;

    @Value("${rocketmq.consumer.dev.topic:comm-dev-msg}")
    public String consumerDevTopic;

    @Value("${rocketmq.consumer.tags:*}")
    public String consumerTags;


    @Value("${rocketmq.producer.groupname:st-alarm-producer}")
    public String producerGroup;

    @Value("${rocketmq.producer.notify.topic:st-alarm-notify}")
    public String producerNotifyTopic;

    @Value("${rocketmq.consumer.threadmin:8}")
    public Integer threadMin;

    @Value("${rocketmq.consumer.threadmax:16}")
    public Integer threadMax;
}
