package mvc.Helpers;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import DTO.UserSession;
import jakarta.servlet.http.HttpSession;
import mvc.Helpers.Audits.AuditService;

public class SessionHelper {
    // Core session properties
    private UserSession userSession;

    // Session key
    private static final String USER_SESSION_KEY = "USER_DETAILS";

    private HttpSession session;
    private Logger logger = AuditService.getLogger();
    private ObjectMapper objectMapper = new ObjectMapper();

    public SessionHelper() {
        this.userSession = new UserSession(); // Initialize empty userSession
    }

    public SessionHelper(HttpSession session) {
        this.session = session;
        try {
            // Try to load from session first
            Object sessionObj = session.getAttribute(USER_SESSION_KEY);

            if (sessionObj instanceof UserSession) {
                // Direct UserSession object found
                this.userSession = (UserSession) sessionObj;
            } else if (sessionObj != null) {
                // Try to parse from JSON representation
                JsonNode root = objectMapper.readTree(sessionObj.toString());

                // Create UserSession object from JSON data
                UserSession newUserSession = new UserSession();
                newUserSession.setId(root.path("id").asInt());
                newUserSession.setUsername(root.path("username").asText(null));
                newUserSession.setEmail(root.path("email").asText(null));
                newUserSession.setRole(root.path("role").asText("0"));
                newUserSession.setAuthenticated(root.path("isAuthenticated").asBoolean(false));

                // Handle imageId if present
                if (root.has("imageId")) {
                    newUserSession.setImageId(root.path("imageId").asInt());
                }

                // Handle permissions - convert to List<String> for accessUrls
                String permissionsStr = root.path("permissions").asText("");
                if (permissionsStr != null && !permissionsStr.isEmpty()) {
                    List<String> accessUrls = Arrays.asList(permissionsStr.split(","));
                    newUserSession.setAccessUrls(accessUrls);
                } else {
                    newUserSession.setAccessUrls(new ArrayList<>());
                }

                this.userSession = newUserSession;
                session.setAttribute(USER_SESSION_KEY, newUserSession);
            } else {
                // Create a new empty UserSession if nothing was found
                this.userSession = new UserSession();
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading session data: " + ex.getMessage());
            this.userSession = new UserSession(); // Create empty userSession on error
        }
    }

    /**
     * Store the UserSession object in the session
     * 
     * @param userSession The UserSession object to store
     */
    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
        if (userSession != null && session != null) {
            session.setAttribute(USER_SESSION_KEY, userSession);
            updateJsonRepresentation();
        }
    }

    /**
     * Retrieve the stored UserSession object
     * 
     * @return The UserSession object or null if not found
     */
    public UserSession getUserSession() {
        return this.userSession;
    }

    // Update the JSON representation based on the UserSession object
    private void updateJsonRepresentation() {
        if (this.userSession != null && session != null) {
            try {
                ObjectNode json = objectMapper.createObjectNode();
                json.put("id", this.userSession.getId());
                json.put("username", this.userSession.getUsername());
                json.put("email", this.userSession.getEmail());
                json.put("role", this.userSession.getRole());
                json.put("isAuthenticated", this.userSession.isAuthenticated());

                // Handle imageId if present
                if (this.userSession.getImageId() != null) {
                    json.put("imageId", this.userSession.getImageId());
                }

                // Handle accessUrls/permissions
                if (this.userSession.getAccessUrls() != null && !this.userSession.getAccessUrls().isEmpty()) {
                    // Join accessUrls with comma
                    json.put("permissions", String.join(",", this.userSession.getAccessUrls()));
                } else {
                    json.put("permissions", "");
                }

                session.setAttribute(USER_SESSION_KEY, this.userSession);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error updating JSON representation: " + ex.getMessage());
            }
        }
    }

    // Getters and setters that directly use the UserSession object
    public int getId() {
        return userSession != null ? userSession.getId() : -1;
    }

    public String getUsername() {
        return userSession != null ? userSession.getUsername() : null;
    }

    public void setUsername(String username) {
        if (userSession != null) {
            userSession.setUsername(username);
            updateJsonRepresentation();
        }
    }

    public String getEmail() {
        return userSession != null ? userSession.getEmail() : null;
    }

    public void setEmail(String email) {
        if (userSession != null) {
            userSession.setEmail(email);
            updateJsonRepresentation();
        }
    }

    public String getRole() {
        return userSession != null ? userSession.getRole() : "0";
    }

    public void setRole(String role) {
        if (userSession != null && role != null) {
            userSession.setRole(role);
            updateJsonRepresentation();
        }
    }

    public List<String> getAccessUrls() {
        return userSession != null ? userSession.getAccessUrls() : new ArrayList<>();
    }

    public void setAccessUrls(List<String> accessUrls) {
        if (userSession != null) {
            userSession.setAccessUrls(accessUrls);
            updateJsonRepresentation();
        }
    }

    public Integer getImageId() {
        return userSession != null ? userSession.getImageId() : null;
    }

    public void setImageId(Integer imageId) {
        if (userSession != null) {
            userSession.setImageId(imageId);
            updateJsonRepresentation();
        }
    }

    public boolean isAuthenticated() {
        return userSession != null && userSession.isAuthenticated();
    }

    public void setAuthenticated(boolean isAuthenticated) {
        if (userSession != null) {
            userSession.setAuthenticated(isAuthenticated);
            updateJsonRepresentation();
        }
    }

    // Generic session attribute helpers
    public String getKey() {
        return USER_SESSION_KEY;
    }

    public String get(String key) {
        return session != null && session.getAttribute(key) != null ? session.getAttribute(key).toString() : null;
    }

    public void set(String key, String value) {
        set(key, value, null);
    }

    public void set(String key, String value, Timestamp timestamp) {
        if (session != null) {
            if (timestamp != null) {
                try {
                    ObjectNode json = objectMapper.createObjectNode();
                    json.put("value", value);
                    json.put("timestamp", timestamp.toString());
                    value = JsonConverter.serialize(json);
                } catch (JsonProcessingException e) {
                    logger.log(Level.WARNING, "Error in set method: " + e.getMessage());
                }
            }
            session.setAttribute(key, value);
        }
    }

    public void remove(String key) {
        if (session != null) {
            session.removeAttribute(key);
        }
    }

    public String getSessionID() {
        return session != null ? session.getId() : null;
    }

    public void remove() {
        if (session != null) {
            session.removeAttribute(USER_SESSION_KEY);
        }
        this.userSession = new UserSession(); // Reset to empty
    }

    public void clear() {
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                // Session may already be invalidated
                logger.log(Level.FINE, "Session already invalidated: " + e.getMessage());
            }
        }
        this.userSession = new UserSession(); // Reset to empty
    }
}