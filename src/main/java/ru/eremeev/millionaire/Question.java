package ru.eremeev.millionaire;

import java.util.Arrays;
import java.util.Objects;

public class Question {
    private int id;
    private String text;
    private String[] answers;
    private int rightAnswer;
    private int level;
    private String source;

    public Question() {
        this(0, "", new String[]{"", "", "", ""}, 1, 1, "base");
    }

    public Question(String[] values) {
        this(
                0,
                values.length > 0 ? values[0] : "",
                new String[]{
                        values.length > 1 ? values[1] : "",
                        values.length > 2 ? values[2] : "",
                        values.length > 3 ? values[3] : "",
                        values.length > 4 ? values[4] : ""
                },
                parseInt(values.length > 5 ? values[5] : "1", 1),
                parseInt(values.length > 6 ? values[6] : "1", 1),
                "base"
        );
    }

    public Question(int id, String text, String[] answers, int rightAnswer, int level, String source) {
        setId(id);
        setText(text);
        setAnswers(answers);
        setRightAnswer(rightAnswer);
        setLevel(level);
        setSource(source);
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = Math.max(0, id);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null || text.trim().isEmpty()) {
            this.text = "Вопрос не задан";
        } else {
            this.text = text.trim();
        }
    }

    public String[] getAnswers() {
        return Arrays.copyOf(answers, answers.length);
    }

    public String getAnswer(int index) {
        if (index < 1 || index > 4) {
            return "";
        }
        return answers[index - 1];
    }

    public void setAnswers(String[] answers) {
        this.answers = new String[4];
        for (int i = 0; i < this.answers.length; i++) {
            if (answers != null && i < answers.length && answers[i] != null && !answers[i].trim().isEmpty()) {
                this.answers[i] = answers[i].trim();
            } else {
                this.answers[i] = "Вариант " + (i + 1);
            }
        }
    }

    public int getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(int rightAnswer) {
        if (rightAnswer < 1 || rightAnswer > 4) {
            this.rightAnswer = 1;
        } else {
            this.rightAnswer = rightAnswer;
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < 1) {
            this.level = 1;
        } else if (level > 15) {
            this.level = 15;
        } else {
            this.level = level;
        }
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            this.source = "base";
        } else {
            this.source = source.trim();
        }
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", answers=" + Arrays.toString(answers) +
                ", rightAnswer=" + rightAnswer +
                ", level=" + level +
                ", source='" + source + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Question other)) {
            return false;
        }
        return id == other.id &&
                rightAnswer == other.rightAnswer &&
                level == other.level &&
                Objects.equals(text, other.text) &&
                Arrays.equals(answers, other.answers) &&
                Objects.equals(source, other.source);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, text, rightAnswer, level, source);
        result = 31 * result + Arrays.hashCode(answers);
        return result;
    }
}
