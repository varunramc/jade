package com.redrock.jade.shared.dao;

import com.google.common.base.Preconditions;
import org.jongo.MongoCollection;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright RedRock 2013-14
 */
public class DocumentCollection<T extends Document> {
    private static final Map<Class, String> collectionNameMap = new HashMap<>();

    protected final Class<T>        documentClass;
    protected final MongoCollection collection;

    public DocumentCollection(DocumentStore documentStore, Class<T> documentClass) {
        Preconditions.checkNotNull(documentStore);
        Preconditions.checkNotNull(documentClass);

        this.documentClass = documentClass;
        collection = documentStore.getCollection(DocumentCollection.getCollection(documentClass));
    }

    public T getById(String id) {
        return collection.findOne("{id: #}", id).as(documentClass);
    }

    public Iterable<T> getAll() {
        return collection.find().as(documentClass);
    }

    public void save(T document) {
        collection.save(document);
    }

    public void insert(T document) {
        collection.insert(document);
    }

    public void insert(Iterable<T> documents) {
        for (T document : documents) {
            collection.insert(document);
        }
    }

    private static <E> String getCollection(Class<E> entityClass) {
        if (!DocumentCollection.collectionNameMap.containsKey(entityClass)) {
            CollectionName collectionAnnotation = entityClass.getAnnotation(CollectionName.class);

            if (collectionAnnotation == null) {
                throw new IllegalArgumentException(String.format(
                        "Entity class '%s' does not contain a collection annotation",
                        entityClass.getCanonicalName()));
            }

            DocumentCollection.collectionNameMap.put(entityClass, collectionAnnotation.value());
        }

        return DocumentCollection.collectionNameMap.get(entityClass);
    }
}
