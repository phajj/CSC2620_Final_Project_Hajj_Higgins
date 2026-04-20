package client.audio;

public class KeywordDetector {
    private static final String[] KEYWORDS = {"hello", "computer", "java", "keyword", "test"};

    public static boolean containsKeyword(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        String lowerInput = input.toLowerCase();
        for (String keyword : KEYWORDS) {
            if (lowerInput.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
