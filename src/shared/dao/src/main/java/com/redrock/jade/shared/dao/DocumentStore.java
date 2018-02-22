package com.redrock.jade.shared.dao;

import com.mongodb.DB;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

/**
 * Copyright RedRock 2013-14
 */
public abstract class DocumentStore {

    protected static final String ENTITY_DATABASE_NAME = "entities";

    private final Jongo jongo;

    public DocumentStore(DB database) {
        jongo = new Jongo(database);
    }

    public final MongoCollection getCollection(String collectionName) {
        return jongo.getCollection(collectionName);
    }
}