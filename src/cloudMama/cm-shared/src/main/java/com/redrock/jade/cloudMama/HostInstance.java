package com.redrock.jade.cloudMama;

import com.redrock.jade.shared.dao.CollectionName;
import com.redrock.jade.shared.dao.Document;

import java.time.Instant;

/**
 * Copyright RedRock 2013-14
 */
@CollectionName("hostInstances")
public final class HostInstance extends Document {

    /**
     * The unique name for the instance
     */
    private String name;

    /**
     * The host IP address
     */
    private String address;

    /**
     * The UTC time when the instance was spawned
     */
    private Instant createTime;
}
