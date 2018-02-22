package com.redrock.jade.cloudMama;

import com.github.fakemongo.Fongo;
import com.redrock.jade.shared.dao.DocumentStore;

/**
 * Copyright RedRock 2013-14
 */
public final class FongoDocumentStore extends DocumentStore {

    public FongoDocumentStore() {
        super(new Fongo("name").getDB(DocumentStore.ENTITY_DATABASE_NAME));
    }
}
