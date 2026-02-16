package com.saveplate.models;

public class Rating {
    private int ratingId;  //smtg like database ID
    private User fromUser; // The person giving the rating
    private User toUser;   // The vendor or NGO being rated
    private int score;     // 1 to 5 stars
    private String comment;

    public Rating(int ratingId, User fromUser, User toUser, int score, String comment) {
        this.ratingId = ratingId;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.score = score;
        this.comment = comment;
    }

    // Getters and Setters
    public int getRatingId() { return ratingId; }
    public User getFromUser() { return fromUser; }
    public User getToUser() { return toUser; }
    public int getScore() { return score; }
    public String getComment() { return comment; }

    @Override
    public String toString() {
        return "Rating from " + fromUser.getName() + ": " + score + "/5 - " + comment;
    }

}
