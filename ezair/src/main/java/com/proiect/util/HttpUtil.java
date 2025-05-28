package com.proiect.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.proiect.config.ConfigurationManager;

public class HttpUtil {
    private static final Logger logger = Logger.getLogger(HttpUtil.class.getName());
    private static final ConfigurationManager config = ConfigurationManager.getInstance();

    public static HttpURLConnection createConnection(String path, String method) throws IOException, URISyntaxException {
        String baseUrl = config.getServerUrl();
        URI uri = new URI(baseUrl + path);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(config.getConnectTimeout());
        connection.setReadTimeout(config.getReadTimeout());
        connection.setInstanceFollowRedirects(true);
        return connection;
    }

    public static HttpURLConnection createJsonConnection(String path, String method) throws IOException, URISyntaxException {
        HttpURLConnection connection = createConnection(path, method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    public static void disconnect(HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public static boolean isSuccessful(int responseCode) {
        return responseCode >= 200 && responseCode < 300;
    }

    public static void validateResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (!isSuccessful(responseCode)) {
            String error = String.format("Server returned error code: %d", responseCode);
            logger.warning(error);
            throw new IOException(error);
        }
    }

    public static void retryConnection(RetryableOperation operation) throws IOException, URISyntaxException {
        int maxRetries = config.getMaxRetries();
        int retryDelay = config.getRetryDelay();
        IOException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                operation.execute();
                return;
            } catch (IOException e) {
                lastException = e;
                if (attempt < maxRetries) {
                    logger.log(Level.WARNING, 
                             String.format("Încercare %d/%d eșuată, reîncerc în %d ms", 
                                         attempt + 1, maxRetries, retryDelay), 
                             e);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Operația a fost întreruptă", ie);
                    }
                }
            }
        }

        throw new IOException("Operația a eșuat după " + maxRetries + " încercări", lastException);
    }

    @FunctionalInterface
    public interface RetryableOperation {
        void execute() throws IOException, URISyntaxException;
    }
}
