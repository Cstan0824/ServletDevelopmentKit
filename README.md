# Servlet Development Kit

A lightweight, annotation-driven Java web framework built on top of Jakarta Servlets, providing MVC architecture, middleware support, and modern web development features.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Core Components](#core-components)
- [Controllers](#controllers)
- [Annotations](#annotations)
- [Middleware](#middleware)
- [Data Access](#data-access)
- [Views and JSP](#views-and-jsp)
- [Error Handling](#error-handling)
- [Configuration](#configuration)
- [API Examples](#api-examples)
- [Best Practices](#best-practices)
- [Contributing](#contributing)

## Overview

The Servlet Development Kit is a modern Java web framework that simplifies web application development by providing:

- **MVC Architecture**: Clean separation of concerns with Model-View-Controller pattern
- **Annotation-Based Routing**: Simple HTTP method and route definitions
- **Middleware Support**: Extensible request/response processing pipeline
- **Multi-format Support**: JSON, HTML, file uploads, and custom content types
- **Built-in Security**: Authorization and session management
- **Audit Logging**: Comprehensive request tracking and error logging

## Features

### 🚀 Core Features
- **Annotation-driven routing** with `@HttpRequest`
- **Middleware pipeline** for cross-cutting concerns
- **Multi-format responses** (JSON, HTML, Files, Custom)
- **Parameter mapping** from JSON, form data, and query parameters
- **Session management** with helper utilities
- **File upload support** with configurable limits
- **Built-in error handling** and custom error pages
- **Audit logging** for monitoring and debugging

### 🔧 Technical Features
- **Thread-safe operations** using ThreadLocal variables
- **Reflection-based method invocation** for dynamic routing
- **JSON serialization/deserialization** with Jackson
- **JSP view rendering** with parameter passing
- **Custom content type support**
- **Configurable file handling**

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   HTTP Request  │───▶│  HttpBase Filter │───▶│   Controller    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                               │                         │
                               ▼                         ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │   Middleware     │    │     Result      │
                       │   Pipeline       │    │   (Response)    │
                       └──────────────────┘    └─────────────────┘
                               │                         │
                               ▼                         ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │  Authorization   │    │   View/JSON/    │
                       │  Audit Logging   │    │   File Output   │
                       └──────────────────┘    └─────────────────┘
```

## Getting Started

### Prerequisites
- Java 8 or higher
- Jakarta EE compatible servlet container (Tomcat 10+, Jetty, etc.)
- Maven 3.6+

### Basic Setup

1. **Create a Controller**:
```java
package Controllers;

import mvc.ControllerBase;
import mvc.Result;
import mvc.Annotations.HttpRequest;
import mvc.Http.HttpMethod;

public class HomeController extends ControllerBase {
    
    @HttpRequest(HttpMethod.GET)
    public Result index() throws Exception {
        return page(); // Returns /Home/index.jsp
    }
    
    @HttpRequest(HttpMethod.POST)
    public Result api() throws Exception {
        return json("Hello World!");
    }
}
```

2. **Create corresponding JSP view** at `/src/main/webapp/Views/Home/index.jsp`:
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Home Page</title>
</head>
<body>
    <h1>Welcome to Servlet Development Kit!</h1>
</body>
</html>
```

3. **Deploy and access**:
   - GET `/Home/index` → Renders JSP view
   - POST `/Home/api` → Returns JSON response

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   ├── Controllers/           # MVC Controllers
│   │   │   ├── LandingController.java
│   │   │   └── SampleController.java
│   │   ├── DAO/                   # Data Access Objects
│   │   ├── DTO/                   # Data Transfer Objects
│   │   ├── Models/                # Entity Models
│   │   └── mvc/                   # Framework Core
│   │       ├── ApplicationContext.java
│   │       ├── ControllerBase.java
│   │       ├── Result.java
│   │       ├── Annotations/       # Custom Annotations
│   │       ├── App/              # Application Lifecycle
│   │       ├── Exceptions/       # Custom Exceptions
│   │       ├── Helpers/          # Utility Classes
│   │       └── Http/             # HTTP Handling
│   ├── resources/
│   │   └── META-INF/
│   │       └── persistence.xml
│   └── webapp/
│       ├── Content/              # Static Resources
│       ├── Views/                # JSP Views
│       └── WEB-INF/
│           └── web.xml
```

## Core Components

### HttpBase
The foundation class that handles HTTP requests and routing:

```java
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public abstract class HttpBase extends HttpServlet
```

**Key Features**:
- Thread-safe request/response handling
- Automatic parameter mapping
- Middleware execution pipeline
- Multi-format response support

### ControllerBase
Base class for all application controllers:

```java
public class ControllerBase extends HttpBase {
    // Provides convenient methods for:
    // - Page rendering
    // - JSON responses
    // - File handling
    // - Error responses
    // - Session management
}
```

### Result
Response wrapper that encapsulates different response types:

```java
public class Result {
    private Object data;
    private String redirectPath;
    private boolean isRedirect;
    private String contentType;
    private HttpStatusCode statusCode;
    private Map<String, String> headers;
    private JsonNode params;
}
```

## Controllers

Controllers handle HTTP requests and return responses. All controllers extend `ControllerBase`.

### Basic Controller Example

```java
package Controllers;

import mvc.ControllerBase;
import mvc.Result;
import mvc.Annotations.HttpRequest;
import mvc.Http.HttpMethod;

public class ProductController extends ControllerBase {
    
    // GET /Product/list
    @HttpRequest(HttpMethod.GET)
    public Result list() throws Exception {
        List<Product> products = productService.getAllProducts();
        return json(products);
    }
    
    // POST /Product/create
    @HttpRequest(HttpMethod.POST)
    public Result create(Product product) throws Exception {
        Product created = productService.createProduct(product);
        return json(created, HttpStatusCode.CREATED, "Product created successfully");
    }
    
    // GET /Product/view
    public Result view() throws Exception {
        return page(); // Returns /Product/view.jsp
    }
}
```

### Response Types

#### 1. Page Responses
```java
// Render JSP view
return page(); // Uses action name
return page("customView"); // Custom view name
return page("customView", "CustomController"); // Custom controller/view
```

#### 2. JSON Responses
```java
// Simple JSON
return json(data);
return json(data, HttpStatusCode.OK, "Success message");

// Success/Error shortcuts
return success("Operation completed");
return error("Something went wrong");
```

#### 3. File Responses
```java
// File download
byte[] fileData = getFileData();
return file(fileData, "document.pdf", FileType.PDF);

// Image source (for inline display)
return source(imageData, "image.png", FileType.PNG);
```

#### 4. Custom Content
```java
return content(data, "text/plain", HttpStatusCode.OK);
```

### Parameter Mapping

The framework automatically maps request parameters to method parameters:

```java
// From JSON body
@HttpRequest(HttpMethod.POST)
public Result createUser(User user) throws Exception {
    // user object populated from JSON request body
    return json(userService.create(user));
}

// From query parameters
@HttpRequest(HttpMethod.GET)
public Result search(String query, int page, int size) throws Exception {
    // Parameters mapped from ?query=...&page=...&size=...
    return json(searchService.search(query, page, size));
}

// From form data
@HttpRequest(HttpMethod.POST)
public Result upload(String title, Part file) throws Exception {
    // Handle multipart form data
    return success("File uploaded successfully");
}
```

## Annotations

### @HttpRequest
Defines HTTP method for controller actions:

```java
@HttpRequest(HttpMethod.GET)    // GET request
@HttpRequest(HttpMethod.POST)   // POST request
@HttpRequest(HttpMethod.PUT)    // PUT request
@HttpRequest(HttpMethod.DELETE) // DELETE request
```

**Default**: If no annotation is present, defaults to GET.

### @Authorization
Implements access control:

```java
@Authorization(accessUrls = "admin,manager")
public Result adminPanel() throws Exception {
    return page();
}

@Authorization(accessUrls = "*") // Allow all authenticated users
public Result dashboard() throws Exception {
    return page();
}
```

### @ActionAttribute
For custom metadata (framework extensible):

```java
@ActionAttribute(key = "cache", value = "true")
public Result cachedEndpoint() throws Exception {
    return json(expensiveOperation());
}
```

## Middleware

Middleware provides cross-cutting functionality that executes before/after controller actions.

### Creating Custom Middleware

```java
package mvc.Annotations;

import mvc.Http.HttpContext;

public class LoggingHandler implements Middleware {
    
    @Override
    public void executeBeforeAction(HttpContext context) {
        // Log request details
        System.out.println("Request: " + context.getRequest().getRequestURI());
    }
    
    @Override
    public void executeAfterAction(HttpContext context) {
        // Log response details
        System.out.println("Response sent");
    }
    
    @Override
    public void onError(HttpContext context, Exception ex) {
        // Handle errors
        System.err.println("Error occurred: " + ex.getMessage());
    }
}
```

### Registering Middleware

In `ApplicationContext.java`:

```java
@Override
public void initialize(ServletContextEvent sce) {
    // Register built-in middleware
    HttpBase.addMiddleware(new AuthorizationHandler());
    HttpBase.addMiddleware(new AuditService());
    
    // Register custom middleware
    HttpBase.addMiddleware(new LoggingHandler());
}
```

### Built-in Middleware

#### AuthorizationHandler
- Handles `@Authorization` annotations
- Validates user permissions
- Redirects unauthorized users

#### AuditService
- Logs all HTTP requests
- Tracks API usage
- Stores audit data to database

## Data Access

### DAO Pattern
Data Access Objects handle database operations:

```java
package DAO;

public class UserDAO extends DataAccess {
    
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        return query(User.class, sql);
    }
    
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return queryFirst(User.class, sql, id);
    }
    
    public void createUser(User user) {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
        execute(sql, user.getName(), user.getEmail());
    }
}
```

### DTO Pattern
Data Transfer Objects for API communication:

```java
package DTO;

public class UserDTO {
    private String name;
    private String email;
    private String role;
    
    // Getters and setters
}
```

## Views and JSP

### JSP View Structure
```
Views/
├── Error/
│   ├── internalError.jsp
│   ├── notFound.jsp
│   └── unauthorized.jsp
├── Landing/
│   └── index.jsp
└── Sample/
    └── index.jsp
```

### Passing Data to Views

```java
// In Controller
@HttpRequest(HttpMethod.GET)
public Result userProfile(int userId) throws Exception {
    User user = userService.getUserById(userId);
    
    ObjectMapper mapper = new ObjectMapper();
    JsonNode params = mapper.createObjectNode();
    ((ObjectNode) params).put("user", JsonConverter.serialize(user));
    
    return page("profile", params);
}
```

```jsp
<!-- In JSP -->
<%@ page import="com.fasterxml.jackson.databind.JsonNode" %>
<%@ page import="mvc.Helpers.JsonConverter" %>

<%
    JsonNode params = (JsonNode) request.getAttribute("params");
    if (params != null) {
        User user = JsonConverter.deserialize(params.get("user").asText(), User.class);
%>
    <h1>Welcome, <%= user.getName() %>!</h1>
<%
    }
%>
```

## Error Handling

### Built-in Error Pages
- **404 Not Found**: `/Views/Error/notFound.jsp`
- **401 Unauthorized**: `/Views/Error/unauthorized.jsp`
- **500 Internal Error**: `/Views/Error/internalError.jsp`

### Custom Error Handling

```java
// In Controller
public Result riskyOperation() throws Exception {
    try {
        // Some operation that might fail
        return success("Operation completed");
    } catch (BusinessException ex) {
        return error(HttpStatusCode.BAD_REQUEST, ex.getMessage());
    } catch (Exception ex) {
        logger.log(Level.SEVERE, "Unexpected error", ex);
        return error(HttpStatusCode.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}
```

### Global Error Configuration
Set environment variables for error page URLs:
- `NOT_FOUND_PAGE`: Path to 404 error page
- `UNAUTHORIZED_PAGE`: Path to 401 error page  
- `INTERNAL_ERROR_PAGE`: Path to 500 error page

## Configuration

### Environment Variables
```bash
# Error page URLs
NOT_FOUND_PAGE=/web/Error/notFound
UNAUTHORIZED_PAGE=/web/Error/unauthorized
INTERNAL_ERROR_PAGE=/web/Error/internalError

# File upload configuration
FILE_UPLOAD_PATH=/path/to/upload/directory
```

### Web.xml Configuration
```xml
<!DOCTYPE web-app PUBLIC
 "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
 "http://java.sun.com/dtd/web-app_2_3.dtd" >

<web-app>
  <display-name>Servlet Development Kit Application</display-name>
  
  <!-- Additional servlet configurations -->
</web-app>
```

### Maven Dependencies
Key dependencies in `pom.xml`:
- Jakarta Servlet API
- Jackson for JSON processing
- JUnit for testing

## API Examples

### RESTful API Example

```java
package Controllers;

import mvc.ControllerBase;
import mvc.Result;
import mvc.Annotations.HttpRequest;
import mvc.Annotations.Authorization;
import mvc.Http.HttpMethod;

public class ApiController extends ControllerBase {
    
    @HttpRequest(HttpMethod.GET)
    public Result users() throws Exception {
        List<User> users = userService.getAllUsers();
        return json(users);
    }
    
    @HttpRequest(HttpMethod.POST)
    @Authorization(accessUrls = "admin")
    public Result createUser(User user) throws Exception {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return error(HttpStatusCode.BAD_REQUEST, "Name is required");
        }
        
        User created = userService.createUser(user);
        return json(created, HttpStatusCode.CREATED, "User created successfully");
    }
    
    @HttpRequest(HttpMethod.PUT)
    @Authorization(accessUrls = "admin,manager")
    public Result updateUser(int id, User user) throws Exception {
        User updated = userService.updateUser(id, user);
        return json(updated, HttpStatusCode.OK, "User updated successfully");
    }
    
    @HttpRequest(HttpMethod.DELETE)
    @Authorization(accessUrls = "admin")
    public Result deleteUser(int id) throws Exception {
        userService.deleteUser(id);
        return success("User deleted successfully");
    }
}
```

### File Upload Example

```java
@HttpRequest(HttpMethod.POST)
public Result uploadFile(String description, Part file) throws Exception {
    if (file == null || file.getSize() == 0) {
        return error("No file provided");
    }
    
    // Validate file type
    String contentType = file.getContentType();
    if (!contentType.startsWith("image/")) {
        return error("Only image files are allowed");
    }
    
    // Save file
    String fileName = fileService.saveFile(file);
    
    return json(Map.of(
        "fileName", fileName,
        "size", file.getSize(),
        "contentType", contentType
    ), HttpStatusCode.OK, "File uploaded successfully");
}
```

### Session Management Example

```java
@HttpRequest(HttpMethod.POST)
public Result login(String username, String password) throws Exception {
    User user = authService.authenticate(username, password);
    
    if (user != null) {
        SessionHelper session = getSessionHelper();
        session.setUsername(user.getUsername());
        session.setRole(user.getRole());
        session.setAuthenticated(true);
        session.setAccessUrls(user.getPermissions());
        
        return success("Login successful");
    } else {
        return error(HttpStatusCode.UNAUTHORIZED, "Invalid credentials");
    }
}

@HttpRequest(HttpMethod.POST)
@Authorization(accessUrls = "*")
public Result logout() throws Exception {
    SessionHelper session = getSessionHelper();
    session.setAuthenticated(false);
    request.getSession().invalidate();
    
    return success("Logout successful");
}
```

## Best Practices

### 1. Controller Design
- Keep controllers thin - delegate business logic to services
- Use appropriate HTTP methods and status codes
- Validate input parameters
- Handle exceptions gracefully

### 2. Security
- Always use `@Authorization` for protected endpoints
- Validate and sanitize user inputs
- Implement proper session management
- Use HTTPS in production

### 3. Performance
- Use appropriate middleware for caching
- Implement pagination for large datasets
- Optimize database queries in DAOs
- Use appropriate response formats (JSON vs HTML)

### 4. Error Handling
- Provide meaningful error messages
- Log errors appropriately
- Use proper HTTP status codes
- Implement global error handling

### 5. Code Organization
- Follow package naming conventions
- Separate concerns (Controller, Service, DAO)
- Use DTOs for API communication
- Keep views simple and focused

## Contributing

### Development Setup
1. Clone the repository
2. Import into your IDE as a Maven project
3. Configure your servlet container
4. Set up environment variables
5. Run tests: `mvn test`
6. Deploy: `mvn package`

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Write unit tests for new features

### Submitting Changes
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write/update tests
5. Submit a pull request

---

**Version**: 1.0-SNAPSHOT  
**License**: [Specify your license]  
**Author**: [Your name/organization]

For more information, examples, or support, please refer to the source code documentation or create an issue in the repository.
