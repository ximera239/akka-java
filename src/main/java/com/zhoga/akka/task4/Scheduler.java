package com.zhoga.akka.task4;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.zhoga.akka.utils.Defaults.TERMINATE_AFTER_DURATION;
import static com.zhoga.akka.utils.Validations.require;

/**
 * Scheduler actor init agent actors, setup terminal state
 * for simulation (as 5 seconds ReceiveTimeout on Supervisor)
 */
public class Scheduler extends AbstractActor {
    /**
     * Props factory for Scheduler actor
     * @param agents agents to communicate with
     * @return Props object for Scheduler actor
     * @throws IllegalArgumentException in case of illegal number of agents
     */
    public static Props props(final List<ActorRef> agents) {
        return Props.create(Scheduler.class, () ->
            new Scheduler(agents)
        );
    }
    public final static String Name = "scheduler";

    private final List<ActorRef> agents;
    private final PriorityQueue<TriggerHolder> queue;
    private static Random random = new Random();

    private Scheduler(final List<ActorRef> agents) {
        this.agents = agents;
        this.queue = new PriorityQueue<>(Math.max(1, agents.size()),
                (o1, o2) -> (int) (o1.trigger.time.compareTo(o2.trigger.time))
        );
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        // iterate all ids to init agents
        for(ActorRef agent: agents) {
            // for each agent trigger is added into queue
            queue.add(
                    new TriggerHolder(
                            new Messages.Trigger(random.nextInt(100)),
                            agent
                    )
            );
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().
                matchEquals(Messages.Start, m -> {
                    send();
                }).
                match(Messages.Trigger.class, m -> {
                    queue.add(new TriggerHolder(m, getContext().sender()));
                }).
                matchEquals(Messages.Acknowledgement, m -> {
                    send();
                }).
                build();
    }


    private void send() {
        if (!queue.isEmpty()) {
            TriggerHolder h = queue.poll();
            h.agent.tell(
                    h.trigger,
                    self()
            );
        }
    }

    private static class TriggerHolder {
        final Messages.Trigger trigger;
        final ActorRef agent;

        TriggerHolder(final Messages.Trigger trigger, final ActorRef agent) {
            this.trigger = trigger;
            this.agent = agent;
        }
    }
}
