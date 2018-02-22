package com.redrock.jade.cloudMama.jobs.stubs;

import com.redrock.jade.cloudMama.jobs.JobCallback;

/**
* Copyright RedRock 2013-14
*/
public class MethodSignatureTargetStub {
    private final int value = 4;

    @JobCallback
    public void simple() {

    }

    @JobCallback
    public int duplicate(int toAdd) {
        return value + toAdd;
    }

    @JobCallback
    public int duplicate() {
        return 7;
    }
}
