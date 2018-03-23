package com.zhoga.akka.task2;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.zhoga.akka.utils.AbstractWorker;

import static com.zhoga.akka.utils.Validations.require;

/**
 * Worker actor could be configured into two behaviours:
 * - as forwarding actor (add checkpoint and forward message further)
 * - as terminal state actor, which prints checkpoints info and
 *      stops simulation
 */
public class Worker extends AbstractWorker {
    /**
     * Props factory for Worker actor
     * @param id actor unique identifier, should be greater than 0
     * @return Props object for Worker actor
     * @throws IllegalArgumentException in case of non-positive id
     */
    public static Props props(final int id) throws IllegalArgumentException {
        require(id > 0, "Allowed identifier is positive integer");
        return Props.create(Worker.class, () ->
                new Worker(id)
        );
    }
    public static String name(int id) {
        return "worker-" + id;
    }

    private final int id;
    private Worker(final int id) {
        this.id = id;
    }

    @Override
    public Receive createReceive() {
        return receiveConfigure().
                orElse(receiveEcho(null));
    }

    private Receive receiveConfigure() {
        return receiveBuilder()
                .matchEquals(Messages.PrintCheckpoints, m -> {
                    getContext().become(receivePrint().
                            orElse(receiveConfigure()).
                            orElse(receiveEcho(m)));
                })
                .match(Messages.ForwardMessage.class, m -> {
                    getContext().become(receiveForward(m.next).
                            orElse(receiveConfigure()).
                            orElse(receiveEcho(m)));
                })
                .build();
    }

    private Receive receivePrint() {
        return receiveBuilder().
                match(Messages.Message.class, m -> {
                    long time = System.currentTimeMillis();
                    m.checkpoints.forEach(c -> {
                        System.out.printf("Actor %d, message received %d\n", c.id, c.time);
                    });
                    System.out.printf("Actor %d, message received %d\n", id, time);
                }).
                build();
    }

    private Receive receiveForward(final ActorRef next) {
        return receiveBuilder().
                match(Messages.Message.class, m -> {
                    next.forward(m.checkpoint(id), getContext());
                }).
                build();
    }
}
