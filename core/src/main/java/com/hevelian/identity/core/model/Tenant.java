package com.hevelian.identity.core.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.eclipse.persistence.annotations.Index;

import com.google.common.base.Objects;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Constraint: to make the 'dateActiveChanged' work properly - update the
 * 'active' property only via the entity object.
 * 
 * @author yuflyud
 *
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "domain", callSuper = false)
public class Tenant extends AbstractEntity {
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime dateCreated;

    @Column(nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime dateActiveChanged;

    @Column(nullable = false, unique = true, updatable = false)
    @Index
    private String domain;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false, updatable = false)
    private String adminName;

    @Column(nullable = false)
    private String contactEmail;

    @Column
    // TODO handle description length properly
    private String description;

    // ADDITIONAL JPA SPECIFIC LOGIC TO HANDLE CREATE DATE AND ACTIVE DATE
    // CHANGED PROPERTIES.
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Boolean _initialActive;

    @PostLoad
    public void afterLoad() {
        _initialActive = active;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        setDateCreated(now);
        setDateActiveChanged(now);
    }

    @PreUpdate
    public void onUpdate() {
        if (!Objects.equal(active, _initialActive)) {
            setDateActiveChanged(LocalDateTime.now());
            _initialActive = active;
        }
    }
}
