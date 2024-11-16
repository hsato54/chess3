package ui;

import com.google.gson.Gson;
import model.GameData;
import model.ListGames;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;

public class HttpCommunicator {

    private final String baseURL;
    private final ServerFacade facade;
    private final Gson gson;

    public HttpCommunicator(ServerFacade facade, String serverDomain) {
        this.baseURL = "http://" + serverDomain;
        this.facade = facade;
        this.gson = new Gson();
    }

    public boolean register(String username, String password, String email) {
        var body = Map.of("username", username, "password", password, "email", email);
        Map<String, Object> resp = sendPostRequest("/user", gson.toJson(body));
        if (resp.containsKey("Error")) return false;

        facade.setAuthToken((String) resp.get("authToken"));
        return true;
    }

    public boolean login(String username, String password) {
        var body = Map.of("username", username, "password", password);
        Map<String, Object> resp = sendPostRequest("/session", gson.toJson(body));
        if (resp.containsKey("Error")) return false;

        facade.setAuthToken((String) resp.get("authToken"));
        return true;
    }

    public boolean logout() {
        Map<String, Object> resp = sendDeleteRequest("/session");
        if (resp.containsKey("Error")) return false;

        facade.setAuthToken(null);
        return true;
    }

    public int createGame(String gameName) {
        var body = Map.of("gameName", gameName);
        Map<String, Object> resp = sendPostRequest("/game", gson.toJson(body));
        if (resp.containsKey("Error")) return -1;

        return ((Double) resp.get("gameID")).intValue();
    }

    public HashSet<GameData> listGames() {
        String resp = sendGetRequest("/game");
        if (resp.contains("Error")) return new HashSet<>();

        ListGames gamesList = gson.fromJson(resp, ListGames.class);
        return new HashSet<>(gamesList.games());
    }

    public boolean joinGame(int gameId, String playerColor) {
        var body = Map.of("gameID", gameId, "playerColor", playerColor);
        Map<String, Object> resp = sendPutRequest("/game", gson.toJson(body));
        return !resp.containsKey("Error");
    }

    public boolean observeGame(int gameId) {
        var body = Map.of("gameID", gameId);
        Map<String, Object> resp = sendPostRequest("/game/observe", gson.toJson(body));
        return !resp.containsKey("Error");
    }

    public String getAuthToken() {
        return facade.getAuthToken();
    }

    private Map<String, Object> sendPostRequest(String endpoint, String body) {
        return sendRequest("POST", endpoint, body);
    }

    private Map<String, Object> sendPutRequest(String endpoint, String body) {
        return sendRequest("PUT", endpoint, body);
    }

    private Map<String, Object> sendDeleteRequest(String endpoint) {
        return sendRequest("DELETE", endpoint, null);
    }

    private String sendGetRequest(String endpoint) {
        try {
            HttpURLConnection connection = createConnection("GET", endpoint, null);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return "Error: Unauthorized";
            }
            return readResponse(connection.getInputStream());
        } catch (IOException | URISyntaxException e) {
            return "Error: " + e.getMessage();
        }
    }

    private Map<String, Object> sendRequest(String method, String endpoint, String body) {
        try {
            HttpURLConnection connection = createConnection(method, endpoint, body);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return Map.of("Error", "Unauthorized");
            }
            return parseJsonResponse(connection.getInputStream());
        } catch (IOException | URISyntaxException e) {
            return Map.of("Error", e.getMessage());
        }
    }

    private HttpURLConnection createConnection(String method, String endpoint, String body) throws IOException, URISyntaxException {
        URI uri = new URI(baseURL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod(method);
        if (facade.getAuthToken() != null) {
            connection.setRequestProperty("Authorization", facade.getAuthToken());
        }
        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.getBytes());
            }
        }
        return connection;
    }

    private Map<String, Object> parseJsonResponse(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            return gson.fromJson(reader, Map.class);
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
//    public void clear() {
//        Map<String, Object> response = request("DELETE", "/clear");
//    }
//    private Map<String, Object> request(String method, String endpoint) {
//        Map<String, Object> responseMap = null;
//        try {
//            URL url = new URL(baseURL + endpoint);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod(method);
//
//            if (facade.getAuthToken() != null) {
//                connection.setRequestProperty("Authorization", facade.getAuthToken());
//            }
//
//            connection.connect();
//
//            InputStream responseStream = connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST
//                    ? connection.getInputStream()
//                    : connection.getErrorStream();
//            InputStreamReader reader = new InputStreamReader(responseStream);
//            responseMap = new Gson().fromJson(reader, Map.class);
//            reader.close();
//        } catch (Exception e) {
//            responseMap = Map.of("Error", e.getMessage());
//        }
//        return responseMap;
//    }

}
