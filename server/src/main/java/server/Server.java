package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");


        // Register your endpoints and handle exceptions here.
        UserHandler userhandler = null;
        GameHandler gamehandler = null;
        Spark.post("/user", userhandler::register);
        Spark.post("/session", userhandler::login);
        Spark.delete("/session", userhandler::logout);
        Spark.get("/game", gamehandler::listGames);
        Spark.post("/game", gamehandler::createGame);
        Spark.put("/game", gamehandler::joinGame);

        //do the rest of the endpoints

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
