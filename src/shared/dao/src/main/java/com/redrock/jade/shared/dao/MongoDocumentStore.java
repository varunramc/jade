package com.redrock.jade.shared.dao;

import com.mongodb.MongoClient;

import java.net.UnknownHostException;

/**
 * Copyright RedRock 2013-14
 */
public final class MongoDocumentStore extends DocumentStore {
    private final MongoClient mongoClient;

    public MongoDocumentStore(String address, int port, String databaseName) throws UnknownHostException {
        this(new MongoClient(address, port), databaseName);
    }

    private MongoDocumentStore(MongoClient mongoClient, String databaseName) {
        super(mongoClient.getDB(databaseName));
        this.mongoClient = mongoClient;
    }
}
