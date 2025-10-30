package com.example.search;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class RankingService {
    
    private static final int CURRENT_YEAR = java.time.Year.now().getValue();
    
    /**
     * Sophisticated ranking with TF-IDF, title matching, and recency
     */
    public static List<RankedBook> rankBooks(List<Book> books, String query, Set<Integer> bookIds) {
        List<RankedBook> rankedBooks = books.stream()
            .map(RankedBook::new)
            .collect(Collectors.toList());
        
        if (rankedBooks.isEmpty() || query == null) {
            return rankedBooks;
        }
        
        String[] terms = query.toLowerCase().split("\\s+");
        Map<String, Integer> termFreqs = getTermFrequencies(terms);
        
        for (RankedBook book : rankedBooks) {
            double score = calculateBookScore(book, terms, termFreqs, bookIds);
            book.setFinalScore(score);
        }
        
        // Sort by score descending
        rankedBooks.sort((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()));
        return rankedBooks;
    }
    
    /**
     * Calculate comprehensive score for a book
     */
    private static double calculateBookScore(RankedBook book, String[] terms, 
                                           Map<String, Integer> termFreqs, Set<Integer> bookIds) {
        double score = 0.0;
        
        // 1. TF-IDF Component (40% weight)
        double tfidfScore = calculateTfIdfScore(book, terms, termFreqs);
        
        // 2. Title Match Component (35% weight)  
        double titleScore = calculateTitleScore(book, terms);
        
        // 3. Author Match Component (15% weight)
        double authorScore = calculateAuthorScore(book, terms);
        
        // 4. Recency Component (10% weight)
        double recencyScore = calculateRecencyScore(book);
        
        // Weighted combination
        score = (tfidfScore * 0.40) + (titleScore * 0.35) + 
                (authorScore * 0.15) + (recencyScore * 0.10);
        
        return Math.max(0.1, score); // Minimum score
    }
    
    /**
     * Calculate TF-IDF based score
     */
    private static double calculateTfIdfScore(RankedBook book, String[] terms, 
                                            Map<String, Integer> termFreqs) {
        double score = 0.0;
        int totalBooks = 50000; // Estimated corpus size
        
        for (String term : terms) {
            int termFreq = termFreqs.getOrDefault(term, 1);
            
            if (termFreq > 0) {
                // Simple TF-IDF: log(total_docs / doc_freq)
                double idf = Math.log((double) totalBooks / termFreq);
                score += idf;
            }
        }
        
        return Math.min(1.0, score / 10.0); // Normalize
    }
    
    /**
     * Calculate title matching score
     */
    private static double calculateTitleScore(RankedBook book, String[] terms) {
        String title = book.getTitle().toLowerCase();
        double score = 0.0;
        
        for (String term : terms) {
            if (title.contains(term)) {
                // Exact word boundary match gets full points
                if (title.matches(".*\\b" + java.util.regex.Pattern.quote(term) + "\\b.*")) {
                    score += 1.0;
                } else {
                    // Partial match gets less
                    score += 0.6;
                }
                
                // Bonus for title starting with term
                if (title.startsWith(term)) {
                    score += 0.3;
                }
            }
        }
        
        return Math.min(1.0, score / terms.length);
    }
    
    /**
     * Calculate author matching score
     */
    private static double calculateAuthorScore(RankedBook book, String[] terms) {
        String author = book.getAuthor().toLowerCase();
        double score = 0.0;
        
        for (String term : terms) {
            if (author.contains(term)) {
                score += 0.8;
            }
        }
        
        return Math.min(1.0, score / terms.length);
    }
    
    /**
     * Calculate recency score (newer books score higher)
     */
    private static double calculateRecencyScore(RankedBook book) {
        int year = book.getYear();
        
        if (year <= 0) {
            return 0.1; // Unknown year
        }
        
        int age = CURRENT_YEAR - year;
        
        // Books from last 50 years get higher scores
        if (age <= 10) return 1.0;      // Very recent
        if (age <= 25) return 0.8;      // Recent
        if (age <= 50) return 0.6;      // Modern
        if (age <= 100) return 0.4;     // Classic
        
        return 0.2; // Historical
    }
    
    /**
     * Get term frequencies from inverted index
     */
    private static Map<String, Integer> getTermFrequencies(String[] terms) {
        Map<String, Integer> frequencies = new HashMap<>();
        
        try {
            String dataPath = DatabaseConfig.getProperty("data.repository.path", "../data_repository");
            File indexFile = new File(dataPath + "/inverted_index.json");
            
            if (!indexFile.exists()) {
                // Return default frequencies if index not available
                for (String term : terms) {
                    frequencies.put(term, 1000); // Default frequency
                }
                return frequencies;
            }
            
            try (FileReader reader = new FileReader(indexFile)) {
                JsonObject jsonObj = JsonParser.parseReader(reader).getAsJsonObject();
                
                for (String term : terms) {
                    if (jsonObj.has(term)) {
                        JsonArray bookIds = jsonObj.getAsJsonArray(term);
                        frequencies.put(term, bookIds.size());
                    } else {
                        frequencies.put(term, 1); // Rare term
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error reading inverted index: " + e.getMessage());
            // Fallback to default frequencies
            for (String term : terms) {
                frequencies.put(term, 1000);
            }
        }
        
        return frequencies;
    }

}