package ru.eremeev.millionaire;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:WhoWantsToBeAMillionaire.db";

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            createTables();
            importQuestionsIfNeeded();
        } catch (Exception ex) {
            throw new IllegalStateException("Ошибка инициализации базы данных: " + ex.getMessage(), ex);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTables() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS questions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "text TEXT NOT NULL," +
                    "answer1 TEXT NOT NULL," +
                    "answer2 TEXT NOT NULL," +
                    "answer3 TEXT NOT NULL," +
                    "answer4 TEXT NOT NULL," +
                    "right_answer INTEGER NOT NULL CHECK(right_answer BETWEEN 1 AND 4)," +
                    "level INTEGER NOT NULL CHECK(level BETWEEN 1 AND 15)," +
                    "source TEXT NOT NULL DEFAULT 'base'" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_name TEXT NOT NULL," +
                    "prize INTEGER NOT NULL," +
                    "reached_level INTEGER NOT NULL," +
                    "created_at TEXT NOT NULL" +
                    ")");
        }
    }

    private void importQuestionsIfNeeded() throws Exception {
        if (countQuestions() > 0) {
            return;
        }
        InputStream stream = DatabaseManager.class.getResourceAsStream("/questions.tsv");
        if (stream == null) {
            throw new IllegalStateException("Файл questions.tsv не найден в ресурсах проекта");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\t");
                if (parts.length >= 7) {
                    insertQuestion(new Question(parts));
                }
            }
        }
    }

    public int countQuestions() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM questions")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Question getRandomQuestion(int level, Set<Integer> excludedIds) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM questions WHERE level = ?");
        List<Integer> excludes = new ArrayList<>();
        if (excludedIds != null && !excludedIds.isEmpty()) {
            query.append(" AND id NOT IN (");
            int count = 0;
            for (Integer id : excludedIds) {
                if (id != null) {
                    if (count > 0) {
                        query.append(",");
                    }
                    query.append("?");
                    excludes.add(id);
                    count++;
                }
            }
            query.append(")");
        }
        query.append(" ORDER BY RANDOM() LIMIT 1");
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query.toString())) {
            statement.setInt(1, level);
            for (int i = 0; i < excludes.size(); i++) {
                statement.setInt(i + 2, excludes.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapQuestion(rs);
                }
            }
        }
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM questions WHERE level = ? ORDER BY RANDOM() LIMIT 1")) {
            statement.setInt(1, level);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapQuestion(rs);
                }
            }
        }
        throw new SQLException("В базе нет вопросов уровня " + level);
    }

    public void insertQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO questions(text, answer1, answer2, answer3, answer4, right_answer, level, source) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, question.getText());
            statement.setString(2, question.getAnswer(1));
            statement.setString(3, question.getAnswer(2));
            statement.setString(4, question.getAnswer(3));
            statement.setString(5, question.getAnswer(4));
            statement.setInt(6, question.getRightAnswer());
            statement.setInt(7, question.getLevel());
            statement.setString(8, question.getSource());
            statement.executeUpdate();
        }
    }

    public void addRecord(String playerName, int prize, int reachedLevel) throws SQLException {
        String sql = "INSERT INTO records(player_name, prize, reached_level, created_at) VALUES(?, ?, ?, ?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerName == null || playerName.trim().isEmpty() ? "Игрок" : playerName.trim());
            statement.setInt(2, Math.max(0, prize));
            statement.setInt(3, Math.max(0, reachedLevel));
            statement.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            statement.executeUpdate();
        }
    }

    public List<Record> getTopRecords() throws SQLException {
        List<Record> result = new ArrayList<>();
        String sql = "SELECT id, player_name, prize, reached_level, created_at FROM records ORDER BY prize DESC, reached_level DESC, id ASC LIMIT 10";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(new Record(
                        rs.getInt("id"),
                        rs.getString("player_name"),
                        rs.getInt("prize"),
                        rs.getInt("reached_level"),
                        rs.getString("created_at")
                ));
            }
        }
        return result;
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        return new Question(
                rs.getInt("id"),
                rs.getString("text"),
                new String[]{
                        rs.getString("answer1"),
                        rs.getString("answer2"),
                        rs.getString("answer3"),
                        rs.getString("answer4")
                },
                rs.getInt("right_answer"),
                rs.getInt("level"),
                rs.getString("source")
        );
    }
}
