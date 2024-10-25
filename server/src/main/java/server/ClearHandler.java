package server;

import com.google.gson.Gson;
import service.ClearService;
import spark.Request;
import spark.Response;

public class ClearHandler {

    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public Object clearData(Request req, Response resp) {
        try {
            // Perform the clear operation
            clearService.clear();
            resp.status(200);
            return gson.toJson(new SuccessResponse("Data cleared successfully."));

        } catch (Exception e) {
            resp.status(500);
            return gson.toJson(new ErrorResponse("Internal Server Error: " + e.getMessage()));
        }
    }

    public static class SuccessResponse {
        private final String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
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
