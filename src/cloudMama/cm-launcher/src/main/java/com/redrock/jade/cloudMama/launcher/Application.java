package com.redrock.jade.cloudMama.launcher;

import com.redrock.jade.cloudMama.console.ContentServer;
import com.redrock.jade.cloudMama.console.JsClientConfiguration;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;

/**
 * Copyright RedRock 2013-14
 */
public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    final ContentServer     contentServer     = new ContentServer();
    final LauncherApiServer launcherApiServer = new LauncherApiServer();

    private void start() throws IOException, OperationNotSupportedException {

        try {
            contentServer.start();
            launcherApiServer.start();

            setupClientConfiguration();
        }
        catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    private void setupClientConfiguration() throws OperationNotSupportedException {
        JsClientConfiguration clientConfig = contentServer.getClientConfiguration();
        clientConfig.addConfiguration("launcherApiHost", "localhost");
        clientConfig.addConfiguration("launcherApiPort", launcherApiServer.getPort());
    }

    private void stop() {
        contentServer.stop();
        launcherApiServer.stop();
    }

    private void run() throws IOException, OperationNotSupportedException, InterruptedException {
        start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        openServerUrl();

        while (true) {
            Thread.sleep(1000);
        }
    }

    private void openServerUrl() throws IOException {
        String urlApp = getOsSpecificUrlApplication();

        CommandLine urlCommandLine =
                CommandLine.parse(String.format("%s %s%s", urlApp, contentServer.getUrl(), "landing.html"));
        DefaultExecutor executor = new DefaultExecutor();
        executor.execute(urlCommandLine);
    }

    private String getOsSpecificUrlApplication() {
        if (OS.isFamilyMac()) {
            return "open";
        }
        else if (OS.isFamilyUnix()) {
            return "xdg-open";
        }

        throw new UnsupportedOperationException(String.format("Platform %s is not supported",
                                                              System.getProperty("os.name")));
    }

    public static void main(String[] args) throws IOException, OperationNotSupportedException, InterruptedException {
        new Application().run();
    }
}
