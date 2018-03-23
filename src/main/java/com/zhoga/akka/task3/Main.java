package com.zhoga.akka.task3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Random;

/**
 * Main class to run simulation
 *
 * Simulation description: created n actors with unique ids
 * from 1 to n. THey are organized in "ring", where each actor
 * has prev and next siblings, having accordingly id less by 1 or
 * greate by 1. For actor with id 1 prev sibling has id n, and
 * for actor with id n, next sibling has id 1.
 * Simulation starts with message sent to random actor in ring.
 * If sender is not prev or next sibling, message is forwarded
 * to both (prev and next). If message is received from prev sibling
 * first time, it is forwarded to next sibling, otherwise ignored.
 * If message is received from next sibling for the first time,
 * it is forwarded to prev sibling, otherwise ignored.
 * As soon as actor receive message from both sides, it print message
 * received last (from second sibling).
 *
 * Number of worker actors is specified as input parameter.
 * Main class initialize Supervisor actor and than sends
 * message for actor with random id between 1 and n where n
 * is number of workers
 */
public class Main {
    public static void main(String...args) {
        if (args.length == 0) {
            System.out.println("Required number of workers as input parameter");
        } else {
            try {
                final int workersNum = Integer.parseInt(args[0]);
                final Props SupervisorProps = Supervisor.props(workersNum);

                final ActorSystem system = ActorSystem.create("test-system");
                final ActorRef ref = system.actorOf(
                        SupervisorProps,
                        Supervisor.Name
                );
                final int id = new Random().nextInt(workersNum) + 1;
                ref.tell(new Messages.MessageToActor(id, String.format("Hello from: %d", id)), null);

            } catch (NumberFormatException e) {
                System.out.println("Number of workers should be int");
            } catch (IllegalArgumentException e) {
                System.out.println("Illegal argument: " + e.getMessage());
            }
        }
    }
}
