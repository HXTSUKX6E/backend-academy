package backend.academy.linktracker.scrapper.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public final class LinkUrlParser {

    private LinkUrlParser() {}

    public record GitHubRepo(String owner, String repo) {}

    public static Optional<GitHubRepo> parseGitHubRepo(String url) {
        try {
            URI uri = URI.create(url);
            if (uri.getHost() == null || !uri.getHost().contains("github.com")) {
                return Optional.empty();
            }
            List<String> segments = splitPath(uri.getPath());
            if (segments.size() < 2) {
                return Optional.empty();
            }
            return Optional.of(new GitHubRepo(segments.get(0), segments.get(1)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static OptionalLong parseStackOverflowQuestionId(String url) {
        try {
            URI uri = URI.create(url);
            if (uri.getHost() == null || !uri.getHost().contains("stackoverflow.com")) {
                return OptionalLong.empty();
            }
            List<String> segments = splitPath(uri.getPath());
            int idx = segments.indexOf("questions");
            if (idx < 0 || segments.size() <= idx + 1) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(Long.parseLong(segments.get(idx + 1)));
        } catch (Exception e) {
            return OptionalLong.empty();
        }
    }

    private static List<String> splitPath(String path) {
        String[] raw = path.split("/");
        List<String> out = new ArrayList<>();
        for (String part : raw) {
            if (!part.isBlank()) {
                out.add(part);
            }
        }
        return out;
    }
}
