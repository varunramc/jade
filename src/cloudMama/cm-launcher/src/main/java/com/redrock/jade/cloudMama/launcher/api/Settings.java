package com.redrock.jade.cloudMama.launcher.api;

import com.mongodb.MongoClient;
import com.redrock.jade.cloudMama.launcher.ProcessUtils;
import com.redrock.jade.shared.dao.CollectionName;
import com.redrock.jade.shared.dao.Document;
import com.redrock.jade.shared.dao.MongoDocumentStore;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Copyright RedRock 2013-14
 */
public final class Settings {
    private String machineName;
    private String devRoot;
    private String mongoVersion;
    private String vagrantVersion;

    public Settings() {
        detectMachineName();
        devRoot = System.getenv("JADE_ROOT");
        detectMongo();
        detectVagrant();
    }

    private void detectMachineName() {
        try {
            machineName = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex) {
            machineName = System.getenv("HOSTNAME");
        }
    }

    private void detectMongo() {
        try {
            mongoVersion = new MongoClient("localhost").getVersion();
        }
        catch (UnknownHostException ex) {
            mongoVersion = null;
        }
    }

    private void detectVagrant() {
        try {
            vagrantVersion = ProcessUtils.getProcessOutput("vagrant -v");
        }
        catch (IOException ex) {
            vagrantVersion = null;
        }
    }

    public String getMachineName() {
        return machineName;
    }

    public String getDevRoot() {
        return devRoot;
    }

    public String getMongoVersion() {
        return mongoVersion;
    }

    public String getVagrantVersion() {
        return vagrantVersion;
    }
}
