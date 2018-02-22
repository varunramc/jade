package com.redrock.jade.cloudMama.console.jobs;

import com.redrock.jade.cloudMama.jobs.Job;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

/**
 * Copyright RedRock 2013-14
 */
public class EmptyJob extends Job {
    @Override
    protected void start() {

    }

    @Override
    protected Service getExecutor(ServiceCollection serviceCollection) {
        return null;
    }
}
