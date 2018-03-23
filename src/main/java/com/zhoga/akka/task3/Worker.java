package com.zhoga.akka.task3;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.zhoga.akka.utils.AbstractWorker;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.zhoga.akka.utils.Validations.require;

/**
 * Worker actor could be configured with prev and next sibling actors,
 * which should be different (otherwise actor will be stopped with
 * IllegalArgumentException while initializing)
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
    private static Random random = new Random();

    private final int id;
    private boolean receivedPrev = false;
    private boolean receivedNext = false;

    private Worker(final int id) {
        this.id = id;
    }

    private void printConditional(final String msg) {
        if (receivedNext && receivedPrev) {
            System.out.printf("Actor %d got message [%s]\n", id, msg);
        }
    }

    @Override
    public Receive createReceive() {
        return receiveConfigure().
                orElse(receiveEcho(null));
    }

    private Receive receiveConfigure() {
        return receiveBuilder()
                .match(Messages.ForwardMessage.class, m -> {
                    getContext().become(receiveForward(m.prev, m.next).
                            orElse(receiveConfigure()).
                            orElse(receiveEcho(m)));
                })
                .build();
    }

    private Receive receiveForward(final ActorRef prev, final ActorRef next) {
        require(!prev.equals(next), String.format("Failed to init actor with id: %d. Prev and next should be different actors!", id));
        return receiveBuilder().
                match(String.class, m -> {
                    if (prev.equals(sender()) && !receivedPrev) {
                        receivedPrev = true;
                        printConditional(m);
                        getContext().system().scheduler().scheduleOnce(
                                new FiniteDuration(random.nextInt(100) + 1, TimeUnit.MILLISECONDS),
                                next,
                                m,
                                getContext().dispatcher(),
                                self()
                        );
                    } else if (next.equals(sender()) && !receivedNext) {
                        receivedNext = true;
                        printConditional(m);
                        getContext().system().scheduler().scheduleOnce(
                                new FiniteDuration(random.nextInt(100) + 1, TimeUnit.MILLISECONDS),
                                prev,
                                m,
                                getContext().dispatcher(),
                                self()
                        );
                    } else if (!next.equals(sender()) && !prev.equals(sender())) {
                        next.tell(m, self());
                        prev.tell(m, self());
                    }
                }).
                build();
    }
}
