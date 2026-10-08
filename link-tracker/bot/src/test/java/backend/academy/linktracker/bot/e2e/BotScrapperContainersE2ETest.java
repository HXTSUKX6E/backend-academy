package backend.academy.linktracker.bot.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BotScrapperContainersE2ETest {

    private static final Path PROJECT_ROOT = Path.of("..").toAbsolutePath().normalize();

    private static final String BOT_IMAGE_TAG = "link-tracker-bot-e2e:latest";
    private static final String SCRAPPER_IMAGE_TAG = "link-tracker-scrapper-e2e:latest";

    private Network network;
    private GenericContainer<?> bot;
    private GenericContainer<?> scrapper;
    private PostgreSQLContainer postgres;

    @BeforeAll
    void setUp() throws Exception {
        buildImage("bot/Dockerfile", BOT_IMAGE_TAG);
        buildImage("scrapper/Dockerfile", SCRAPPER_IMAGE_TAG);

        network = Network.newNetwork();

        postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                .withNetwork(network)
                .withNetworkAliases("postgres")
                .withDatabaseName("scrapper")
                .withUsername("postgres")
                .withPassword("postgres")
                .withInitScript("e2e-scrapper-schema.sql");

        scrapper = new GenericContainer<>(DockerImageName.parse(SCRAPPER_IMAGE_TAG))
                .withImagePullPolicy(imageName -> false)
                .withNetwork(network)
                .withNetworkAliases("scrapper")
                .withExposedPorts(8081)
                .withEnv("BOT_BASE_URL", "http://bot:8080")
                .withEnv("BOT_PROTOCOL", "rest")
                .withEnv("STACKOVERFLOW_KEY", "test")
                .withEnv("STACKOVERFLOW_ACCESS_KEY", "test")
                .withEnv("GITHUB_TOKEN", "test")
                .withEnv("SCRAPPER_DB_URL", "jdbc:postgresql://postgres:5432/scrapper")
                .withEnv("SCRAPPER_DB_USERNAME", "postgres")
                .withEnv("SCRAPPER_DB_PASSWORD", "postgres")
                .waitingFor(
                        Wait.forHttp("/actuator/health").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(8)));

        bot = new GenericContainer<>(DockerImageName.parse(BOT_IMAGE_TAG))
                .withImagePullPolicy(imageName -> false)
                .withNetwork(network)
                .withNetworkAliases("bot")
                .withExposedPorts(8080)
                .withEnv("TELEGRAM_TOKEN", "dummy-token")
                .withEnv("APP_TELEGRAM_POLLING_ENABLED", "false")
                .withEnv("SCRAPPER_BASE_URL", "http://scrapper:8081")
                .waitingFor(
                        Wait.forHttp("/actuator/health").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(8)));

        postgres.start();
        scrapper.start();
        bot.start();
    }

    @AfterAll
    void tearDown() {
        if (bot != null) {
            bot.stop();
        }
        if (scrapper != null) {
            scrapper.stop();
        }
        if (postgres != null) {
            postgres.stop();
        }
        if (network != null) {
            network.close();
        }
    }

    private static void buildImage(String dockerfileRelativePath, String imageTag)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder("docker", "build", "-f", dockerfileRelativePath, "-t", imageTag, ".");

        pb.directory(PROJECT_ROOT.toFile());
        pb.inheritIO();

        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException(
                    "docker build failed for " + dockerfileRelativePath + ", exit code = " + exitCode);
        }
    }

    @Test
    void servicesRunInContainers_andInteractOverHttp() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String scrapperBase = "http://localhost:" + scrapper.getMappedPort(8081);
        String botBase = "http://localhost:" + bot.getMappedPort(8080);

        HttpResponse<String> reg = client.send(
                HttpRequest.newBuilder(URI.create(scrapperBase + "/tg-chat/1"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(reg.statusCode()).isEqualTo(200);

        HttpResponse<String> add = client.send(
                HttpRequest.newBuilder(URI.create(scrapperBase + "/links"))
                        .header("Tg-Chat-Id", "1")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "{\"link\":\"https://github.com/user/repo\",\"tags\":[\"work\"],\"filters\":[]}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(add.statusCode()).isEqualTo(200);

        HttpResponse<String> list = client.send(
                HttpRequest.newBuilder(URI.create(scrapperBase + "/links"))
                        .header("Tg-Chat-Id", "1")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(list.statusCode()).isEqualTo(200);
        assertThat(list.body()).contains("https://github.com/user/repo");

        HttpResponse<String> update = client.send(
                HttpRequest.newBuilder(URI.create(botBase + "/updates"))
                        .header("Content-Type", "application/json")
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        "{\"id\":1,\"url\":\"https://github.com/user/repo\",\"description\":\"updated\",\"tgChatIds\":[1]}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(update.statusCode()).isEqualTo(200);
    }

    @Test
    void invalidBotUpdateRequest_returnsBadRequest() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String botBase = "http://localhost:" + bot.getMappedPort(8080);

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create(botBase + "/updates"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"id\":1}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void deletingUnknownChat_returnsNotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String scrapperBase = "http://localhost:" + scrapper.getMappedPort(8081);

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create(scrapperBase + "/tg-chat/999999"))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(404);
    }
}
