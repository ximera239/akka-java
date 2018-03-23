package com.zhoga.akka.task3;

import akka.actor.*;
import akka.testkit.TestProbe;
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


    private static class EmptyActor extends AbstractActor {
        public Receive createReceive() {
            return receiveBuilder().build();
        }
    }
    private Props emptyActorProps() {
        return Props.create(EmptyActor.class, EmptyActor::new);
    }

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
    public void testFailedWorkerConfiguration() {
        new TestKit(system) {{
            final ActorRef self = getRef();
            final int workerId = 100;
            final String initialized = "initialized";

            final ActorRef supervisor = system.actorOf(Props.create(AbstractActor.class, () -> {
                return new AbstractActor() {
                    @Override
                    public SupervisorStrategy supervisorStrategy() {
                        return Supervisor.strategy;
                    }

                    @Override
                    public void preStart() throws Exception {
                        super.preStart();
                        final ActorRef w = getContext().actorOf(
                                Worker.props(workerId),
                                Worker.name(workerId)
                        );
                        watch(w);
                        self.tell(initialized, null);
                    }

                    public Receive createReceive() {
                        return receiveBuilder().
                                match(Terminated.class, m -> {
                                    self.forward(m, getContext());
                                }).build();
                    }
                };
            }));

            expectMsgEquals(initialized);
            final ActorPath workerPath = supervisor.path().child(Worker.name(workerId));
            system.actorSelection(workerPath).
                    tell(new Messages.ForwardMessage(getRef(), getRef()), getRef());
            final Terminated msg = expectMsgClass(Terminated.class);
            assertEquals("Wrong actor was terminated", msg.getActor().path(), workerPath);
        }};
    }

    @Test
    public void testWorkersInitialization() {
        new TestKit(system) {{
            final ActorRef worker = system.actorOf(
                    Worker.props(1),
                    Worker.name(1)
            );

            final ActorRef sourceRef = system.actorOf(emptyActorProps());

            worker.tell(TestMessages.GetWorkerConfigurationMessage, getRef());
            final TestMessages.WorkerConfigurationMessage received1 = expectMsgClass(WAIT_PERIOD, TestMessages.WorkerConfigurationMessage.class);
            assertTrue("Worker has wrong initial configuration", received1.isEmpty());

            worker.tell(new Messages.ForwardMessage(sourceRef, getRef()), getRef());
            worker.tell(TestMessages.GetWorkerConfigurationMessage, getRef());
            final TestMessages.WorkerConfigurationMessage received2 = expectMsgClass(WAIT_PERIOD, TestMessages.WorkerConfigurationMessage.class);
            assertTrue("Worker has wrong configuration", received2.message instanceof Messages.ForwardMessage);
            assertEquals("Worker has wrong next ref", ((Messages.ForwardMessage)received2.message).next.path(), getRef().path());
            assertEquals("Worker has wrong prev ref", ((Messages.ForwardMessage)received2.message).prev.path(), sourceRef.path());
        }};
    }

    @Test
    public void testWorkerForward() {
        new TestKit(system) {{
            final ActorRef worker = system.actorOf(
                    Worker.props(2),
                    Worker.name(2)
            );
            final ActorRef sourceRef = system.actorOf(emptyActorProps());
            worker.tell(new Messages.ForwardMessage(sourceRef, getRef()), getRef());

            final String message = "test";
            worker.tell(message, sourceRef);
            final String received = expectMsgClass(WAIT_PERIOD, String.class);

            assertEquals("Wrong message received", received, message);
        }};
    }
    @Test
    public void testWorkerForwardOppositeDirection() {
        new TestKit(system) {{
            final ActorRef worker = system.actorOf(
                    Worker.props(3),
                    Worker.name(3)
            );
            final ActorRef sourceRef = system.actorOf(emptyActorProps());
            worker.tell(new Messages.ForwardMessage(getRef(), sourceRef), getRef());

            final String message = "test";
            worker.tell(message, sourceRef);
            final String received = expectMsgClass(WAIT_PERIOD, String.class);

            assertEquals("Wrong message received", received, message);
        }};
    }

}
