package com.zhoga.akka.task3;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.zhoga.akka.utils.TestMessages;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.zhoga.akka.utils.Defaults.TERMINATE_AFTER_DURATION;
import static com.zhoga.akka.utils.Validations.require;

/**
 * Supervisor actor init worker actors, setup terminal state
 * for simulation (as 5 seconds ReceiveTimeout on Supervisor)
 */
public class Supervisor extends AbstractActor {
    /**
     * Props factory for Supervisor actor
     * Allows 2 workers to setup, but actually this case will fail
     * because of worker initialization exception,
     *
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
        require(workersNum > 2, "Amount of workers should be 3 or more!");
        return Props.create(Supervisor.class, () ->
                new Supervisor(workersNum, terminateAfterInactive, reportTo)
        );
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

    public static final SupervisorStrategy strategy =
            new OneForOneStrategy(10, Duration.create(1, TimeUnit.MINUTES),
                    DeciderBuilder
                            .match(IllegalArgumentException.class, e -> {
                                System.out.printf("Got Illegal argument exception with message [%s]. Will stop actor.", e.getMessage());
                                return SupervisorStrategy.stop();
                            })
                            .matchAny(o -> SupervisorStrategy.escalate())
                            .build());
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        // init workers
        for(int id = 1;id <= workersNum;id++) {
            getContext().actorOf(
                    Worker.props(id),
                    Worker.name(id)
            );
        }

        // iterate all workers while setup prev and next siblings
        for(int id = 1;id <= workersNum;id++) {
            final int prevId = ( (id - 2 + workersNum) % workersNum + 1);
            final int nextId = ( (id + workersNum) % workersNum + 1);
            final ActorRef prev = getContext().getChild(Worker.name(prevId));
            final ActorRef next = getContext().getChild(Worker.name(nextId));
            final ActorRef cur = getContext().getChild(Worker.name(id));

            cur.tell(new Messages.ForwardMessage(prev, next), null);
        }

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
