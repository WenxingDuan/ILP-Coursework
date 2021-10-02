package uk.ac.ed.inf;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;
import java.io.IOException;
import java.lang.InterruptedException;

public class ClientIO {
    private static final HttpClient client = HttpClient.newHttpClient();
    private String endpoint;

    public ClientIO(String endpoint) {
        this.endpoint = endpoint;
    }

    public String get(String address) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint + "/" + address)).build();
        try {
            HttpResponse<String> response = ClientIO.client.send(request, BodyHandlers.ofString());
            return response.body();
        }
        catch (Exception e) {
            return null;
        }

    }
}
