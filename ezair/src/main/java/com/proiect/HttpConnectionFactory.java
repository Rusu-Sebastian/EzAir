package com.proiect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

public interface HttpConnectionFactory {
    HttpURLConnection createConnection(URI uri) throws IOException;
}
