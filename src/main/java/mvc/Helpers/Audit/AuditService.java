package mvc.Helpers.Audits;

import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import Models.Users.User;
import jakarta.persistence.EntityManager;
import mvc.DataAccess;

public class AuditService {
    private static Logger logger = Logger.getLogger(AuditService.class.getName());
    private final String LOG_PATH = System.getenv("LOG_PATH");
    private EntityManager db = DataAccess.getEntityManager();
    private Audit audit = new Audit();

    // Private common initialization method
    private void initializeAudit() {
        this.audit.setTimestamp(new Timestamp(System.currentTimeMillis())); // Set timestamp on creation
    }

    public AuditService() {
        initializeAudit(); // Set timestamp
    }

    // Constructor accepting an existing Audit object - might need review if
    // timestamp should be overridden
    public AuditService(Audit audit) {
        this.audit = audit;
        // Decide if we should override the existing timestamp or keep it
        if (this.audit.getTimestamp() == null) {
            this.audit.setTimestamp(new Timestamp(System.currentTimeMillis()));
        }
    }

    public AuditService(String message) {
        this(message, LogTarget.FILE);
    }

    public AuditService(String message, LogTarget target) {
        this(message, AuditType.ERROR, target);
    }

    public AuditService(String message, AuditType type, LogTarget target) {
        this(message, getCallerInfo(), type, target);
    }

    public AuditService(String message, String source, AuditType type, LogTarget target) {
        initializeAudit(); // Set timestamp
        this.audit.setMessage(message);
        this.audit.setSource(source);
        this.audit.setType(type);
        this.audit.setTarget(target);
    }

    public AuditService(String message, String source, AuditType type, LogTarget target, User user) {
        this(message, source, type, target); // Calls constructor that sets timestamp
    }

    // Removed constructor with explicit timestamp parameter

    public static Logger getLogger() {
        return logger;
    }

    public String getMessage() {
        return this.audit.getMessage();
    }

    public void setMessage(String message) {
        this.audit.setMessage(message);
    }

    public String getSource() {
        return this.audit.getSource();
    }

    public void setSource(String source) {
        this.audit.setSource(source);
    }

    public AuditType getType() {
        return this.audit.getType();
    }

    public void setType(AuditType type) {
        this.audit.setType(type);
    }

    public Timestamp getTimestamp() {
        return this.audit.getTimestamp();
    }

    // Removed setTimestamp method to prevent manual setting
    // public void setTimestamp(Timestamp timestamp) {
    // this.audit.setTimestamp(timestamp);
    // }

    public LogTarget getTarget() {
        return this.audit.getTarget();
    }

    public void setTarget(LogTarget target) {
        this.audit.setTarget(target);
    }

    // ID might still be relevant if loading from DB, keep setId for now
    public int getId() {
        return this.audit.getId();
    }

    public void setId(int id) {
        this.audit.setId(id);
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    @Override
    public String toString() {
        // Ensure timestamp is not null before formatting
        Timestamp ts = this.getTimestamp();
        String formattedTimestamp = (ts != null) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts) : "N/A";

        return String.format("[%s] [%s] [%s] - %s",
                formattedTimestamp,
                this.getType().toString(),
                this.getSource(),
                this.getMessage());
    }

    public void log() {
        // Ensure timestamp is set before logging
        if (this.audit.getTimestamp() == null) {
            this.audit.setTimestamp(new Timestamp(System.currentTimeMillis()));
        }
        switch (this.getTarget()) {
            case DATABASE -> logToDatabase();
            case FILE -> logToFile();
        }
    }

    private void logToDatabase() {
        try {
            db.getTransaction().begin();
            db.persist(audit);
            db.getTransaction().commit();
        } catch (Exception e) {
            logger.severe("Failed to log audit to database: " + e.getMessage());
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            // Optionally, try logging to file as a fallback
            // logToFile();
        }
    }

    private void logToFile() {
        if (LOG_PATH == null || LOG_PATH.isEmpty()) {
            logger.severe("LOG_PATH environment variable not set. Cannot log audit to file.");
            return;
        }
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        // Use AuditType in filename for better organization
        String fileName = String.format("%s-%s.log", date, this.getType().toString());
        File file = new File(LOG_PATH, fileName); // Use File constructor with parent and child

        try {
            file.getParentFile().mkdirs(); // Create parent directories if they don't exist
            try (FileWriter writer = new FileWriter(file, true)) { // Use try-with-resources
                writer.write(this.toString() + System.lineSeparator());
            }
        } catch (Exception ex) {
            logger.severe("Failed to log audit to file '" + file.getAbsolutePath() + "': " + ex.getMessage());
            ex.printStackTrace(); // Print stack trace for debugging
        }
    }

    private static String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Index might vary depending on JVM and context, adjust if needed
        // stackTrace[0] = Thread.getStackTrace
        // stackTrace[1] = this method (getCallerInfo)
        // stackTrace[2] = constructor calling getCallerInfo
        // stackTrace[3] = the actual caller of the constructor
        final int CALLER_INDEX = 3;
        if (stackTrace.length > CALLER_INDEX) {
            StackTraceElement caller = stackTrace[CALLER_INDEX];
            return caller.getClassName() + "#" + caller.getMethodName();
        }
        return "UnknownSource"; // Provide a default value
    }
}
