package academy.config;

import academy.AppConfig;
import academy.utils.AffineParser;
import academy.utils.FunctionParser;

public final class CliArgsParser {

    private CliArgsParser() {}

    public static void parse(AppConfig config, String[] args) {

        for (int i = 0; i < args.length; i++) {

            String arg = args[i];

            if (arg.startsWith("-D")) {
                continue;
            }

            switch (arg) {
                case "--width":
                case "-w":
                    config.width = Integer.parseInt(args[++i]);
                    break;

                case "--height":
                case "-h":
                    config.height = Integer.parseInt(args[++i]);
                    break;

                case "--iteration-count":
                case "-i":
                    config.iterationCount = Long.parseLong(args[++i]);
                    break;

                case "--threads":
                case "-t":
                    config.threads = Integer.parseInt(args[++i]);
                    break;

                case "--seed":
                    config.seed = Long.parseLong(args[++i]);
                    break;

                case "--output-path":
                case "-o":
                    config.outputPath = args[++i];
                    break;

                case "--gamma-correction":
                case "-g":
                    config.gammaCorrection = true;
                    break;

                case "--gamma":
                    config.gamma = Double.parseDouble(args[++i]);
                    break;

                case "--symmetry-level":
                case "-s":
                    config.symmetryLevel = Math.max(1, Integer.parseInt(args[++i]));
                    break;

                case "--functions":
                case "-f":
                    config.transformations = FunctionParser.parse(args[++i], config.seed);
                    break;

                case "--affine-params":
                case "-ap":
                    config.affineTransforms = AffineParser.parse(args[++i]);
                    break;

                case "--config":
                    i++;
                    break;

                default:
                    throw new IllegalArgumentException("Unknown CLI option: " + arg);
            }
        }
    }
}
