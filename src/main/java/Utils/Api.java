package Utils;

public class Api {
    public static String getApiKey() {
        String key = System.getenv("MY_API_KEY");
        if (key == null) {
            throw new RuntimeException("API key not found! Please set MY_API_KEY environment variable.");
        }
        return key;
    }
}