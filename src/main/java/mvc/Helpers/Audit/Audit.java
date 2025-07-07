package mvc.Helpers.Audits;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import mvc.Helpers.Audits.AuditType;
import mvc.Helpers.Audits.LogTarget;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "Audit_Trail")
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String source;

    @Column(name = "description")
    private String message;

    @Enumerated(EnumType.STRING)
    private AuditType type;

    @Column(name = "timestamp", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp timestamp;

    // ignore by JPA and Json
    @Transient
    @JsonIgnore
    private LogTarget target;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public AuditType getType() {
        return type;
    }

    public void setType(AuditType type2) {
        this.type = type2;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public LogTarget getTarget() {
        return target;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTarget(LogTarget target) {
        this.target = target;
    }

}
