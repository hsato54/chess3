import com.google.gson.Gson;
import spark.*;
import java.util.*;

public class ServerExample {
    private ArrayList<String> names = new ArrayList<>();

    public static void main(String[] args) {
        new ServerExample().run();
    }

    private void run() {
        // Specify the port you want the server to listen on
        Spark.port(8080);

        // Register a directory for hosting static files
        Spark.externalStaticFileLocation("public");

        // Register handlers for each endpoint using the method reference syntax
        Spark.post("/name/:name", this::addName);
        Spark.get("/name", this::listNames);
        Spark.delete("/name/:name", this::deleteName);
    }

    private Object addName(Request req, Response res) {
        names.add(req.params(":name"));
        return listNames(req, res);
    }
    //need these method for each handler, return an object/string.

    private Object listNames(Request req, Response res) {
        res.type("application/json");
        return new Gson().toJson(Map.of("name", names));
    }

    private Object deleteName(Request req, Response res) {
        names.remove(req.params(":name"));
        return listNames(req, res);
    }

    //
}