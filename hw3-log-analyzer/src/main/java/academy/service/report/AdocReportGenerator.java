package academy.service.report;

import academy.model.Statistics;

public class AdocReportGenerator extends BaseReportGenerator {

    @Override
    protected String generateContent(Statistics statistics) {
        StringBuilder sb = new StringBuilder();
        sb.append("Log Analysis Report\n\n");

        sb.append("Files:: ").append(String.join(", ", statistics.files())).append("\n");
        sb.append("Total Requests:: ").append(statistics.totalRequestsCount()).append("\n");
        sb.append("Average Response Size:: ")
                .append(statistics.responseSizeInBytes().average())
                .append(" bytes\n");
        sb.append("Max Response Size:: ")
                .append(statistics.responseSizeInBytes().max())
                .append(" bytes\n");
        sb.append("95th Percentile Response Size:: ")
                .append(statistics.responseSizeInBytes().p95())
                .append(" bytes\n\n");

        return sb.toString();
    }
}
