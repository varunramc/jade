package com.redrock.jade.cloudMama;

import com.redrock.jade.shared.dao.Document;

import java.time.Instant;

/**
 * Copyright RedRock 2013-14
 */
public final class RoleInstance extends Document {

    /**
     * The unique name of the instance
     */
    private String name;

    /**
     * The host instance hosting the role instance
     */
    private HostInstance hostInstance;

    /**
     * The UTC time when the instance was spawned
     */
    private Instant createTime;

}
