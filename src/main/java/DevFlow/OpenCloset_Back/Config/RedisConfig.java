package DevFlow.OpenCloset_Back.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RedisConfig {

    @Bean
    public ThreadPoolTaskExecutor chatRedisMessageExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("chat-redis-message-");
        return executor;
    }

    @Bean(destroyMethod = "close")
    public SimpleAsyncTaskExecutor chatRedisSubscriptionExecutor() {
        return new SimpleAsyncTaskExecutor("chat-redis-subscription-");
    }

    /**
     * Redis Pub/Sub 리스너 컨테이너
     * 채팅 메시지를 Redis 채널에서 수신하기 위한 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory, RedisSubscriber subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // Keep Redis callback delivery ordered; subscription work must use a separate executor.
        container.setTaskExecutor(chatRedisMessageExecutor());
        container.setSubscriptionExecutor(chatRedisSubscriptionExecutor());
        container.addMessageListener(subscriber, new PatternTopic("chat:room:*"));
        return container;
    }
}
