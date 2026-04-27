package academy.model;

import java.util.List;

public record Statistics(
        List<String> files,
        int totalRequestsCount,
        ResponseSize responseSizeInBytes,
        List<ResourceStat> resources,
        List<ResponseCodeStat> responseCodes,
        List<DateStat> requestsPerDate,
        List<String> uniqueProtocols) {
    public record ResponseSize(double average, double max, double p95) {}

    public record ResourceStat(String resource, int totalRequestsCount) {}

    public record ResponseCodeStat(int code, int totalResponsesCount) {}

    public record DateStat(String date, String weekday, int totalRequestsCount, double totalRequestsPercentage) {}
}
