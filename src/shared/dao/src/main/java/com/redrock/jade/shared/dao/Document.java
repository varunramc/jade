package com.redrock.jade.shared.dao;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;

import java.io.Serializable;
import java.util.UUID;

/**
 * Copyright RedRock 2013-14
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_class")
public abstract class Document implements Serializable {
    @Id
    @ObjectId
    public        String dbId;
    private final String id;

    public Document() {
        id = UUID.randomUUID().toString();
    }

    public Document(String id) {
        this.id = id;
    }

    public void resolveReferences(DocumentStore documentStore) throws DocumentException {
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Document)) {
            return false;
        }

        return id.equals(((Document) obj).getId());
    }

    @Override
    public String toString() {
        return String.format("[%s: %s]", getClass().getName(), id);
    }
}
