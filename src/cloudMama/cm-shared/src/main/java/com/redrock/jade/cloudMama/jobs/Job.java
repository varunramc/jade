package com.redrock.jade.cloudMama.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import com.redrock.jade.cloudMama.StreamUtils;
import com.redrock.jade.cloudMama.ThrowableUtils;
import com.redrock.jade.cloudMama.jobs.exceptions.CallbackMethodException;
import com.redrock.jade.cloudMama.jobs.exceptions.ChildJobNotFoundException;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;
import com.redrock.jade.shared.dao.CollectionName;
import com.redrock.jade.shared.dao.Document;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

/**
 * Copyright RedRock 2013-14
 */
@CollectionName("jobs")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_class")
public abstract class Job extends Document implements Serializable {
    private final List<String>                 children;
    private final Map<String, ChildJobHandler> childJobHandlers;
    private final Set<String>                  pendingChildren;
    private       String                       parentId;
    private       String                       rootJobId;
    private       ExecutionStatus              status;
    private       String                       error;
    private       float                        progressPercentage;

    @JsonIgnore
    private final ConcurrentLinkedQueue<Job> childJobsToStart;

    @JsonIgnore
    private transient JobEnvironment environment;

    protected Job() {
        parentId = null;
        rootJobId = getId();

        status = ExecutionStatus.PENDING;
        children = new LinkedList<>();
        childJobHandlers = new HashMap<>();
        pendingChildren = new HashSet<>();
        childJobsToStart = new ConcurrentLinkedQueue<>();
    }

    final void setParent(Job parent) {
        Preconditions.checkNotNull(parent);

        parentId = parent.getId();
        rootJobId = parent.getRootJobId();
    }

    final void startJob(JobEnvironment environment) {
        onPreExecution(environment);

        try {
            start();
        }
        catch (Throwable throwable) {
            error = throwable.getClass().getName();
            environment.log()
                       .logError("Exception encountered during job startup:\n" +
                                 ThrowableUtils.getFullDescription(throwable));
            status = ExecutionStatus.FAILED;
        }

        onPostExecution();
    }

    final void onChildrenCompletion(Iterable<Job> children, JobEnvironment environment) {
        for (Job child : children) {
            onChildCompletion(child, environment);

            if (status.isComplete()) {
                return;
            }
        }
    }

    private void onChildCompletion(Job child, JobEnvironment environment) {
        onPreExecution(environment);
        ChildJobHandler childJobHandler = getChildJobHandler(child);

        try {
            childJobHandler.onComplete(child, this);
        }
        catch (Throwable throwable) {
            error = throwable.getClass().getName();
            environment.log().logError(String.format(
                    "Exception encountered during job completion processing of '%s':\n%s",
                    child,
                    ThrowableUtils.getFullDescription(throwable)));
            status = ExecutionStatus.FAILED;
        }

        removeChildJobHandler(child);
        pendingChildren.remove(child.getId());
        onPostExecution();
    }

    private void onPreExecution(JobEnvironment environment) {
        this.environment = environment;
        status = ExecutionStatus.RUNNING;
    }

    private void onPostExecution() {
        if (!status.isComplete()) {
            if (pendingChildren.isEmpty()) {
                setProgressPercentage(1f);
                status = ExecutionStatus.SUCCEEDED;
            }
            else {
                status = ExecutionStatus.AWAITING_CHILD_COMPLETION;
            }
        }
    }

    private ChildJobHandler getChildJobHandler(Job child) {
        ChildJobHandler childJobHandler = childJobHandlers.getOrDefault(child.getId(), null);
        if (childJobHandler == null) {
            throw new ChildJobNotFoundException("Unrecognized job: " + child);
        }

        return childJobHandler;
    }

    private void removeChildJobHandler(Job child) {
        childJobHandlers.remove(child.getId());
    }

    protected abstract void start() throws Exception;

    protected abstract Service getExecutor(ServiceCollection serviceCollection);

    protected final void startChildJob(Job job) {
        startChildJob(job, null);
    }

