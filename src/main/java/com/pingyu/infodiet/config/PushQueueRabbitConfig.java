package com.pingyu.infodiet.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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

    /**
     * 统一使用 JSON 消息转换
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
