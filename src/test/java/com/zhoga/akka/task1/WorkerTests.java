package com.zhoga.akka.task1;

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
            assertTrue("Worker configuration should be empty", received1.isEmpty());

            worker.tell(new Messages.ForwardMessage(getRef()), getRef());
            worker.tell(TestMessages.GetWorkerConfigurationMessage, getRef());
            final TestMessages.WorkerConfigurationMessage received2 = expectMsgClass(WAIT_PERIOD, TestMessages.WorkerConfigurationMessage.class);
            assertTrue("Worker has wrong configuration", received2.message instanceof Messages.ForwardMessage);
            assertEquals("Worker has wrong next ref", ((Messages.ForwardMessage)received2.message).next.path(), getRef().path());

            worker.tell(Messages.PrintHops, getRef());
            worker.tell(TestMessages.GetWorkerConfigurationMessage, getRef());
            final TestMessages.WorkerConfigurationMessage received3 = expectMsgClass(WAIT_PERIOD, TestMessages.WorkerConfigurationMessage.class);
            assertEquals("Worker has wrong configuration", received3.message, Messages.PrintHops);
        }};
    }
    @Test
    public void testWorkerIncreasesHopsNumber() {
        new TestKit(system) {{
            final ActorRef worker = system.actorOf(
                    Worker.props(2),
                    Worker.name(2)
            );
            worker.tell(new Messages.ForwardMessage(getRef()), getRef());
            final Messages.Message toSend = new Messages.Message();
            assertEquals("Wrong initial number of hops", toSend.numberOfHopsTravelled, 0);

            worker.tell(toSend, getRef());
            final Messages.Message received = expectMsgClass(WAIT_PERIOD, Messages.Message.class);

            assertEquals("Wrong number of hops", received.numberOfHopsTravelled, 1);
        }};
    }

}
