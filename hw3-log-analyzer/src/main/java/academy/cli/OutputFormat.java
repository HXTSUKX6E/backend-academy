package academy.cli;

public enum OutputFormat {
    JSON(".json"),
    MARKDOWN(".md"),
    ADOC(".adoc");

    private final String fileExtension;

    OutputFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
