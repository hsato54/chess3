package server;

import dataaccess.*;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.*;

import java.util.concurrent.ConcurrentHashMap;

public class Server {



    public static UserService userService;
    public static GameService gameService;

    private final ClearService clearservice;
    private final UserHandler userhandler;
    private final GameHandler gamehandler;
    private final ClearHandler clearhandler;

    private final WebsocketHandler websockethandler;


    public Server() {
        UserDAO userdao = new SQLUserDAO();
        GameDAO gamedao = new SQLGameDAO();
        AuthDAO authdao = new SQLAuthDAO();

        userService = new UserService(userdao, authdao);
        gameService = new GameService(gamedao, authdao);

        clearservice = new ClearService(userdao, gamedao, authdao);
        userhandler = new UserHandler(userService);
        gamehandler = new GameHandler(gameService);
        clearhandler = new ClearHandler(clearservice);

        websockethandler = new WebsocketHandler();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        //System.out.println("[DEBUG] Registering WebSocket endpoint at /ws");
        Spark.webSocket("/ws", websockethandler);


        //System.out.println("[DEBUG] Registering REST endpoints...");
        Spark.post("/user", userhandler::register);
        Spark.post("/session", userhandler::login);
        Spark.delete("/session", userhandler::logout);
        Spark.get("/game", gamehandler::listGames);
        Spark.post("/game", gamehandler::createGame);
        Spark.put("/game", gamehandler::joinGame);
        Spark.delete("/db", clearhandler::clearData);



        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
