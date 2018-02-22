# Overview
Jade is a simple yet highly scalable job scheduler based on Akka(Java) primarily intended for managing cloud infrastructure. Jobs are authored as Java classes and are remotely executed on an Akka agent that runs on VM or container nodes. A web based 'launcher' provides a simple interface to schedule and monitor jobs. Jade uses a MongoDB backed document store to track nodes and jobs.

# Launcher
![Launcher Settings](img/settings.png?raw=true)
![Launcher Settings](img/job-params.png?raw=true)
![Launcher Settings](img/job-status.png?raw=true)
![Launcher Settings](img/job-logs.png?raw=true)

# Jobs
A job is a serializable Java class that executes some logic on a host node. All jobs must extend the base `Job` class. The `start()` method is the entry point for a job and both synchronous as well as asynchronous executions are supported. Async jobs yield by delegating sub-tasks to other jobs and resume after the child jobs are done.

Here's an example async job:
```java
public class NewLocalDeployment extends LauncherJob {
    @Parameter(isRequired = true)
    public String name;

    @Parameter(defaultValue = "N/A")
    public String description;

    @Parameter(defaultValue = "1.1.13")
    public String version;

    @Override
    protected void start() {
        env().log().logInfo("Starting new local deployment with name = " + name);
        startChildJob(new NewDeploymentJob(name, DeploymentType.Local), NewLocalDeployment::onDeploymentCreated);
    }

    @JobCallback
    public void onDeploymentCreated(NewDeploymentJob child) {
        env().log().logInfo("Finished calling child deployment job. Deployment: " + child.getDeployment().getId());
    }
}
```

Here's a sample synchronous job that does not yield. Note that the job can still create logs and update progress, which get picked up by the job executor and streamed to the launcher panel.
```java
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
```

# Running the launcher
An IntelliJ run configuration named 'CM Launcher' is provided to run the launcher locally. This starts a launcher and job executor service.

