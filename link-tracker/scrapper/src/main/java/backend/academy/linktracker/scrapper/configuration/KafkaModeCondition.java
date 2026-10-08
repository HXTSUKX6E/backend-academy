package backend.academy.linktracker.scrapper.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class KafkaModeCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String protocol = context.getEnvironment().getProperty("app.bot.protocol", "kafka-outbox");
        return "kafka".equals(protocol) || "kafka-outbox".equals(protocol);
    }
}
