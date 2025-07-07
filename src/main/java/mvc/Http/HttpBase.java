package mvc.Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Array;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mvc.Annotations.ActionAttribute;
import mvc.Annotations.HttpRequest;
import mvc.Annotations.Middleware;
import mvc.Exceptions.InvalidActionResultException;
import mvc.Exceptions.PageNotFoundException;
import mvc.FileType;
import mvc.Helpers.Audits.AuditService;
import mvc.Helpers.JsonConverter;
import mvc.Result;

/*
    * ControllerBase class is the base class for all controllers in the application.
    * It handles the request and response, and invokes the appropriate action method.
    * It also handles the middleware execution before and after the action method.
    REMARKS:
        The Actions inside ControllerBase's child class should be annotated with capable to support 
        multipart/form-data, application/json, and query parameters.
 */

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50)
public abstract class HttpBase extends HttpServlet {
    protected static final ThreadLocal<HttpServletRequest> threadLocalRequest = new ThreadLocal<>();
    protected static final ThreadLocal<HttpServletResponse> threadLocalResponse = new ThreadLocal<>();
    protected static final ThreadLocal<HttpContext> threadLocalContext = new ThreadLocal<>();

    protected static HttpServletRequest request;
    protected static HttpServletResponse response;
    protected static HttpContext context;

    protected Logger logger = AuditService.getLogger();
    private static List<Middleware> middlewares;

    private static final String DEFAULT_CONTROLLER = "LandingController";
    private static final String DEFAULT_ACTION = "index";
    private static final String NOT_FOUND_URL = System.getenv("NOT_FOUND_PAGE");
    private static final String INTERNAL_ERROR_URL = System.getenv("INTERNAL_ERROR_PAGE");

    // #region SERVER RESPONSE METHODS
    protected abstract Result page() throws Exception;

    protected abstract Result page(String action) throws Exception;

    protected abstract Result page(String action, String controller) throws Exception;

    protected abstract Result page(String action, String controller, JsonNode params) throws Exception;

    protected abstract Result json(Object data) throws Exception;

    protected abstract Result json(Object data, HttpStatusCode status, String message) throws Exception;

    protected abstract Result content(Object data, String contentType) throws Exception;

    protected abstract Result content(Object data, String contentType, HttpStatusCode status) throws Exception;

    protected abstract Result file(byte[] bytes) throws Exception;

    protected abstract Result file(byte[] bytes, String fileName) throws Exception;

    protected abstract Result file(byte[] bytes, String fileName, FileType fileType) throws Exception;

    protected abstract Result source(byte[] bytes, String fileName, FileType fileType) throws Exception;

    protected abstract Result success() throws Exception;

    protected abstract Result success(String message) throws Exception;

    protected abstract Result success(HttpStatusCode status, String message) throws Exception;

    protected abstract Result error() throws Exception;

    protected abstract Result error(String message) throws Exception;

    protected abstract Result error(HttpStatusCode status, String message) throws Exception;
    // #endregion

    public HttpBase() {

    }

    public static void addMiddleware(Middleware middleware) {
        // Add Middleware
        if (middlewares == null) {
            middlewares = new ArrayList<>();
        }
        middlewares.add(middleware);
    }

    protected static String getDefaultController() {
        return DEFAULT_CONTROLLER;
    }

    protected static String getDefaultAction() {
        return DEFAULT_ACTION;
    }

