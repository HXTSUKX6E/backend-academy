package backend.academy.linktracker.ai.configuration;

import com.example.notification.LinkUpdateEvent;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class KafkaConsumerConfiguration {

    private final AiAgentProperties properties;

    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        var consumer = properties.kafka().consumer();
        var backoff = new FixedBackOff(consumer.backoffInterval(), consumer.maxAttempts() - 1L);
        var handler = new DefaultErrorHandler(recoverer, backoff);
        handler.addNotRetryableExceptions(DeserializationException.class);
        return handler;
    }

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ConcurrentKafkaListenerContainerFactory<String, LinkUpdateEvent> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory, CommonErrorHandler kafkaErrorHandler) {
        var factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
