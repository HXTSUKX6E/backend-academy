package academy;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "academy.ClassInspector", version = "1.0", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    @Option(
            names = {"-c", "--class"},
            required = true,
            description = "Fully qualified class name")
    private String className;

    @Option(
            names = {"-f", "--format"},
            defaultValue = "TEXT",
            description = "Output format: TEXT or JSON")
    private String format;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            String fmt = format == null ? "TEXT" : format.toUpperCase();
            if (!fmt.equals("TEXT") && !fmt.equals("JSON")) {
                System.err.println("Unsupported format: " + format);
                System.exit(2);
                return;
            }

            Class<?> clazz = Class.forName(className);
            String result = ClassInspector.inspect(clazz, fmt);
            System.out.println(result);
            System.exit(0);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + className);
            System.exit(2);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
