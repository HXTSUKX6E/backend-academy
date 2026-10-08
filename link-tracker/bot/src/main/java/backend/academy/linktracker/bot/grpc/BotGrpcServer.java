package backend.academy.linktracker.bot.grpc;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotGrpcServer {

    public static final String SERVICE_NAME = "linktracker.BotUpdatesService";
    public static final String METHOD_NAME = "SendUpdate";

    private final UpdateNotifier notifier;
    private final GrpcProperties properties;
    private final ObjectMapper objectMapper;

    private Server server;

    @PostConstruct
    public void start() throws IOException {
        MethodDescriptor<StringValue, Empty> descriptor = descriptor();

        ServerServiceDefinition serviceDefinition = ServerServiceDefinition.builder(SERVICE_NAME)
                .addMethod(descriptor, ServerCalls.asyncUnaryCall(this::handleUpdate))
                .build();

        server = io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder.forPort(properties.getPort())
                .addService(serviceDefinition)
                .build()
                .start();

        log.atInfo().addKeyValue("port", properties.getPort()).log("Bot gRPC server started");
    }

    @PreDestroy
    public void shutdown() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void handleUpdate(StringValue request, StreamObserver<Empty> responseObserver) {
        try {
            LinkUpdate update = objectMapper.readValue(request.getValue(), LinkUpdate.class);
            notifier.notifyUpdate(update);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    public static MethodDescriptor<StringValue, Empty> descriptor() {
        return MethodDescriptor.<StringValue, Empty>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, METHOD_NAME))
                .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(Empty.getDefaultInstance()))
                .build();
    }
}
