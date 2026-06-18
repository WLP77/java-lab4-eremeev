package ru.eremeev.millionaire;

public class Record {
    private final int id;
    private final String playerName;
    private final int prize;
    private final int reachedLevel;
    private final String createdAt;

    public Record(int id, String playerName, int prize, int reachedLevel, String createdAt) {
        this.id = id;
        this.playerName = playerName;
        this.prize = prize;
        this.reachedLevel = reachedLevel;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPrize() {
        return prize;
    }

    public int getReachedLevel() {
        return reachedLevel;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
