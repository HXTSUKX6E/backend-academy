package backend.academy.linktracker.bot.configuration;

import com.example.notification.ProcessedUpdateEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfiguration {

    @Value("${app.kafka.consumer.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.kafka.consumer.backoff-interval:1000}")
    private long backoffInterval;

    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        var backoff = new FixedBackOff(backoffInterval, maxAttempts - 1L);
        var handler = new DefaultErrorHandler(recoverer, backoff);
        handler.addNotRetryableExceptions(DeserializationException.class);
        return handler;
    }

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ConcurrentKafkaListenerContainerFactory<String, ProcessedUpdateEvent> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory, CommonErrorHandler kafkaErrorHandler) {
        var factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