    protected final <J extends Job, C extends Job> void startChildJob(C job,
                                                                      BiConsumer<J, C> onCompleteHandler) {
        Preconditions.checkNotNull(job);
        Preconditions.checkArgument(job != this);

        if (children.contains(job.getId())) {
            throw new IllegalArgumentException(String.format("Child job with id '%s' already exists in job '%s'",
                                                             job.getId(),
                                                             getId()));
        }

        job.setParent(this);
        children.add(job.getId());
        childJobsToStart.add(job);
        pendingChildren.add(job.getId());
        addChildJobHandler(job, onCompleteHandler);
    }

    private <J extends Job, C extends Job> void addChildJobHandler(C job,
                                                                   BiConsumer<J, C> onCompleteHandler) {
        MethodSignature methodSignature = getChildHandlerMethodSignature(onCompleteHandler);
        ChildJobHandler childJobHandler = new ChildJobHandler(job.getId(), methodSignature);

        childJobHandlers.put(job.getId(), childJobHandler);
    }

    private <J extends Job, C extends Job> MethodSignature getChildHandlerMethodSignature(BiConsumer<J, C> onCompleteHandler) {
        MethodSignature methodSignature = null;

        if (onCompleteHandler != null) {
            validateJobClassForCallbackSupport();

            try {
                methodSignature = MethodSignatureExtractor.extract(getClass(),
                                                                   instance -> onCompleteHandler.accept((J) instance,
                                                                                                        null));
            }
            catch (IllegalAccessException exception) {
                throw new CallbackMethodException("Failed to extract child completion callback signature", exception);
            }
        }

        return methodSignature;
    }

    private void validateJobClassForCallbackSupport() {
        validateClassHasPublicDefaultConstructor();
        validateClassIsPublic();
        validateClassIsNotFinal();
    }

    private void validateClassHasPublicDefaultConstructor() {
        boolean hasPublicDefaultCtor = StreamUtils.stream(getClass().getDeclaredConstructors())
                                                  .filter(ctor -> Modifier.isPublic(ctor.getModifiers()) &&
                                                                  ctor.getParameters().length == 0)
                                                  .findAny()
                                                  .isPresent();

        if (!hasPublicDefaultCtor) {
            throw new CallbackMethodException(
                    String.format(
                            "The job class %s has no public default constructor. Callbacks cannot be supported.",
                            getClass().getCanonicalName()),
                    null);
        }
    }

    private void validateClassIsPublic() {
        if (!Modifier.isPublic(getClass().getModifiers())) {
            throw new CallbackMethodException(
                    String.format(
                            "The job class %s is not public. Callbacks are only supported on public classes.",
                            getClass().getCanonicalName()),
                    null);
        }
    }

    private void validateClassIsNotFinal() {
        if (Modifier.isFinal(getClass().getModifiers())) {
            throw new CallbackMethodException(
                    String.format(
                            "The job class %s is final. Callbacks are only supported on classes that can be sub-classed.",
                            getClass().getCanonicalName()),
                    null);
        }
    }

    protected JobEnvironment env() {
        return environment;
    }

    final void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    final void setError(String error) {
        this.error = error;
    }

    final Collection<String> getPendingChildren() {
        return pendingChildren;
    }

    public final ExecutionStatus getStatus() {
        return status;
    }

    public final Collection<String> getChildren() {
        return children;
    }

    public String getParentId() {
        return parentId;
    }

    public String getRootJobId() {
        return rootJobId;
    }

    public final float getProgressPercentage() {
        return progressPercentage;
    }

    protected final void setProgressPercentage(float progressPercentage) {
        Preconditions.checkArgument(progressPercentage >= 0f && progressPercentage <= 1.0f,
                                    String.format("Percentage value '%f' is not in the range [0, 1]",
                                                  progressPercentage));
        this.progressPercentage = progressPercentage;
    }

    @Override
    public String toString() {
        return String.format("[%s: %s]", getClass().getCanonicalName(), getId());
    }

    public final String getError() {
        return error;
    }

    final List<Job> resetAndGetChildJobsToStart() {
        List<Job> result = new ArrayList<>();
        Job child;

        while ((child = childJobsToStart.poll()) != null) {
            result.add(child);
        }

        return result;
    }
}
