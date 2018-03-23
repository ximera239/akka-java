package com.zhoga.akka.task4;

import akka.actor.*;
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

public class SupervisorTests extends JUnitSuite {
    private static ActorSystem system;
    private static final FiniteDuration WAIT_PERIOD = new FiniteDuration(5, TimeUnit.SECONDS);

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
    public void testFailedSupervisorInitialization() {
        Supervisor.props(-1);
    }

    @Test
    public void testSupervisorInitialization() {
        new TestKit(system) {{
            final int workersNum = 10;
            final ActorRef supervisor = system.actorOf(
                    Supervisor.props(workersNum, Duration.Inf(), getRef()),
                    Supervisor.Name
            );

            // wait for supervisor is initialized
            expectMsgEquals(WAIT_PERIOD, TestMessages.PreStartIsFinished);

            // and than test
            for (int id = 1;id <= workersNum;id++) {
                final ActorPath agentPath = supervisor.path().child(Agent.name(id));
                system.actorSelection(agentPath).
                        tell(new Identify(id), getRef());
                final ActorIdentity received = expectMsgClass(WAIT_PERIOD, ActorIdentity.class);

                assertTrue(String.format("Agent with id %d was not initialized", id), received.ref().nonEmpty());
                assertTrue(String.format("Agent with id %d has wrong correlation id", id), received.correlationId() instanceof Integer);
                assertEquals(String.format("Agent with id %d has wrong correlation id", id), ((Integer)received.correlationId()).intValue(), id);
                assertEquals(String.format("Agent with id %d has wrong address", id), received.ref().get().path(), agentPath);
            }
            final int id = workersNum + 1;
            final ActorPath schedulerPath = supervisor.path().child(Scheduler.Name);
            system.actorSelection(schedulerPath).
                    tell(new Identify(id), getRef());
            final ActorIdentity received = expectMsgClass(WAIT_PERIOD, ActorIdentity.class);

            assertTrue("Scheduler was not initialized", received.ref().nonEmpty());
            assertTrue("Scheduler has wrong correlation id", received.correlationId() instanceof Integer);
            assertEquals("Scheduler has wrong correlation id", ((Integer)received.correlationId()).intValue(), id);
            assertEquals("Scheduler has wrong address", received.ref().get().path(), schedulerPath);
        }};
    }
}
