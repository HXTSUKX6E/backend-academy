package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.bot", name = "protocol", havingValue = "grpc")
public class GrpcBotClient implements BotNotifierClient {

    private static final String SERVICE_NAME = "linktracker.BotUpdatesService";
    private static final String METHOD_NAME = "SendUpdate";

    private final ManagedChannel botGrpcChannel;
    private final ObjectMapper objectMapper;

    @Override
    public void sendUpdate(LinkUpdateRequest request) {
        MethodDescriptor<StringValue, Empty> descriptor = MethodDescriptor.<StringValue, Empty>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, METHOD_NAME))
                .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(Empty.getDefaultInstance()))
                .build();
        try {
            StringValue payload = StringValue.of(objectMapper.writeValueAsString(request));
            ClientCalls.blockingUnaryCall(botGrpcChannel, descriptor, io.grpc.CallOptions.DEFAULT, payload);
            log.atInfo()
                    .addKeyValue("url", request.url())
                    .addKeyValue("chatCount", request.tgChatIds().size())
                    .log("Update sent to bot via gRPC");
        } catch (StatusRuntimeException | com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to send gRPC update", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        botGrpcChannel.shutdown();
    }
}
