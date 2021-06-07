package org.springframework.boot.test.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;

/**
 * 生产者服务
 */
@Service
@Slf4j
public class RocketMqProducer {
    private final RocketMqConfig config;
    private DefaultMQProducer producer;

    public RocketMqProducer(RocketMqConfig config) {
        this.config = config;
    }

    /**
     * 开启生产者实例
     */
    @PostConstruct
    public void init() {
        producer = new DefaultMQProducer(config.producerGroup);
        producer.setNamesrvAddr(config.namesrvAddr);
        producer.setInstanceName(UUID.randomUUID().toString());
//        producer.setVipChannelEnabled(false);
        try {
            producer.start();
        } catch (MQClientException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 关闭并释放生产者实例
     */
    @PreDestroy
    public void destroy() {
        producer.shutdown();
    }

}


