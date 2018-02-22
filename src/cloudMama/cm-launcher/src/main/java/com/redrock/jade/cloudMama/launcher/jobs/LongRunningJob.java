package com.redrock.jade.cloudMama.launcher.jobs;

import com.redrock.jade.cloudMama.jobs.Job;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

/**
 * Copyright RedRock 2013-14
 */
public class LongRunningJob extends Job {
    @Override
    protected void start() {
    }

    @Override
    protected Service getExecutor(ServiceCollection serviceCollection) {
        return null;
    }
}
