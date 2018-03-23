package com.zhoga.akka.task3;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.zhoga.akka.utils.TestMessages;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
        Supervisor.props(0);
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
                final ActorPath workerPath = supervisor.path().child(Worker.name(id));
                final int prevId = ( (id - 2 + workersNum) % workersNum + 1);
                final int nextId = ( (id + workersNum) % workersNum + 1);
                final ActorPath nextWorkerPath = supervisor.path().child(Worker.name(nextId));
                final ActorPath prevWorkerPath = supervisor.path().child(Worker.name(prevId));
                system.actorSelection(workerPath).
                        tell(TestMessages.GetWorkerConfigurationMessage, getRef());
                final TestMessages.WorkerConfigurationMessage received = expectMsgClass(WAIT_PERIOD, TestMessages.WorkerConfigurationMessage.class);

                assertTrue(String.format("Worker with id %d has wrong configuration", id), received.message instanceof Messages.ForwardMessage);
                assertEquals(String.format("Worker with id %d has wrong next ref", id), ((Messages.ForwardMessage)received.message).next.path(), nextWorkerPath);
                assertEquals(String.format("Worker with id %d has wrong prev ref", id), ((Messages.ForwardMessage)received.message).prev.path(), prevWorkerPath);
            }
        }};
    }
}
