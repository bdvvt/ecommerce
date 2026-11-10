package org.example.notificationservice.configs;

import org.example.notificationservice.service.INotificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    public static final String NOTIFICATION_CHANNEL = "notification_channel";

    @Bean
    public MessageListenerAdapter messageListenerAdapter(INotificationService subscriber) {
        return new MessageListenerAdapter(subscriber, "listenerAdapter");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(NOTIFICATION_CHANNEL));
        return container;
    }
}
