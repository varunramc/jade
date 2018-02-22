package com.redrock.jade.cloudMama.launcher.jobs;

/**
 * Copyright RedRock 2013-14
 */
public final class BusyJob extends LauncherJob {
    public BusyJob() {
    }

    @Override
    protected void start() throws Exception {
        env().log().logInfo("Booting up...");
        env().log().logWarning("Running long operation...");

        Thread.sleep((long)(5000d * Math.random()));

        setProgressPercentage(0.2f);
        if (Math.random() < 0.1f) {
            throw new IllegalStateException("Something bad happened...");
        }
        env().log().logVerbose("Progressing...");
        for (int i=3; i <= 10; i++) {
            Thread.sleep((long)(2000d * Math.random()));
            env().log().logInfo("Progressed to step " + i);
            setProgressPercentage(i * 0.1f);
        }

        env().log().logVerbose("Finalizing...");

        Thread.sleep((long)(3000d * Math.random()));
        env().log().logInfo("Done!");
    }
}
