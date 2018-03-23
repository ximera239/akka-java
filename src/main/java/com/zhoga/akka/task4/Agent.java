package com.zhoga.akka.task4;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.Random;

import static com.zhoga.akka.utils.Validations.require;

/**
 * Agent actor responds up to 9 times with Trigger message followed
 * by Acknowledge message.
 */
public class Agent extends AbstractActor {
    /**
     * Props factory for Agent actor
     * @param id actor unique identifier, should be greater than 0
     * @return Props object for Worker actor
     * @throws IllegalArgumentException in case of non-positive id
     */
    public static Props props(final int id) throws IllegalArgumentException {
        require(id > 0, "Allowed identifier is positive integer");
        return Props.create(Agent.class, () ->
            new Agent(id)
        );
    }
    public static String name(int id) {
        return "agent-" + id;
    }
    private static Random random = new Random();

    private final int id;
    private int counter = 0;

    private Agent(final int id) {
        this.id = id;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.Trigger.class, m -> {
                    if (m.time.isFinite()) {
                        System.out.printf("%d: %d\n", id, m.time.toMillis());
                    } else {
                        System.out.printf("%d: inf\n", id);
                    }
                    counter += 1;

                    if (counter < 10) {
                        sender().tell(
                                m.plusMillis(random.nextInt(100)),
                                getContext().self()
                        );
                    }
                    sender().tell(
                            Messages.Acknowledgement,
                            getContext().self()
                    );
                })
                .build();
    }
}
