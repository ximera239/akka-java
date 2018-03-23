package com.zhoga.akka.task4;

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
     * @param agentsNum amount of worker actors to create
     * @return Props object for Supervisor actor
     * @throws IllegalArgumentException in case of illegal number of workers
     */
    public static Props props(final Integer agentsNum) throws IllegalArgumentException {
        return props(agentsNum, TERMINATE_AFTER_DURATION, null);
    }
    public static Props props(final Integer agentsNum,
                              final Duration terminateAfterInactive,
                              final ActorRef reportTo) throws IllegalArgumentException {
        require(agentsNum >= 0, "Amount of agents should be 0 or more!");
        return Props.create(
                Supervisor.class,
                () -> new Supervisor(agentsNum, terminateAfterInactive, reportTo));
    }
    public final static String Name = "supervisor";

    private final int agentsNum;
    private final Duration terminateAfterInactive;
    private final ActorRef reportTo;

    private Supervisor(final Integer agentsNum,
                       final Duration terminateAfterInactive,
                       final ActorRef reportTo) {
        this.agentsNum = agentsNum;
        this.terminateAfterInactive = terminateAfterInactive;
        this.reportTo = reportTo;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        // create agents
        final List<ActorRef> agents = Stream.iterate(agentsNum, i -> i - 1).
                limit(agentsNum).
                map(id -> getContext().actorOf(
                        Agent.props(id),
                        Agent.name(id)
                )).collect(Collectors.toList());
        // create scheduler
        getContext().actorOf(
                Scheduler.props(agents),
                Scheduler.Name
        );
        // setup receive timeout to terminate simulation
        getContext().setReceiveTimeout(terminateAfterInactive);
        if (reportTo != null) {
            reportTo.tell(TestMessages.PreStartIsFinished, self());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().
                matchEquals(Messages.Start, m -> {
                    getContext().findChild(Scheduler.Name).ifPresent(ref -> {
                        ref.forward(m, getContext());
                    });
                }).
                match(ReceiveTimeout.class, m -> {
                    getContext().getSystem().terminate();
                }).
                build();
    }

}
