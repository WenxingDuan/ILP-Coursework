/**
 * Class for a client application used for Get and Post
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;

public class ServerUtiles {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    /**
     * Static method to get the body of the input address
     * 
     * @param address the communication endpoints to the server
     * @return the body in type of {@link String} of the input address
     */
    public static String get(String address) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(address)).build();
        try {
            HttpResponse<String> response = ServerUtiles.CLIENT.send(request, BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
