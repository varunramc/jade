package com.redrock.jade.shared.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Copyright RedRock 2013-14
 */

/**
 * Encapsulates a foreign key like reference to another document
 * NOTE: Cyclical references are not supported automatically.
 *
 * @param <T>
 *     The type of the reference document.
 */
public final class DocumentReference<T extends Document> {
    private final String id;

    @JsonIgnore
    private T value;

    public DocumentReference() {
        id = null;
    }

    public DocumentReference(T value) {
        Preconditions.checkNotNull(value);

        this.value = value;
        id = value.getId();
    }

    public void resolve(DocumentStore documentStore, Class<T> entityClass) throws DocumentException {
        if (Strings.isNullOrEmpty(id)) {
            throw new DocumentException(String.format("Unable to resolve reference of type '%s' as ID is empty", entityClass.getName()));
        }

        value = new DocumentCollection<>(documentStore, entityClass).getById(id);
    }

    public String getId() {
        return id;
    }

    public T getValue() {
        return value;
    }
}
