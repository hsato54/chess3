package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.UserData;
import service.UserService;
import spark.Request;
import spark.Response;

public class UserHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object register(Request req, Response resp) {
        try {
            UserData userData = gson.fromJson(req.body(), UserData.class);

            if (userData.username() == null || userData.password() == null) {
                throw new BadRequestException("Missing username and/or password.");
            }

            AuthData authData = userService.createUser(userData);
            resp.status(200);
            return gson.toJson(authData);

        } catch (BadRequestException e) {
            resp.status(400);
            return gson.toJson(new ErrorResponse("Bad Request: " + e.getMessage()));
        } catch (Exception e) {
            resp.status(403);
            return gson.toJson(new ErrorResponse("Error: Username already taken"));
        }
    }

    public Object login(Request req, Response resp) throws DataAccessException {
            UserData userData = gson.fromJson(req.body(), UserData.class);

            AuthData authData = userService.login(userData);
            resp.status(200);
            return gson.toJson(authData);
    }

    public Object logout(Request req, Response resp) {
        try {

            String authToken = req.headers("authorization");

            if (authToken == null || authToken.isEmpty()) {
                throw new UnauthorizedException();
            }

            // Perform logout using the auth token
            userService.logout(authToken);
            resp.status(200);
            return "{}";

        } catch (UnauthorizedException e) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            resp.status(500);
            return gson.toJson(new ErrorResponse("Internal Server Error: " + e.getMessage()));
        }
    }

    public static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}