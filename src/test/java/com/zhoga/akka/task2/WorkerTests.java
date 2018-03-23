package com.zhoga.akka.task2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.zhoga.akka.utils.TestMessages;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorkerTests extends JUnitSuite {
    static ActorSystem system;
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
    public void testFailedWorkerInitialization() {
        Worker.props(-1);
    }

    @Test
    public void testWorkersInitialization() {
        new TestKit(system) {{
            final ActorRef worker = system.actorOf(
                    Worker.props(1),
                    Worker.name(1)
            );

            worker.tell(TestMessages.GetWorkerConfigurationMessage, getRef());
            final TestMessages.WorkerConfigurationMessage received1 = expectMsgClass(WAIT_PERIOD, TestMessages.WorkerConfigurationMessage.class);
            assertTrue("Non-empty initial state!", received1.isEmpty());

            worker.tell(new Messages.ForwardMessage(getRef()), getRef());
            worker.tell(TestMessages.GetWorkerConfigurationMessage, getRef());
            final TestMessages.WorkerConfigurationMessage received2 = expectMsgClass(WAIT_PERIOD, TestMessages.WorkerConfigurationMessage.class);
            assertTrue("Wrong configuration message received from worker!", received2.message instanceof Messages.ForwardMessage);
            assertEquals("Wrong next ref!", ((Messages.ForwardMessage)received2.message).next.path(), getRef().path());

            worker.tell(Messages.PrintCheckpoints, getRef());
            worker.tell(TestMessages.GetWorkerConfigurationMessage, getRef());
            final TestMessages.WorkerConfigurationMessage received3 = expectMsgClass(WAIT_PERIOD, TestMessages.WorkerConfigurationMessage.class);
            assertEquals("Wrong configuration message received from worker!", received3.message, Messages.PrintCheckpoints);
        }};
    }

    @Test
    public void testWorkerAddCheckpoint() {
        new TestKit(system) {{
            final ActorRef worker = system.actorOf(
                    Worker.props(2),
                    Worker.name(2)
            );
            worker.tell(new Messages.ForwardMessage(getRef()), getRef());
            final Messages.Message toSend = new Messages.Message();
            assertEquals("Initial number of checkpoints non-zero!", toSend.checkpoints.size(), 0);
            final long startTimestamp = System.currentTimeMillis();

            worker.tell(toSend, getRef());
            final Messages.Message received = expectMsgClass(WAIT_PERIOD, Messages.Message.class);
            final long endTimestamp = System.currentTimeMillis();

            assertEquals("Wrong number of checkpoints, should be 1!", received.checkpoints.size(), 1);
            final Messages.Checkpoint checkpoint = received.checkpoints.get(0);
            assertTrue("Checkpoint timestamp is out of range!",
                    startTimestamp <= checkpoint.time && checkpoint.time <= endTimestamp);
        }};
    }

}
