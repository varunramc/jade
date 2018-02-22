package com.redrock.jade.cloudMama.services.messages;

/**
 * Copyright RedRock 2013-14
 */
public final class GetServicesMsg {
    private final String serviceType;

    public GetServicesMsg(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceType() {
        return serviceType;
    }
}
