package com.example.search;

public class RankedBook extends Book {
    private double finalScore;
    private double tfidfScore;
    private double titleScore;
    private double authorScore;
    private double recencyScore;
    
    public RankedBook() {}
    
    public RankedBook(Book book) {
        super(book.getBook_id(), book.getTitle(), book.getAuthor(), book.getLanguage(), book.getYear());
        this.finalScore = 0.0;
        this.tfidfScore = 0.0;
        this.titleScore = 0.0;
        this.authorScore = 0.0;
        this.recencyScore = 0.0;
    }
    
    // Getters and setters
    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }
    
    public double getTfidfScore() { return tfidfScore; }
    public void setTfidfScore(double tfidfScore) { this.tfidfScore = tfidfScore; }
    
    public double getTitleScore() { return titleScore; }
    public void setTitleScore(double titleScore) { this.titleScore = titleScore; }
    
    public double getAuthorScore() { return authorScore; }
    public void setAuthorScore(double authorScore) { this.authorScore = authorScore; }
    
    public double getRecencyScore() { return recencyScore; }
    public void setRecencyScore(double recencyScore) { this.recencyScore = recencyScore; }
    
    /**
     * Get formatted score breakdown for debugging
     */
    public String getScoreBreakdown() {
        return String.format("Final: %.3f (TF-IDF: %.3f, Title: %.3f, Author: %.3f, Recency: %.3f)",
                finalScore, tfidfScore, titleScore, authorScore, recencyScore);
    }
}