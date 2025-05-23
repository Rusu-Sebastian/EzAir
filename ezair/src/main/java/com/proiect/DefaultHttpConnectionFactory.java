package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

public class DefaultHttpConnectionFactory implements HttpConnectionFactory {
    @Override
    public HttpURLConnection createConnection(URI uri) throws IOException {
        return (HttpURLConnection) uri.toURL().openConnection();
    }
}
