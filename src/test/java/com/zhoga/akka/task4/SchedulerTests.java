package com.zhoga.akka.task4;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class SchedulerTests extends JUnitSuite {
    static ActorSystem system;
    static final FiniteDuration NO_MESSAGE_WAIT_PERIOD = new FiniteDuration(1, TimeUnit.SECONDS);
    static final FiniteDuration WAIT_PERIOD = new FiniteDuration(5, TimeUnit.SECONDS);

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testWorkersInitialization() {
        new TestKit(system) {{
            final List<ActorRef> agents = new ArrayList<>();
            agents.add(getRef());
            final ActorRef scheduler = system.actorOf(
                    Scheduler.props(agents),
                    Scheduler.Name + "-test1"
            );

            scheduler.tell(Messages.Start, null);
            final Messages.Trigger receivedTrigger = expectMsgClass(WAIT_PERIOD, Messages.Trigger.class);
            final long millis = receivedTrigger.time.toMillis();
            assertTrue("Wrong time in initial scheduler trigger", millis >=0 && millis <= 100);
            scheduler.tell(Messages.Acknowledgement, null);
            expectNoMessage(NO_MESSAGE_WAIT_PERIOD);
        }};
    }

    @Test
    public void testInfiniteDurationSupport() {
        new TestKit(system) {{
            final List<ActorRef> agents = new ArrayList<>();
            agents.add(getRef());
            final ActorRef scheduler = system.actorOf(
                    Scheduler.props(agents),
                    Scheduler.Name + "-test2"
            );

            scheduler.tell(Messages.Start, null);
            expectMsgClass(WAIT_PERIOD, Messages.Trigger.class);

            final Messages.Trigger trigger = new Messages.Trigger(Duration.Inf());
            scheduler.tell(trigger, getRef());
            scheduler.tell(Messages.Acknowledgement, null);
            final Messages.Trigger received = expectMsgClass(WAIT_PERIOD, Messages.Trigger.class);
            assertTrue("Received trigger contains finite time", !received.time.isFinite());
        }};
    }
}
