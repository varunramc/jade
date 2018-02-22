package com.redrock.jade.cloudMama;

import akka.dispatch.Mapper;
import com.redrock.jade.cloudMama.services.Service;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

/**
 * Copyright RedRock 2013-14
 */
public class AkkaUtils {
    public static <NewType> Future<NewType> mapToType(Future<Object> future, ExecutionContext executor) {
        return future.map(new Mapper<Object, NewType>() {
            @Override
            public NewType apply(Object value) {
                return (NewType) value;
            }
        }, executor);
    }

    public static String getRemoteServiceActorPath(Service service, String actorName) {
        return String.format("akka.tcp://%s@%s:%d/user/%s",
                             service.getType(),
                             service.getAddress(),
                             service.getPort(),
                             actorName);
    }
}
