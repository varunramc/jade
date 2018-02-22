package com.redrock.jade.cloudMama.services;

import com.redrock.jade.shared.dao.CollectionName;
import com.redrock.jade.shared.dao.Document;

/**
 * Copyright RedRock 2013-14
 */
@CollectionName("services")
public abstract class Service extends Document {
    private String type;
    private String address;
    private int    port;

    public Service() {
    }

    public Service(String type, String address, int port) {
        this.type = type;
        this.address = address;
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
