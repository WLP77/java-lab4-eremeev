package ru.eremeev.millionaire;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIGenerator {
    private static final Pattern CONTENT_PATTERN = Pattern.compile("\\\"content\\\"\\s*:\\s*\\\"(.*?)\\\"", Pattern.DOTALL);
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public AIGenerator() {
        apiUrl = System.getenv("MILLIONAIRE_AI_URL");
        apiKey = System.getenv("MILLIONAIRE_AI_KEY");
        String envModel = System.getenv("MILLIONAIRE_AI_MODEL");
        model = envModel == null || envModel.trim().isEmpty() ? "gpt-4o-mini" : envModel.trim();
    }

    public boolean isConfigured() {
        return apiUrl != null && !apiUrl.trim().isEmpty() && apiKey != null && !apiKey.trim().isEmpty();
    }

    public Optional<Question> generateQuestion(int level) {
        if (!isConfigured()) {
            return Optional.empty();
        }
        try {
            String prompt = "Сгенерируй один вопрос для игры Кто хочет стать миллионером на русском языке. " +
                    "Уровень сложности от 1 до 15: " + level + ". " +
                    "Верни только строки в формате: QUESTION: текст, A1: ответ, A2: ответ, A3: ответ, A4: ответ, RIGHT: номер от 1 до 4. " +
                    "Не добавляй пояснения.";
            String body = "{\"model\":\"" + escapeJson(model) + "\",\"messages\":[{" +
                    "\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}],\"temperature\":0.8}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }
            String content = extractContent(response.body());
            return parseQuestion(content, level);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Optional<Question> parseQuestion(String text, int level) {
        if (text == null || text.trim().isEmpty()) {
            return Optional.empty();
        }
        String question = findLine(text, "QUESTION:");
        String a1 = findLine(text, "A1:");
        String a2 = findLine(text, "A2:");
        String a3 = findLine(text, "A3:");
        String a4 = findLine(text, "A4:");
        String rightText = findLine(text, "RIGHT:");
        int right = 1;
        try {
            right = Integer.parseInt(rightText.replaceAll("[^1-4]", "").substring(0, 1));
        } catch (Exception ignored) {
            right = 1;
        }
        if (question.isEmpty() || a1.isEmpty() || a2.isEmpty() || a3.isEmpty() || a4.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Question(0, question, new String[]{a1, a2, a3, a4}, right, level, "ai"));
    }

    private String findLine(String text, String prefix) {
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.toUpperCase().startsWith(prefix)) {
                return trimmed.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private String extractContent(String json) {
        Matcher matcher = CONTENT_PATTERN.matcher(json);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return "";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String value) {
        StringBuilder result = new StringBuilder();
        boolean slash = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (slash) {
                switch (ch) {
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case '\\' -> result.append('\\');
                    case '"' -> result.append('"');
                    default -> result.append(ch);
                }
                slash = false;
            } else if (ch == '\\') {
                slash = true;
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
