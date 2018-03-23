package com.zhoga.akka.task2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Main class to run simulation
 *
 * Simulation description: run requested amount of actors. Each
 * actor has unique sequential id from 1 to n. Message is sent
 * to actor with id 1, after actor either increase save time it
 * receives message and forward message to actor with id i+1, or,
 * if no more actors, print info on all saved checkpoints.
 *
 * Number of worker actors is specified as input parameter.
 * Main class initialize Supervisor actor and than sends
 * message for actor with id 1
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
                ref.tell(new Messages.MessageToActor(1, new Messages.Message()), null);
            } catch (NumberFormatException e) {
                System.out.println("Number of workers should be int");
            } catch (IllegalArgumentException e) {
                System.out.println("Illegal argument: " + e.getMessage());
            }
        }
    }
}
