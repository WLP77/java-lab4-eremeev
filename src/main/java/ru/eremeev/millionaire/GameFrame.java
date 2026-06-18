package ru.eremeev.millionaire;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class GameFrame extends JFrame {
    private static final int[] PRIZES = {500, 1000, 2000, 3000, 5000, 10000, 15000, 25000, 50000, 100000, 200000, 400000, 800000, 1500000, 3000000};
    private static final String[] FRIENDS = {"Алексей", "Мария", "Иван", "Елена", "Дмитрий"};

    private final DatabaseManager databaseManager;
    private final AIGenerator aiGenerator;
    private final Random random = new Random();
    private final DecimalFormat moneyFormat;

    private final JLabel statusLabel = new JLabel("Готово", SwingConstants.CENTER);
    private final JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
    private final JTextArea questionArea = new JTextArea();
    private final JButton[] answerButtons = new JButton[4];
    private final JButton btnFifty = new JButton("50/50");
    private final JButton btnAudience = new JButton("Помощь зала");
    private final JButton btnFriend = new JButton("Звонок другу");
    private final JButton btnMistake = new JButton("Право на ошибку");
    private final JButton btnSwitch = new JButton("Замена вопроса");
    private final JButton btnTakeMoney = new JButton("Забрать деньги");
    private final JButton btnNewGame = new JButton("Новая игра");
    private final JButton btnRecords = new JButton("TOP 10");
    private final JButton btnAi = new JButton("ИИ вопрос");
    private final DefaultListModel<String> moneyModel = new DefaultListModel<>();
    private final JList<String> moneyList = new JList<>(moneyModel);

    private final Set<Integer> usedQuestionIds = new HashSet<>();
    private final Set<String> usedHints = new LinkedHashSet<>();
    private String playerName = "Игрок";
    private int level = 0;
    private int answeredLevel = 0;
    private int guaranteedPrize = 0;
    private int guaranteedLevel = 0;
    private boolean rightToMistakeActive = false;
    private Question currentQuestion;

    public GameFrame() {
        databaseManager = new DatabaseManager();
        aiGenerator = new AIGenerator();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' ');
        moneyFormat = new DecimalFormat("#,###", symbols);
        configureWindow();
        createInterface();
    }

    private void configureWindow() {
        setTitle("Кто хочет стать миллионером");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 720));
        setLocationRelativeTo(null);
    }

    private void createInterface() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(18, 18, 40));

        JLabel titleLabel = new JLabel("Кто хочет стать миллионером", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        statusLabel.setForeground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        root.add(createLeftPanel(), BorderLayout.WEST);
        root.add(createCenterPanel(), BorderLayout.CENTER);
        root.add(createMoneyPanel(), BorderLayout.EAST);

        setContentPane(root);
        pack();
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridLayout(10, 1, 6, 6));
        panel.setPreferredSize(new Dimension(170, 0));
        panel.setOpaque(false);
        List<JButton> buttons = List.of(btnFifty, btnAudience, btnFriend, btnMistake, btnSwitch, btnAi, btnTakeMoney, btnNewGame, btnRecords);
        for (JButton button : buttons) {
            tuneSideButton(button);
            panel.add(button);
        }
        btnFifty.addActionListener(this::useFiftyFifty);
        btnAudience.addActionListener(this::useAudience);
        btnFriend.addActionListener(this::useFriend);
        btnMistake.addActionListener(this::useMistake);
        btnSwitch.addActionListener(this::useSwitchQuestion);
        btnAi.addActionListener(this::useAiQuestion);
        btnTakeMoney.addActionListener(e -> finishGame(getCurrentPrize(), "Игрок забрал деньги"));
        btnNewGame.addActionListener(e -> startGame());
        btnRecords.addActionListener(e -> showRecords());
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);

        imageLabel.setPreferredSize(new Dimension(650, 310));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        loadLogo();
        center.add(imageLabel, BorderLayout.NORTH);

        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setFont(new Font("Arial", Font.BOLD, 20));
        questionArea.setRows(4);
        questionArea.setMargin(new Insets(14, 14, 14, 14));
        questionArea.setBackground(new Color(235, 235, 255));
        questionArea.setForeground(Color.BLACK);
        center.add(new JScrollPane(questionArea), BorderLayout.CENTER);

        JPanel answersPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        answersPanel.setOpaque(false);
        for (int i = 0; i < answerButtons.length; i++) {
            JButton button = new JButton();
            button.setFont(new Font("Arial", Font.BOLD, 15));
            button.setActionCommand(String.valueOf(i + 1));
            button.setMargin(new Insets(12, 12, 12, 12));
            button.addActionListener(this::answerQuestion);
            answerButtons[i] = button;
            answersPanel.add(button);
        }
        center.add(answersPanel, BorderLayout.SOUTH);
        return center;
    }

    private JPanel createMoneyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(165, 0));
        panel.setOpaque(false);
        for (int i = PRIZES.length - 1; i >= 0; i--) {
            moneyModel.addElement(formatMoney(PRIZES[i]));
        }
        moneyList.setFont(new Font("Arial", Font.BOLD, 17));
        moneyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moneyList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 8));
                if (isSelected) {
                    label.setBackground(new Color(255, 205, 80));
                    label.setForeground(Color.BLACK);
                }
                return label;
            }
        });
        panel.add(new JScrollPane(moneyList), BorderLayout.CENTER);
        return panel;
    }

    private void tuneSideButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setMargin(new Insets(8, 6, 8, 6));
    }

    private void loadLogo() {
        try (InputStream stream = GameFrame.class.getResourceAsStream("/picture.jpg")) {
            if (stream != null) {
                Image image = ImageIO.read(stream);
                Image scaled = image.getScaledInstance(650, 310, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
            } else {
                imageLabel.setText("Кто хочет стать миллионером");
                imageLabel.setFont(new Font("Arial", Font.BOLD, 24));
                imageLabel.setForeground(Color.WHITE);
            }
        } catch (Exception ex) {
            imageLabel.setText("Кто хочет стать миллионером");
        }
    }

    public void startGame() {
        askPlayerData();
        usedQuestionIds.clear();
        usedHints.clear();
        level = 0;
        answeredLevel = 0;
        rightToMistakeActive = false;
        enableHints(true);
        enableAnswerButtons(true);
        nextStep();
    }

    private void askPlayerData() {
        JTextField nameField = new JTextField(playerName);
        JComboBox<String> bankBox = new JComboBox<>(createBankOptions());
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.add(new JLabel("Введите имя игрока:"));
        panel.add(nameField);
        panel.add(new JLabel("Выберите несгораемую сумму:"));
        panel.add(bankBox);
        int result = JOptionPane.showConfirmDialog(this, panel, "Начало игры", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            playerName = nameField.getText() == null || nameField.getText().trim().isEmpty() ? "Игрок" : nameField.getText().trim();
            guaranteedPrize = parseMoney((String) bankBox.getSelectedItem());
            guaranteedLevel = getPrizeLevel(guaranteedPrize);
        } else if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Игрок";
        }
    }

    private String[] createBankOptions() {
        String[] result = new String[PRIZES.length + 1];
        result[0] = "0";
        for (int i = 0; i < PRIZES.length; i++) {
            result[i + 1] = formatMoney(PRIZES[i]);
        }
        return result;
    }

    private void nextStep() {
        try {
            if (level >= 15) {
                finishGame(PRIZES[PRIZES.length - 1], "Победа в игре");
                return;
            }
            level++;
            currentQuestion = databaseManager.getRandomQuestion(level, usedQuestionIds);
            usedQuestionIds.add(currentQuestion.getId());
            rightToMistakeActive = false;
            showQuestion(currentQuestion);
            moneyList.setSelectedIndex(PRIZES.length - level);
            statusLabel.setText("Игрок: " + playerName + " | Вопрос " + level + " из 15 | Несгораемая сумма: " + formatMoney(guaranteedPrize));
            enableAnswerButtons(true);
        } catch (SQLException ex) {
            showError("Не удалось получить вопрос: " + ex.getMessage());
        }
    }

    private void showQuestion(Question question) {
        questionArea.setText(question.getText());
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setText(toHtml((i + 1) + ". " + question.getAnswer(i + 1), 330));
            answerButtons[i].setEnabled(true);
        }
    }

    private void answerQuestion(ActionEvent event) {
        if (currentQuestion == null) {
            return;
        }
        JButton button = (JButton) event.getSource();
        int selected = Integer.parseInt(button.getActionCommand());
        if (selected == currentQuestion.getRightAnswer()) {
            answeredLevel = level;
            if (level == 15) {
                finishGame(PRIZES[PRIZES.length - 1], "Победа в игре");
            } else {
                JOptionPane.showMessageDialog(this, "Правильный ответ!", "Результат", JOptionPane.INFORMATION_MESSAGE);
                nextStep();
            }
            return;
        }
        if (rightToMistakeActive) {
            button.setEnabled(false);
            rightToMistakeActive = false;
            JOptionPane.showMessageDialog(this, "Ответ неверный, но право на ошибку сохраняет игру. Выберите другой вариант.", "Право на ошибку", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int prize = answeredLevel >= guaranteedLevel ? guaranteedPrize : 0;
        JOptionPane.showMessageDialog(this, "Неверный ответ. Правильный вариант: " + currentQuestion.getRightAnswer() + ".", "Игра окончена", JOptionPane.WARNING_MESSAGE);
        finishGame(prize, "Неверный ответ");
    }

    private void useFiftyFifty(ActionEvent event) {
        if (!markHintUsed("50/50", btnFifty)) {
            return;
        }
        List<JButton> wrongButtons = new ArrayList<>();
        for (JButton button : answerButtons) {
            int number = Integer.parseInt(button.getActionCommand());
            if (number != currentQuestion.getRightAnswer()) {
                wrongButtons.add(button);
            }
        }
        int disabled = 0;
        while (disabled < 2 && !wrongButtons.isEmpty()) {
            JButton button = wrongButtons.remove(random.nextInt(wrongButtons.size()));
            button.setEnabled(false);
            disabled++;
        }
    }

    private void useAudience(ActionEvent event) {
        if (!markHintUsed("Помощь зала", btnAudience)) {
            return;
        }
        int[] votes = new int[4];
        int rightIndex = currentQuestion.getRightAnswer() - 1;
        int base = Math.max(35, 75 - level * 3);
        votes[rightIndex] = base + random.nextInt(16);
        int rest = 100 - votes[rightIndex];
        for (int i = 0; i < votes.length; i++) {
            if (i != rightIndex) {
                int value = i == votes.length - 1 ? rest : random.nextInt(rest + 1);
                votes[i] = value;
                rest -= value;
            }
        }
        votes[rightIndex] += rest;
        StringBuilder builder = new StringBuilder("Результаты голосования зала:\n\n");
        for (int i = 0; i < votes.length; i++) {
            builder.append(i + 1).append(". ").append(currentQuestion.getAnswer(i + 1)).append(" - ").append(votes[i]).append("%\n");
        }
        JOptionPane.showMessageDialog(this, builder.toString(), "Помощь зала", JOptionPane.INFORMATION_MESSAGE);
    }

    private void useFriend(ActionEvent event) {
        if (!markHintUsed("Звонок другу", btnFriend)) {
            return;
        }
        String friend = FRIENDS[random.nextInt(FRIENDS.length)];
        int probability = Math.max(35, 88 - level * 3);
        int answer = random.nextInt(100) < probability ? currentQuestion.getRightAnswer() : 1 + random.nextInt(4);
        JOptionPane.showMessageDialog(this, friend + " думает, что правильный ответ - " + answer + ".\n" + currentQuestion.getAnswer(answer), "Звонок другу", JOptionPane.INFORMATION_MESSAGE);
    }

    private void useMistake(ActionEvent event) {
        if (!markHintUsed("Право на ошибку", btnMistake)) {
            return;
        }
        rightToMistakeActive = true;
        JOptionPane.showMessageDialog(this, "Теперь можно один раз ошибиться на текущем вопросе.", "Право на ошибку", JOptionPane.INFORMATION_MESSAGE);
    }

    private void useSwitchQuestion(ActionEvent event) {
        if (!canUseHint()) {
            return;
        }
        try {
            Question newQuestion = databaseManager.getRandomQuestion(level, usedQuestionIds);
            currentQuestion = newQuestion;
            usedQuestionIds.add(currentQuestion.getId());
            showQuestion(currentQuestion);
            markHintUsed("Замена вопроса", btnSwitch);
            JOptionPane.showMessageDialog(this, "Вопрос заменен на другой вопрос того же уровня.", "Замена вопроса", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Не удалось заменить вопрос: " + ex.getMessage());
        }
    }

    private void useAiQuestion(ActionEvent event) {
        if (!aiGenerator.isConfigured()) {
            JOptionPane.showMessageDialog(this,
                    "API генерации не настроены.\nУкажите переменные окружения MILLIONAIRE_AI_URL, MILLIONAIRE_AI_KEY и MILLIONAIRE_AI_MODEL.",
                    "ИИ вопрос", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        setButtonsEnabled(false);
        statusLabel.setText("Генерация вопроса через API...");
        new Thread(() -> {
            var generated = aiGenerator.generateQuestion(Math.max(1, level));
            SwingUtilities.invokeLater(() -> {
                setButtonsEnabled(true);
                if (generated.isPresent()) {
                    try {
                        databaseManager.insertQuestion(generated.get());
                        currentQuestion = generated.get();
                        showQuestion(currentQuestion);
                        JOptionPane.showMessageDialog(this, "Новый вопрос сгенерирован и добавлен в базу данных.", "ИИ вопрос", JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        showError("Вопрос сгенерирован, но не сохранен в БД: " + ex.getMessage());
                    }
                } else {
                    showError("Не удалось получить вопрос через API.");
                }
                statusLabel.setText("Игрок: " + playerName + " | Вопрос " + level + " из 15");
            });
        }).start();
    }

    private boolean markHintUsed(String hintName, JButton button) {
        if (!canUseHint()) {
            return false;
        }
        usedHints.add(hintName);
        button.setEnabled(false);
        statusLabel.setText("Использована подсказка: " + hintName + " | Всего подсказок: " + usedHints.size() + " из 4");
        return true;
    }

    private boolean canUseHint() {
        if (currentQuestion == null) {
            return false;
        }
        if (usedHints.size() >= 4) {
            JOptionPane.showMessageDialog(this, "Можно использовать только четыре подсказки из пяти.", "Лимит подсказок", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void finishGame(int prize, String reason) {
        enableAnswerButtons(false);
        enableHints(false);
        int safePrize = Math.max(0, prize);
        try {
            databaseManager.addRecord(playerName, safePrize, answeredLevel);
        } catch (SQLException ex) {
            showError("Результат не удалось сохранить: " + ex.getMessage());
        }
        statusLabel.setText(reason + " | Выигрыш: " + formatMoney(safePrize));
        JOptionPane.showMessageDialog(this, playerName + ", игра окончена.\nВаш выигрыш: " + formatMoney(safePrize), "Результат", JOptionPane.INFORMATION_MESSAGE);
        showRecords();
    }

    private void showRecords() {
        try {
            List<Record> records = databaseManager.getTopRecords();
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Место", "Игрок", "Выигрыш", "Уровень", "Дата"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (int i = 0; i < records.size(); i++) {
                Record record = records.get(i);
                model.addRow(new Object[]{i + 1, record.getPlayerName(), formatMoney(record.getPrize()), record.getReachedLevel(), record.getCreatedAt()});
            }
            JTable table = new JTable(model);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(26);
            JOptionPane.showMessageDialog(this, new JScrollPane(table), "TOP 10 игроков", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            showError("Не удалось вывести таблицу рекордов: " + ex.getMessage());
        }
    }

    private void enableHints(boolean enabled) {
        btnFifty.setEnabled(enabled);
        btnAudience.setEnabled(enabled);
        btnFriend.setEnabled(enabled);
        btnMistake.setEnabled(enabled);
        btnSwitch.setEnabled(enabled);
        btnAi.setEnabled(enabled);
        btnTakeMoney.setEnabled(enabled);
    }

    private void enableAnswerButtons(boolean enabled) {
        for (JButton button : answerButtons) {
            button.setEnabled(enabled);
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        enableAnswerButtons(enabled);
        btnFifty.setEnabled(enabled && !usedHints.contains("50/50"));
        btnAudience.setEnabled(enabled && !usedHints.contains("Помощь зала"));
        btnFriend.setEnabled(enabled && !usedHints.contains("Звонок другу"));
        btnMistake.setEnabled(enabled && !usedHints.contains("Право на ошибку"));
        btnSwitch.setEnabled(enabled && !usedHints.contains("Замена вопроса"));
        btnAi.setEnabled(enabled);
        btnTakeMoney.setEnabled(enabled);
        btnNewGame.setEnabled(enabled);
        btnRecords.setEnabled(enabled);
    }

    private int getCurrentPrize() {
        if (answeredLevel <= 0) {
            return 0;
        }
        return PRIZES[answeredLevel - 1];
    }

    private int getPrizeLevel(int prize) {
        for (int i = 0; i < PRIZES.length; i++) {
            if (PRIZES[i] == prize) {
                return i + 1;
            }
        }
        return 0;
    }

    private int parseMoney(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.replace(" ", "").trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private String formatMoney(int value) {
        return moneyFormat.format(value);
    }

    private String toHtml(String text, int width) {
        return "<html><body style='width:" + width + "px;text-align:center'>" + escapeHtml(text) + "</body></html>";
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
