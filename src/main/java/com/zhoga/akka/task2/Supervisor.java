package com.zhoga.akka.task2;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import com.zhoga.akka.utils.TestMessages;
import scala.concurrent.duration.Duration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zhoga.akka.utils.Defaults.TERMINATE_AFTER_DURATION;
import static com.zhoga.akka.utils.Validations.require;

/**
 * Supervisor actor init worker actors, setup terminal state
 * for simulation (as 5 seconds ReceiveTimeout on Supervisor)
 */
public class Supervisor extends AbstractActor {
    /**
     * Props factory for Supervisor actor
     * @param workersNum amount of worker actors to create
     * @return Props object for Supervisor actor
     * @throws IllegalArgumentException in case of illegal number of workers
     */
    public static Props props(final Integer workersNum) throws IllegalArgumentException {
        return props(workersNum, TERMINATE_AFTER_DURATION, null);
    }
    public static Props props(final Integer workersNum,
                              final Duration terminateAfterInactive,
                              final ActorRef reportTo) throws IllegalArgumentException {
        require(workersNum > 0, "Amount of workers should be greater than 0!");
        return Props.create(Supervisor.class, () ->
                new Supervisor(workersNum, terminateAfterInactive, reportTo));
    }
    public final static String Name = "supervisor";

    private final int workersNum;
    private final Duration terminateAfterInactive;
    private final ActorRef reportTo;

    private Supervisor(final Integer workersNum,
                       final Duration terminateAfterInactive,
                       final ActorRef reportTo) {
        this.workersNum = workersNum;
        this.terminateAfterInactive = terminateAfterInactive;
        this.reportTo = reportTo;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        // generates reversed sequence of workers
        final List<ActorRef> workers = Stream.iterate(workersNum, i -> i - 1).
                limit(workersNum).
                map(id -> getContext().actorOf(
                        Worker.props(id),
                        Worker.name(id)
                )).collect(Collectors.toList());
        // init last worker to print hops/checkpoints info
        workers.get(0).tell(Messages.PrintCheckpoints, null);
        // init other workers to forward message, returning first ref
        final ActorRef first = workers.stream().skip(1).
                reduce(
                        workers.get(0),
                        (acc, next) -> {
                            next.tell(new Messages.ForwardMessage(acc), null);
                            return next;
                        });
        // setup receive timeout to terminate simulation
        getContext().setReceiveTimeout(terminateAfterInactive);
        if (reportTo != null) {
            reportTo.tell(TestMessages.PreStartIsFinished, self());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().
                match(Messages.MessageToActor.class, m -> {
                    getContext().findChild(Worker.name(m.id)).ifPresent(ref -> {
                        ref.tell(m.message, null);
                    });
                }).
                match(ReceiveTimeout.class, m -> {
                    getContext().getSystem().terminate();
                }).
                build();
    }

}
