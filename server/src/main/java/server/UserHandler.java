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
            return gson.toJson(new ErrorResponse("Bad Request: error" + e.getMessage()));
        } catch (Exception e) {
            resp.status(403);
            return gson.toJson(new ErrorResponse("Error: Username already taken"));
        }
    }

    public Object login(Request req, Response resp) {

        UserData userData = gson.fromJson(req.body(), UserData.class);
        AuthData authData = null;

        try {
            authData = userService.login(userData);
        }
        catch(DataAccessException e) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Unauthorized: error" + e.getMessage()));
        }
        resp.status(200);
        return gson.toJson(authData);


    }

    public Object logout(Request req, Response resp) {
        try {

            String authToken = req.headers("authorization");

            if (authToken == null || authToken.isEmpty()) {
                throw new UnauthorizedException();
            }

            userService.logout(authToken);
            resp.status(200);
            return "{}";

        } catch (Exception e) {
            resp.status(401);
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