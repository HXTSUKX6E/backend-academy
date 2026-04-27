package academy.cli;

import picocli.CommandLine;

public class OutputFormatConverter implements CommandLine.ITypeConverter<OutputFormat> {
    @Override
    public OutputFormat convert(String value) throws Exception {
        try {
            return OutputFormat.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CommandLine.TypeConversionException(
                    "Invalid value for option '--format': expected one of [JSON, MARKDOWN, ADOC] but was '" + value
                            + "'");
        }
    }
}
