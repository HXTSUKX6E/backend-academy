package academy.service.report;

import academy.model.Statistics;
import academy.model.Statistics.ResourceStat;
import academy.model.Statistics.ResponseCodeStat;

public class MarkdownReportGenerator extends BaseReportGenerator {

    @Override
    protected String generateContent(Statistics statistics) {
        StringBuilder sb = new StringBuilder();

        sb.append("#### Информация\n\n");
        sb.append("| Метрика | Значение |\n");
        sb.append("|:--------|---------:|\n");
        sb.append("| Файл(-ы) | `")
                .append(String.join(", ", statistics.files()))
                .append("` |\n");
        sb.append("| Количество запросов | ")
                .append(formatNumber(statistics.totalRequestsCount()))
                .append(" |\n");
        sb.append("| Средний размер ответа | ")
                .append(statistics.responseSizeInBytes().average())
                .append("b |\n");
        sb.append("| Максимальный размер ответа | ")
                .append(statistics.responseSizeInBytes().max())
                .append("b |\n");
        sb.append("| 95p размера ответа | ")
                .append(statistics.responseSizeInBytes().p95())
                .append("b |\n");
        sb.append("\n");

        sb.append("#### Запрашиваемые ресурсы\n\n");
        sb.append("| Ресурс | Количество |\n");
        sb.append("|:-------|-----------:|\n");
        for (ResourceStat resource : statistics.resources()) {
            sb.append("| `")
                    .append(resource.resource())
                    .append("` | ")
                    .append(formatNumber(resource.totalRequestsCount()))
                    .append(" |\n");
        }
        sb.append("\n");

        sb.append("#### Коды ответа\n\n");
        sb.append("| Код | Имя | Количество |\n");
        sb.append("|:---:|:----|-----------:|\n");
        for (ResponseCodeStat code : statistics.responseCodes()) {
            sb.append("| ")
                    .append(code.code())
                    .append(" | ")
                    .append(getHttpStatusName(code.code()))
                    .append(" | ")
                    .append(formatNumber(code.totalResponsesCount()))
                    .append(" |\n");
        }

        return sb.toString();
    }
}