```
[main] INFO com.redrock.jade.cloudMama.console.ContentServer - Starting server
Feb 21, 2018 11:32:17 PM org.glassfish.grizzly.http.server.NetworkListener start
INFO: Started listener bound to [0.0.0.0:49559]
Feb 21, 2018 11:32:17 PM org.glassfish.grizzly.http.server.HttpServer start
INFO: [HttpServer] Started.
[main] INFO com.redrock.jade.cloudMama.console.ContentServer - Server URL: http://10.0.0.239:49559/
[main] INFO com.redrock.jade.cloudMama.launcher.LauncherApiServer - Starting server
[main] INFO org.reflections.Reflections - Reflections took 61 ms to scan 1 urls, producing 5 keys and 9 values 
[INFO] [02/21/2018 23:32:28.075] [main] [Remoting] Starting remoting
[INFO] [02/21/2018 23:32:33.222] [main] [Remoting] Remoting started; listening on addresses :[akka.tcp://JobDispatcher@10.0.0.239:49625]
[INFO] [02/21/2018 23:32:33.224] [main] [Remoting] Remoting now listens on addresses: [akka.tcp://JobDispatcher@10.0.0.239:49625]
[INFO] [02/21/2018 23:32:33.228] [main] [ActorSystem(JobDispatcher)] Akka remote service 'JobDispatcher' has started on '10.0.0.239:49625'
[INFO] [02/21/2018 23:32:33.253] [main] [Remoting] Starting remoting
[INFO] [02/21/2018 23:32:38.265] [main] [Remoting] Remoting started; listening on addresses :[akka.tcp://LauncherJobExecutor@10.0.0.239:49646]
[INFO] [02/21/2018 23:32:38.266] [main] [Remoting] Remoting now listens on addresses: [akka.tcp://LauncherJobExecutor@10.0.0.239:49646]
[INFO] [02/21/2018 23:32:38.267] [main] [ActorSystem(LauncherJobExecutor)] Akka remote service 'LauncherJobExecutor' has started on '10.0.0.239:49646'
Feb 21, 2018 11:32:38 PM org.glassfish.jersey.server.ApplicationHandler initialize
INFO: Initiating Jersey application, version Jersey: 2.10 2014-06-24 10:31:08...
Feb 21, 2018 11:32:38 PM org.glassfish.grizzly.http.server.NetworkListener start
INFO: Started listener bound to [0.0.0.0:49647]
Feb 21, 2018 11:32:38 PM org.glassfish.grizzly.http.server.HttpServer start
INFO: [HttpServer-1] Started.
[main] INFO com.redrock.jade.cloudMama.launcher.LauncherApiServer - Server base URL: http://10.0.0.239:49647/api/
[INFO] [02/21/2018 23:42:54.493] [JobDispatcher-akka.actor.default-dispatcher-3] [akka://JobDispatcher/user/$a] Found new job ready to execute: eb0c77cc-e07a-48ed-932a-a6a764ba74cf
[Grizzly-worker(7)] INFO com.redrock.jade.cloudMama.console.resources.JobResource - Request for new job. Details: JobInputMessage{classCanonicalName='com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment', parameters={name=DGF-OneBox, description=Game server deployment, version=1.1.13}}
[ERROR] [02/21/2018 23:42:54.495] [JobDispatcher-akka.actor.default-dispatcher-3] [akka://JobDispatcher/user/$a] .................. Changed dispatch status of 5a8e747d77c83adbf2faad2a whose status = PENDING
[INFO] [02/21/2018 23:42:54.509] [JobDispatcher-akka.actor.default-dispatcher-4] [akka://JobDispatcher/user/$a/$a] Dispatching job '[com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment: eb0c77cc-e07a-48ed-932a-a6a764ba74cf]' for execution on 'ActorSelection[Anchor(akka.tcp://LauncherJobExecutor@10.0.0.239:49646/), Path(/user/ExecutionRequestRouter)]'
[INFO] [02/21/2018 23:42:54.662] [LauncherJobExecutor-akka.actor.default-dispatcher-5] [akka://LauncherJobExecutor/user/ExecutionRequestRouter/$a] Starting job executor for: [com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment: eb0c77cc-e07a-48ed-932a-a6a764ba74cf]
[WARN] [02/21/2018 23:42:55.033] [LauncherJobExecutor-akka.actor.default-dispatcher-15] [akka://LauncherJobExecutor/user/ExecutionRequestRouter/$a] Progress update for [com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment: eb0c77cc-e07a-48ed-932a-a6a764ba74cf] with state = AWAITING_CHILD_COMPLETION and pending children = 7cb28281-026d-4143-9808-ba81b4e0a814
[INFO] [02/21/2018 23:42:55.033] [LauncherJobExecutor-akka.actor.default-dispatcher-15] [akka://LauncherJobExecutor/user/ExecutionRequestRouter/$a] Shutting down executor for [com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment: eb0c77cc-e07a-48ed-932a-a6a764ba74cf] with state AWAITING_CHILD_COMPLETION
[INFO] [02/21/2018 23:42:55.529] [JobDispatcher-akka.actor.default-dispatcher-4] [akka://JobDispatcher/user/$a] Found new job ready to execute: 7cb28281-026d-4143-9808-ba81b4e0a814
[ERROR] [02/21/2018 23:42:55.529] [JobDispatcher-akka.actor.default-dispatcher-4] [akka://JobDispatcher/user/$a] .................. Changed dispatch status of 5a8e747f77c83adbf2faad2e whose status = PENDING
[INFO] [02/21/2018 23:42:55.532] [JobDispatcher-akka.actor.default-dispatcher-3] [akka://JobDispatcher/user/$a/$b] Dispatching job '[com.redrock.jade.cloudMama.launcher.jobs.NewDeploymentJob: 7cb28281-026d-4143-9808-ba81b4e0a814]' for execution on 'ActorSelection[Anchor(akka.tcp://LauncherJobExecutor@10.0.0.239:49646/), Path(/user/ExecutionRequestRouter)]'
[INFO] [02/21/2018 23:42:55.535] [LauncherJobExecutor-akka.actor.default-dispatcher-15] [akka://LauncherJobExecutor/user/ExecutionRequestRouter/$b] Starting job executor for: [com.redrock.jade.cloudMama.launcher.jobs.NewDeploymentJob: 7cb28281-026d-4143-9808-ba81b4e0a814]
[WARN] [02/21/2018 23:42:55.898] [LauncherJobExecutor-akka.actor.default-dispatcher-5] [akka://LauncherJobExecutor/user/ExecutionRequestRouter/$b] Progress update for [com.redrock.jade.cloudMama.launcher.jobs.NewDeploymentJob: 7cb28281-026d-4143-9808-ba81b4e0a814] with state = SUCCEEDED and pending children = 
[INFO] [02/21/2018 23:42:55.899] [LauncherJobExecutor-akka.actor.default-dispatcher-5] [akka://LauncherJobExecutor/user/ExecutionRequestRouter/$b] Shutting down executor for [com.redrock.jade.cloudMama.launcher.jobs.NewDeploymentJob: 7cb28281-026d-4143-9808-ba81b4e0a814] with state SUCCEEDED
[INFO] [02/21/2018 23:42:55.906] [JobDispatcher-akka.actor.default-dispatcher-4] [akka://JobDispatcher/user/$a/$b] Job '[com.redrock.jade.cloudMama.launcher.jobs.NewDeploymentJob: 7cb28281-026d-4143-9808-ba81b4e0a814]' has finished execution with status 'SUCCEEDED'
[INFO] [02/21/2018 23:42:56.555] [JobDispatcher-akka.actor.default-dispatcher-2] [akka://JobDispatcher/user/$a] Found waiting job ready to execute: eb0c77cc-e07a-48ed-932a-a6a764ba74cf. Completed child count: 1/// DbID = 5a8e747d77c83adbf2faad2a
[INFO] [02/21/2018 23:42:56.555] [JobDispatcher-akka.actor.default-dispatcher-2] [akka://JobDispatcher/user/$a] >>> Children complete for 5a8e747d77c83adbf2faad2a: [com.redrock.jade.cloudMama.launcher.jobs.NewDeploymentJob: 7cb28281-026d-4143-9808-ba81b4e0a814]
[ERROR] [02/21/2018 23:42:56.555] [JobDispatcher-akka.actor.default-dispatcher-2] [akka://JobDispatcher/user/$a] .................. Changed dispatch status of 5a8e747d77c83adbf2faad2a whose status = AWAITING_CHILD_COMPLETION
[INFO] [02/21/2018 23:42:56.557] [JobDispatcher-akka.actor.default-dispatcher-3] [akka://JobDispatcher/user/$a/$c] Dispatching job '[com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment: eb0c77cc-e07a-48ed-932a-a6a764ba74cf]' for execution on 'ActorSelection[Anchor(akka.tcp://LauncherJobExecutor@10.0.0.239:49646/), Path(/user/ExecutionRequestRouter)]'
[INFO] [02/21/2018 23:42:56.563] [LauncherJobExecutor-akka.actor.default-dispatcher-15] [akka://LauncherJobExecutor/user/ExecutionRequestRouter/$c] Starting job executor for: [com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment: eb0c77cc-e07a-48ed-932a-a6a764ba74cf]
[WARN] [02/21/2018 23:42:56.928] [LauncherJobExecutor-akka.actor.default-dispatcher-5] [akka://LauncherJobExecutor/user/ExecutionRequestRouter/$c] Progress update for [com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment: eb0c77cc-e07a-48ed-932a-a6a764ba74cf] with state = SUCCEEDED and pending children = 
[INFO] [02/21/2018 23:42:56.929] [LauncherJobExecutor-akka.actor.default-dispatcher-5] [akka://LauncherJobExecutor/user/ExecutionRequestRouter/$c] Shutting down executor for [com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment: eb0c77cc-e07a-48ed-932a-a6a764ba74cf] with state SUCCEEDED
[INFO] [02/21/2018 23:42:56.935] [JobDispatcher-akka.actor.default-dispatcher-2] [akka://JobDispatcher/user/$a/$c] Job '[com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment: eb0c77cc-e07a-48ed-932a-a6a764ba74cf]' has finished execution with status 'SUCCEEDED'
[Thread-1] INFO com.redrock.jade.cloudMama.console.ContentServer - Shutting down server
[Thread-1] INFO com.redrock.jade.cloudMama.launcher.LauncherApiServer - Shutting down server
[INFO] [02/22/2018 00:17:55.517] [LauncherJobExecutor-akka.remote.default-remote-dispatcher-6] [akka.tcp://LauncherJobExecutor@10.0.0.239:49646/system/remoting-terminator] Shutting down remote daemon.
[INFO] [02/22/2018 00:17:55.519] [LauncherJobExecutor-akka.remote.default-remote-dispatcher-6] [akka.tcp://LauncherJobExecutor@10.0.0.239:49646/system/remoting-terminator] Remote daemon shut down; proceeding with flushing remote transports.
[INFO] [02/22/2018 00:17:55.541] [LauncherJobExecutor-akka.actor.default-dispatcher-5] [akka://LauncherJobExecutor/system/endpointManager/reliableEndpointWriter-akka.tcp%3A%2F%2FJobDispatcher%4010.0.0.239%3A49625-0/endpointWriter/endpointReader-akka.tcp%3A%2F%2FJobDispatcher%4010.0.0.239%3A49625-0] Message [akka.remote.transport.AssociationHandle$Disassociated] from Actor[akka://LauncherJobExecutor/deadLetters] to Actor[akka://LauncherJobExecutor/system/endpointManager/reliableEndpointWriter-akka.tcp%3A%2F%2FJobDispatcher%4010.0.0.239%3A49625-0/endpointWriter/endpointReader-akka.tcp%3A%2F%2FJobDispatcher%4010.0.0.239%3A49625-0#-1441272250] was not delivered. [1] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
[INFO] [02/22/2018 00:17:55.545] [JobDispatcher-akka.actor.default-dispatcher-4] [akka://JobDispatcher/system/transports/akkaprotocolmanager.tcp0/akkaProtocol-tcp%3A%2F%2FLauncherJobExecutor%4010.0.0.239%3A49646-1] Message [akka.remote.transport.AssociationHandle$Disassociated] from Actor[akka://JobDispatcher/deadLetters] to Actor[akka://JobDispatcher/system/transports/akkaprotocolmanager.tcp0/akkaProtocol-tcp%3A%2F%2FLauncherJobExecutor%4010.0.0.239%3A49646-1#-342592513] was not delivered. [1] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
[INFO] [02/22/2018 00:17:55.545] [LauncherJobExecutor-akka.actor.default-dispatcher-17] [akka://LauncherJobExecutor/system/transports/akkaprotocolmanager.tcp1/akkaProtocol-tcp%3A%2F%2FLauncherJobExecutor%4010.0.0.239%3A51652-1] Message [akka.remote.transport.AssociationHandle$Disassociated] from Actor[akka://LauncherJobExecutor/deadLetters] to Actor[akka://LauncherJobExecutor/system/transports/akkaprotocolmanager.tcp1/akkaProtocol-tcp%3A%2F%2FLauncherJobExecutor%4010.0.0.239%3A51652-1#1147285354] was not delivered. [2] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
[ERROR] [02/22/2018 00:17:55.547] [JobDispatcher-akka.remote.default-remote-dispatcher-5] [akka.tcp://JobDispatcher@10.0.0.239:49625/system/endpointManager/reliableEndpointWriter-akka.tcp%3A%2F%2FLauncherJobExecutor%4010.0.0.239%3A49646-0/endpointWriter] AssociationError [akka.tcp://JobDispatcher@10.0.0.239:49625] -> [akka.tcp://LauncherJobExecutor@10.0.0.239:49646]: Error [Shut down address: akka.tcp://LauncherJobExecutor@10.0.0.239:49646] [
akka.remote.ShutDownAssociation: Shut down address: akka.tcp://LauncherJobExecutor@10.0.0.239:49646
Caused by: akka.remote.transport.Transport$InvalidAssociationException: The remote system terminated the association because it is shutting down.
]
[INFO] [02/22/2018 00:17:55.552] [JobDispatcher-akka.actor.default-dispatcher-4] [akka://JobDispatcher/system/transports/akkaprotocolmanager.tcp0/akkaProtocol-tcp%3A%2F%2FLauncherJobExecutor%4010.0.0.239%3A49646-1] Message [akka.remote.transport.ActorTransportAdapter$DisassociateUnderlying] from Actor[akka://JobDispatcher/deadLetters] to Actor[akka://JobDispatcher/system/transports/akkaprotocolmanager.tcp0/akkaProtocol-tcp%3A%2F%2FLauncherJobExecutor%4010.0.0.239%3A49646-1#-342592513] was not delivered. [2] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
[INFO] [02/22/2018 00:17:55.557] [ForkJoinPool-5-worker-15] [Remoting] Remoting shut down
[INFO] [02/22/2018 00:17:55.557] [LauncherJobExecutor-akka.remote.default-remote-dispatcher-6] [akka.tcp://LauncherJobExecutor@10.0.0.239:49646/system/remoting-terminator] Remoting shut down.
[INFO] [02/22/2018 00:17:55.575] [JobDispatcher-akka.remote.default-remote-dispatcher-5] [akka.tcp://JobDispatcher@10.0.0.239:49625/system/remoting-terminator] Shutting down remote daemon.
[INFO] [02/22/2018 00:17:55.575] [JobDispatcher-akka.remote.default-remote-dispatcher-5] [akka.tcp://JobDispatcher@10.0.0.239:49625/system/remoting-terminator] Remote daemon shut down; proceeding with flushing remote transports.
[INFO] [02/22/2018 00:17:55.579] [ForkJoinPool-5-worker-15] [Remoting] Remoting shut down
[INFO] [02/22/2018 00:17:55.579] [JobDispatcher-akka.remote.default-remote-dispatcher-6] [akka.tcp://JobDispatcher@10.0.0.239:49625/system/remoting-terminator] Remoting shut down.

Process finished with exit code 130 (interrupted by signal 2: SIGINT)
```