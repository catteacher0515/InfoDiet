package com.pingyu.infodiet.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 推送队列配置
 */
@Configuration
public class PushQueueRabbitConfig {

    /**
     * 声明推送队列
     */
    @Bean
    public Queue infoDietPushQueue(InfoDietProperties infoDietProperties) {
        return new Queue(infoDietProperties.getPushQueueName(), true);
    }
}
