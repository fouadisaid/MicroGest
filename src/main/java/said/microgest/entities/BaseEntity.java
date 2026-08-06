package said.microgest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import said.microgest.utils.SessionContext;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    protected String createdBy;

    @Column(name = "updated_by", nullable = false, length = 100)
    protected String updatedBy;

    @PrePersist
    protected void onPrePersist() {
        String user = SessionContext.getCurrentUsername();
        createdBy = user;
        updatedBy = user;
    }

    @PreUpdate
    protected void onPreUpdate() {
        updatedBy = SessionContext.getCurrentUsername();
    }
}