    // #region OVERRIDE METHODS[HttpServlet]
    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) {
        Method action = null;
        try {
            // Set ThreadLocal variables first to ensure thread safety
            threadLocalContext.set(new HttpContext(req, res));
            threadLocalRequest.set(req);
            threadLocalResponse.set(res);

            request = threadLocalRequest.get();
            response = threadLocalResponse.get();
            context = threadLocalContext.get();

            // Create and initialize a new HttpContext for this request
            HttpContext localContext = new HttpContext();
            localContext.setRequest(req);
            localContext.setResponse(res);
            threadLocalContext.set(localContext);

            action = getCurrentAction();
            invokeMethod(action);
        } catch (Exception e) {
            // Logging service
            logger.log(Level.WARNING,
                    "Error throws at [service(HttpServletRequest req, HttpServletResponse res)]: " + e.getMessage());
            e.printStackTrace(System.err);
            if (action == null) {
                return;
            }
            try {
                executeMiddleware(action.getAnnotations(), MiddlewareAction.OnError, e);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error throws at [service(HttpServletRequest req, HttpServletResponse res)]: "
                        + e.getMessage());
            }
        } finally {
            // Critical: Clean up ThreadLocal variables to prevent memory leaks
            threadLocalRequest.remove();
            threadLocalResponse.remove();
            threadLocalContext.remove();
        }
    }
    // #endregion

    // #region Process Request and Middleware
    private Method getCurrentAction() throws PageNotFoundException, IOException {
        HttpServletRequest localRequest = threadLocalRequest.get();
        HttpServletResponse localResponse = threadLocalResponse.get();

        String action = localRequest.getPathInfo();
        String httpMethod = localRequest.getMethod().toUpperCase();
        if (action == null)
            action = "";

        if (action.equals("") || action.equals("/")) {
            action = DEFAULT_ACTION;
        } else {
            action = action.replaceFirst("/", "");
        }

        if (httpMethod.equals("")) {
            httpMethod = "GET";
        }

        // Retrieve current Controller's actions
        Method[] methods = this.getClass().getDeclaredMethods();
        if (methods.length == 0) {
            // redirect to error page
            try {
                localResponse.setStatus(HttpStatusCode.NOT_FOUND.get());
                localResponse.sendRedirect(NOT_FOUND_URL);
                // show user requested url
                throw new PageNotFoundException(
                        "No action found for the requested URL: " + localRequest.getRequestURI());
            } catch (IOException | PageNotFoundException e) {
                logger.log(Level.WARNING, "Error throws at [getCurrentAction()]: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }

        // Loop for the Controller Class Actions until the action is found
        for (Method method : methods) {
            // skip if not public method
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            // Compare by action name
            String methodName = method.getName();
            ActionAttribute actionName = method.getAnnotation(ActionAttribute.class);
            if (actionName != null)
                methodName = actionName.urlPattern().replaceAll("[^a-zA-Z0-9/_]", "").replace(" ", "");

            if (!methodName.equals(action))
                continue;

            // Compare by Http Method
            HttpRequest httpRequest = method.getAnnotation(HttpRequest.class);
            if (httpRequest != null) {
                // Check if the Http method is the same as the API method
                if (!httpRequest.value().get().equals(httpMethod)) {
                    continue;
                }
            } else {
                // Default to GET API
                if (!httpMethod.equals("GET"))
                    continue;
            }
            return method;
        }
        // Action Not Found
        localResponse.setStatus(HttpStatusCode.NOT_FOUND.get());
        localResponse.sendRedirect(NOT_FOUND_URL);
        // show user requested url
        throw new PageNotFoundException(
                "No action found for the requested URL: " + localRequest.getRequestURI());
    }

    private void invokeMethod(Method action) throws Exception {
        HttpContext localContext = threadLocalContext.get();
        HttpServletResponse localResponse = threadLocalResponse.get();
        HttpServletRequest localRequest = threadLocalRequest.get();

        // Register Services
        Annotation[] annotations = action.getAnnotations();

        // Parameters Mapping - do mapping based on the request type
        Object[] args = parameterMapping(action);

        // #region execute Middlewares and action
        executeMiddleware(annotations, MiddlewareAction.BeforeAction);
        if (localContext.isRequestCancelled()) {
            if (!localContext.getResult().isRedirect()) {
                return;
            }
            localContext.setRequestCancelled(false);
            localContext.getResult().setRedirect(false);

            System.out.println("Http Status Code: " + localContext.getResult().getStatusCode().get());
            localResponse.setStatus(localContext.getResult().getStatusCode().get());
            localResponse.sendRedirect(localContext.getResult().getPath());
            return;
        }

        Object result = action.invoke(this, args);
        if (result instanceof Result actionResult) {
            // need to declare response header
            localResponse.setContentType(actionResult.getContentType());
            localResponse.setStatus(actionResult.getStatusCode().get());
            localResponse.setCharacterEncoding(actionResult.getCharset());
            // setHeaders
            for (Entry<String, String> header : actionResult.getHeaders().entrySet()) {
                localResponse.setHeader(header.getKey(), header.getValue());
            }
            executeMiddleware(annotations, MiddlewareAction.AfterAction);

            // Set headers to prevent caching from browser
            switch (actionResult.getContentType()) {
                case "application/json" ->
                    localResponse.getWriter().write(JsonConverter.serialize(actionResult.getData()));
                case "text/html" -> {
                    if (actionResult.isRedirect()) {
                        String path = "/web" + actionResult.getPath().replace(".jsp", "");
                        JsonNode params = actionResult.getParams();

                        // Write the params as url query string
                        if (params != null && params.isObject()) {
                            String query = urlQueryStringBuilder(params);
                            path += query.toString();
                        }

                        localResponse.sendRedirect(path);
                    } else {
                        // return current page
                        localRequest.getRequestDispatcher("/Views" + actionResult.getPath()).forward(
                                localRequest,
                                localResponse);
                    }
                }
                default -> {
                    if (FileType.contains(actionResult.getContentType())) {
                        streamFileContent(actionResult.getData());
                    } else {
                        localResponse.getWriter().write((actionResult.getData().toString()));
                    }
                }
            }
        } else {
            try {
                localResponse.setStatus(HttpStatusCode.NOT_FOUND.get());
                localResponse.sendRedirect(INTERNAL_ERROR_URL);
                // show user requested url
                throw new InvalidActionResultException("Invalid Request");

            } catch (IOException | PageNotFoundException e) {
                logger.log(Level.WARNING, "Error throws at [getCurrentAction()]: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        // #endregion
    }

    private String urlQueryStringBuilder(JsonNode params) {
        StringBuilder queryString = new StringBuilder();
        Iterator<Map.Entry<String, JsonNode>> fields = params.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = URLEncoder.encode(field.getKey(), StandardCharsets.UTF_8);
            String value = URLEncoder.encode(field.getValue().asText(), StandardCharsets.UTF_8);
            if (queryString.length() == 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            queryString.append(key).append("=").append(value);
        }
        return queryString.toString();
    }

    private void executeMiddleware(Annotation[] annotations, MiddlewareAction action, Exception ex) throws Exception {
        HttpContext localContext = threadLocalContext.get();

        for (Annotation annotation : annotations) {
            String annotationName = annotation.annotationType().getSimpleName();
            String expectedHandlerName = annotationName + "Handler";
            // Check if the annotation is registered inside Handler Object List
            for (Middleware middleware : middlewares) {
                if (!middleware.getClass().getSimpleName().equals(expectedHandlerName))
                    continue;

                // get all variables from Annotation and set the value to Handler object
                for (Method method : annotation.annotationType().getDeclaredMethods()) {
                    String paramName = method.getName();
                    Object paramValue = method.invoke(annotation);

                    Field field = middleware.getClass().getDeclaredField(paramName);

                    // Assign value to Handler Object
                    field.setAccessible(true); // public for temporary
                    field.set(middleware, paramValue);
                    field.setAccessible(false); // set back to private
                }

                switch (action) {
                    case BeforeAction -> middleware.executeBeforeAction(localContext);
                    case AfterAction -> middleware.executeAfterAction(localContext);
                    case OnError -> middleware.onError(localContext, ex);
                }
                break;
            }
        }
    }

    private void executeMiddleware(Annotation[] annotations, MiddlewareAction action) throws Exception {
        executeMiddleware(annotations, action, null);
    }

    private void streamFileContent(Object data) throws Exception {
        HttpServletResponse localResponse = threadLocalResponse.get();

        if (localResponse == null) {
            throw new IllegalStateException("Thread-local response is not available");
        }

        if (localResponse.isCommitted()) {
            throw new IllegalStateException("Response is already committed, cannot stream file.");
        }

        try {
            OutputStream output = localResponse.getOutputStream();
            if (output == null) {
                System.err.println("Error: OutputStream is null");
                throw new IllegalStateException("OutputStream is not available.");
            }

            if (data == null) {
                System.err.println("Error: Data is null");
                throw new IllegalArgumentException("Data is null.");
            }

            if (data instanceof byte[] bytes) {
                int chunkSize = 512 * 1024; // 512KB chunks (smaller for better handling)
                int totalLength = bytes.length;
                int offset = 0;

                while (offset < totalLength) {
                    int length = Math.min(chunkSize, totalLength - offset);
                    output.write(bytes, offset, length);
                    offset += length;
                    output.flush(); // Ensure the data is written in chunks
                    Thread.yield(); // Give other threads a chance to run
                }
            } else {
                System.err.println("Error: Data is not byte array.");
                throw new IllegalArgumentException("Expected byte[] for file data.");
            }
        } catch (IOException e) {
            System.err.println("IOException in streamFileContent: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error while streaming file content", e);
        }
    }
    // #endregion

    // #region PARAMETER MAPPING
    private Object[] parameterMapping(Method action) throws Exception {
        HttpServletRequest localRequest = threadLocalRequest.get();

        String contentType = localRequest.getContentType();
        String httpMethod = localRequest.getMethod();

        // validate with request method and content_type
        if (httpMethod.equals("GET"))
            return mapFromQueryParams(action);

        // POST, DELETE, PUT, PATCH
        if ("application/json".equalsIgnoreCase(contentType)) {
            return mapFromJson(action);
        }
        // multipart/form-data
        return mapFromFormData(action);
    }

    private Object[] mapFromFormData(Method action) throws Exception {
        HttpServletRequest localRequest = threadLocalRequest.get();

        Object[] formData = new Object[action.getParameters().length];
        Parameter[] parameters = action.getParameters(); // Get method parameters
        int count = 0;

        for (Parameter parameter : parameters) {
            String paramName = parameter.getName();
            Class<?> paramType = parameter.getType();
            Object convertedValue = getDefaultValue(paramType);

            if (paramType.isArray()) {
                // Handle arrays
                Class<?> componentType = paramType.getComponentType();

                if (componentType.equals(byte[].class)) {
                    // Handle file array (List of byte[])
                    List<byte[]> fileList = new ArrayList<>();
                    for (Part part : localRequest.getParts()) {
                        if (part.getName().equals(paramName) && part.getSubmittedFileName() != null) {
                            fileList.add(part.getInputStream().readAllBytes());
                        }
                    }
                    convertedValue = fileList.toArray(byte[][]::new); // a.k.a fileList.toArray(new byte[0][]);
                } else {
                    // Handle text input array
                    String[] paramValues = localRequest.getParameterValues(paramName);
                    if (paramValues != null) {
                        Object[] convertedArray = (Object[]) java.lang.reflect.Array.newInstance(componentType,
                                paramValues.length);
                        for (int i = 0; i < paramValues.length; i++) {
                            Object value = (paramValues[i] != null) ? convertType(paramValues[i], componentType)
                                    : getDefaultValue(componentType);
                            convertedArray[i] = value;
                        }
                        convertedValue = convertedArray;
                    }
                }

            } else {
                // Handle text or file input
                Part part = localRequest.getPart(paramName);
                if (part != null) {
                    if (part.getSubmittedFileName() != null) {
                        convertedValue = part.getInputStream().readAllBytes();
                    } else {
                        convertedValue = convertType(new String(part.getInputStream().readAllBytes()), paramType);
                    }
                }
            }
            formData[count++] = convertedValue;
        }
        return formData;
    }

    private Object[] mapFromQueryParams(Method action) throws Exception {
        HttpServletRequest localRequest = threadLocalRequest.get();

        Object[] queryParams = new Object[action.getParameters().length];
        Parameter[] parameters = action.getParameters(); // Get method parameters
        int count = 0;

        for (Parameter parameter : parameters) {
            String paramName = parameter.getName();
            Class<?> paramType = parameter.getType();

            if (paramType.isArray()) {
                // Handle arrays: get all values as String[]
                String[] paramValues = localRequest.getParameterValues(paramName);
                if (paramValues != null) {
                    queryParams[count] = convertArrayType(paramValues, paramType.getComponentType());
                } else {
                    queryParams[count] = getDefaultValue(paramType);
                }
            } else {
                // Handle single value
                String paramValue = localRequest.getParameter(paramName);
                queryParams[count] = (paramValue != null) ? convertType(paramValue, paramType)
                        : getDefaultValue(paramType);
            }

            count++;
        }
        return queryParams;
    }

    private Object[] mapFromJson(Method action) throws IOException, Exception {
        Object[] args = new Object[action.getParameters().length];
        ObjectMapper objectMapper = new ObjectMapper();
        int count = 0;

        // Read JSON request body
        String jsonBody = getBody();
        JsonNode jsonNode = objectMapper.readTree(jsonBody);

        // Loop through the parameters of the controller's action
        for (Parameter param : action.getParameters()) {
            Class<?> paramType = param.getType();
            Object value;
            if (List.class.isAssignableFrom(paramType)) {
                Type actualTypeArgument = ((ParameterizedType) param.getParameterizedType())
                        .getActualTypeArguments()[0];
                value = objectMapper.convertValue(jsonNode.get(param.getName()), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, objectMapper.constructType(actualTypeArgument))); // Handle
                                                                                                               // List<Object>
                // @@DONT ASK ME WHY, IT JUST WORKS@@ ;)
            } else if (paramType.isArray()) {
                Class<?> componentType = paramType.getComponentType();
                value = objectMapper.convertValue(jsonNode.get(param.getName()),
                        objectMapper.getTypeFactory().constructArrayType(componentType)); // Handle Object[]
                // @@MAGIC CODE THAT CONVERT JSON ARRAY INTO ARRAY OF OBJECT@@
            } else if (isComplexObject(paramType)) {
                value = objectMapper.convertValue(jsonNode.get(param.getName()), paramType); // Handle single object
            } else {
                value = objectMapper.convertValue(jsonNode.get(param.getName()), paramType); // Handle primitive types
            }
            if (value == null) {
                value = getDefaultValue(paramType); // convert to default value of the param Type
            }
            args[count++] = value;
        }
        return args;
    }

    private String getBody() {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try (BufferedReader reader = threadLocalRequest.get().getReader()) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error throws at [getBody()]: " + e.getMessage());
        }
        return stringBuilder.toString();
    }
    // #endregion

    // #region HELPER METHODS
    private boolean isComplexObject(Class<?> type) {
        return !(type.isPrimitive() || type.equals(String.class) || Number.class.isAssignableFrom(type)
                || Boolean.class.equals(type));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getDefaultValue(Class<T> clazz) {
        if (clazz == boolean.class)
            return (T) Boolean.FALSE;
        else if (clazz == byte.class)
            return (T) (Byte) (byte) 0;
        else if (clazz == short.class)
            return (T) (Short) (short) 0;
        else if (clazz == int.class)
            return (T) (Integer) 0;
        else if (clazz == long.class)
            return (T) (Long) 0L;
        else if (clazz == float.class)
            return (T) (Float) 0.0f;
        else if (clazz == double.class)
            return (T) (Double) 0.0;
        else if (clazz == char.class)
            return (T) (Character) '\u0000';
        else
            return null; // Complex objects return null
    }

    private Object convertType(String value, Class<?> targetType) {

        if (targetType == String.class) {
            if (value.equalsIgnoreCase("null"))
                return null;
            return value;
        }
        if (targetType == int.class || targetType == Integer.class)
            return Integer.valueOf(value);
        if (targetType == double.class || targetType == Double.class)
            return Double.valueOf(value);
        if (targetType == boolean.class || targetType == Boolean.class)
            return Boolean.valueOf(value);
        if (targetType == long.class || targetType == Long.class)
            return Long.valueOf(value);
        if (targetType == float.class || targetType == Float.class)
            return Float.valueOf(value);
        return value; // Default: Return as a string
    }

    private Object convertArrayType(String[] values, Class<?> componentType) throws Exception {
        Object array = Array.newInstance(componentType, values.length);
        for (int i = 0; i < values.length; i++) {
            Object value = (values[i] != null) ? convertType(values[i], componentType)
                    : getDefaultValue(componentType);
            Array.set(array, i, value);
        }
        return array;
    }

    private enum MiddlewareAction {
        BeforeAction, AfterAction, OnError
    }
    // #endregion
}
