package com.zhoga.akka.task4;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.zhoga.akka.utils.TestMessages;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AgentTests extends JUnitSuite {
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

    @Test(expected = IllegalArgumentException.class)
    public void testFailedAgentInitialization() {
        Agent.props(-1);
    }

    @Test
    public void testWorkersInitialization() {
        new TestKit(system) {{
            final ActorRef worker = system.actorOf(
                    Agent.props(1),
                    Agent.name(1)
            );

            final Messages.Trigger trigger = new Messages.Trigger(100);
            worker.tell(trigger, getRef());
            final Messages.Trigger receivedTrigger = expectMsgClass(WAIT_PERIOD, Messages.Trigger.class);
            assertTrue("Wrong time in received trigger", receivedTrigger.time.$greater$eq(trigger.time));
        }};
    }

    @Test
    public void testWorkerShouldAnswer9Times() {
        new TestKit(system) {{
            final ActorRef worker = system.actorOf(
                    Agent.props(2),
                    Agent.name(2)
            );

            for (int i = 0;i < 9;i++) {
                final Messages.Trigger trigger = new Messages.Trigger(100);
                worker.tell(trigger, getRef());
                final Messages.Trigger receivedTrigger = expectMsgClass(WAIT_PERIOD, Messages.Trigger.class);
                assertTrue("Wrong time in received trigger", receivedTrigger.time.$greater$eq(trigger.time));
                expectMsgEquals(WAIT_PERIOD, Messages.Acknowledgement);
            }
            final Messages.Trigger trigger = new Messages.Trigger(100);
            worker.tell(trigger, getRef());
            expectMsgEquals(WAIT_PERIOD, Messages.Acknowledgement);
            expectNoMessage(NO_MESSAGE_WAIT_PERIOD);
        }};
    }

    @Test
    public void testInfiniteDurationSupport() {
        new TestKit(system) {{
            final ActorRef worker = system.actorOf(
                    Agent.props(3),
                    Agent.name(3)
            );

            final Messages.Trigger trigger = new Messages.Trigger(Duration.Inf());
            worker.tell(trigger, getRef());
            final Messages.Trigger received = expectMsgClass(WAIT_PERIOD, Messages.Trigger.class);
            assertTrue("Received trigger contains finite time", !received.time.isFinite());
        }};
    }
}
