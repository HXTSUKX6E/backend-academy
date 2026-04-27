package academy.model;

import java.time.LocalDateTime;

public record LogEntry(
        String remoteAddr,
        String remoteUser,
        LocalDateTime timeLocal,
        String request,
        int status,
        long bodyBytesSent,
        String httpReferer,
        String httpUserAgent,
        String resource,
        String protocol) {

    public LogEntry {
        if (resource == null) resource = "";
        if (protocol == null) protocol = "";

        if (request != null && !request.isBlank()) {
            String[] parts = request.split("\\s+", 3);
            if (resource.isBlank()) {
                resource = parts.length >= 2 ? parts[1] : "";
            }
            if (protocol.isBlank()) {
                protocol = parts.length >= 3 ? parts[2] : "";
            }
        }
    }

    public String getProtocol() {
        return protocol;
    }
}
