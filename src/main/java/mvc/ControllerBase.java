package mvc;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mvc.Helpers.JsonConverter;
import mvc.Helpers.SessionHelper;
import mvc.Http.HttpBase;
import mvc.Http.HttpStatusCode;

public class ControllerBase extends HttpBase {
    private boolean isRedirect = true; // to hold redirect status temporarily
    private static final String FILE_UPLOAD_PATH = System.getenv("FILE_UPLOAD_PATH");
    
    public ControllerBase() {
        super();

    }

    @Override
    protected Result page() throws Exception {
        String controller = this.getClass().getSimpleName();
        String action = getCaller();
        isRedirect = false; // no redirect
        return page(action, controller, null);
    }

    @Override
    protected Result page(String action) throws Exception {
        String controller = this.getClass().getSimpleName();
        return page(action, controller, null);
    }

    protected Result page(String action, JsonNode params) throws Exception {
        String controller = this.getClass().getSimpleName();
        return page(action, controller, params);
    }

    @Override
    protected Result page(String action, String controller) throws Exception {
        return page(action, controller, null);
    }


    protected Result page(String action, String controller, JsonNode params) throws Exception {

        String path = "/" + controller.replace("Controller", "") + "/" + action + ".jsp";
        Result result = new Result();

        // Setup response
        result.setPath(path);
        result.setContentType("text/html");
        result.setStatusCode(HttpStatusCode.OK);
        if (params != null) {
            result.setParams(params);
        }
        // Check if the action is the same as the controller
        // If the action is the same as the controller, set redirect to false
        result.setRedirect(isRedirect);
        isRedirect = true; // set back to original state
        return result;
    }

    @Override
    protected Result json(Object data) throws Exception {
        return json(data, HttpStatusCode.OK, "success");
    }

    @Override
    protected Result json(Object data, HttpStatusCode status, String message) throws Exception {
        Result result = new Result();

        JsonNode json = getResponseTemplate();
        // Setup json data
        if (data != null) {
            ((ObjectNode) json).put("data", JsonConverter.serialize(data));
        }
        ((ObjectNode) json).put("status", status.get());
        ((ObjectNode) json).put("message", message);
        ((ObjectNode) json).put("timestamp", System.currentTimeMillis());
        // System.out.println(json);
        // Setup response
        result.setData(json);
        result.setContentType("application/json");
        result.setStatusCode(status);
        result.setRedirect(false);
        return result;
    }

    @Override
    protected Result content(Object data, String contentType, HttpStatusCode status) {
        Result result = new Result();

        // Setup response
        result.setData(data);
        result.setContentType(contentType);
        result.setStatusCode(status);

        return result;
    }

    @Override
    protected Result content(Object data, String contentType) throws Exception {
        return content(data, contentType, HttpStatusCode.OK);
    }

    protected Result file(byte[] bytes) throws Exception {
        String uuid = UUID.randomUUID().toString();
        return file(bytes, uuid);
    }

    protected Result file(byte[] bytes, String fileName) throws Exception {
        return file(bytes, fileName, FileType.UNKNOWN);
    }

    protected Result file(byte[] bytes, String fileName, FileType fileType) throws Exception {
        Result result = new Result();
        result.setContentType(fileType.getMimeType());
        result.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "." + fileType.getExtension() + "\"");
        response.setContentLength(bytes.length);

        result.setData(bytes);

        return result;
    }

    // Caution: only support source attribute at image tag
    protected Result source(byte[] bytes, String fileName, FileType fileType) throws Exception {
        Result result = new Result();
        result.setContentType(fileType.getMimeType());
        result.setData(bytes);
        return result;
    }

    @Override
    protected Result success() throws Exception {
        return success("success");
    }

    @Override
    protected Result success(String message) throws Exception {
        return success(HttpStatusCode.OK, message);
    }

    @Override
    protected Result success(HttpStatusCode status, String message) throws Exception {
        return json(null, status, message);
    }

    @Override
    protected Result error() throws Exception {
        return error("error");
    }

    @Override
    protected Result error(String message) throws Exception {
        return error(HttpStatusCode.BAD_REQUEST, message);
    }

    @Override
    protected Result error(HttpStatusCode status, String message) throws Exception {
        return json(null, status, message);
    }

    protected SessionHelper getSessionHelper() {
        return new SessionHelper(request.getSession());
    }

    private static String getCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Index 0: getStackTrace(), Index 1: getCallerMethodName(), Index 2: actual
        // caller
        return stackTrace[3].getMethodName();
    }

    private static JsonNode getResponseTemplate() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = "";
        String fileName = "response.json";
        String path = FILE_UPLOAD_PATH + "/" + fileName;
        File template = new File(path);
        // Read JSON file and return as JsonNode
        try (Scanner reader = new Scanner(template)) {
            while (reader.hasNextLine()) {
                content += reader.nextLine();
            }
            reader.close();
            return objectMapper.readTree(content);
        }
    }
}
