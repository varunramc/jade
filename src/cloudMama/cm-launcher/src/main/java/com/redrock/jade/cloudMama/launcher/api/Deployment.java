package com.redrock.jade.cloudMama.launcher.api;

import com.redrock.jade.shared.dao.CollectionName;
import com.redrock.jade.shared.dao.Document;

/**
 * Copyright RedRock 2013-14
 */
@CollectionName("deployments")
public final class Deployment extends Document {

    private DeploymentType type;
    private String name;

    public Deployment() {
    }

    public Deployment(DeploymentType type, String name) {
        this.type = type;
        this.name = name;
    }

    public DeploymentType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
