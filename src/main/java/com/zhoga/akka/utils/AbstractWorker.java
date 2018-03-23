package com.zhoga.akka.utils;

import akka.actor.AbstractActor;

public abstract class AbstractWorker extends AbstractActor {
    protected Receive receiveEcho(final Object message) {
        final TestMessages.WorkerConfigurationMessage msg = new TestMessages.WorkerConfigurationMessage(message);

        return receiveBuilder()
                .matchEquals(TestMessages.GetWorkerConfigurationMessage, m -> {
                    sender().tell(
                            msg,
                            self()
                    );
                })
                .build();
    }
}
