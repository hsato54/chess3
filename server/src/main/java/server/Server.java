package server;

import dataaccess.*;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {

    UserDAO userdao = new SQLUserDAO();
    GameDAO gamedao = new SQLGameDAO();
    AuthDAO authdao = new SQLAuthDAO();


    UserService userservice = new UserService(userdao, authdao);
    GameService gameservice = new GameService(gamedao, authdao);
    ClearService clearservice = new ClearService(userdao, gamedao, authdao);

    UserHandler userhandler = new UserHandler(userservice);
    GameHandler gamehandler = new GameHandler(gameservice);
    ClearHandler clearhandler = new ClearHandler(clearservice);





    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");


        // Register your endpoints and handle exceptions here.



        Spark.post("/user", userhandler::register);
        Spark.post("/session", userhandler::login);
        Spark.delete("/session", userhandler::logout);
        Spark.get("/game", gamehandler::listGames);
        Spark.post("/game", gamehandler::createGame);
        Spark.put("/game", gamehandler::joinGame);
        Spark.delete("/db", clearhandler::clearData);

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
