package com.zhoga.akka.task4;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Main class to run simulation
 *
 * Simulation description: one Scheduler actor and n Agent actors
 * are setup. Each agent is able to response on received Trigger
 * message with sequential messages up to 9 times: new Trigger with increased time
 * and Acknowledgement, and 1 time with just Acknowledgement.
 * Scheduler contains prioritized queue with Triggers for specific agent (where
 * least time - the most priority), initialy one trigger for each
 * agent. On Start message or on Acknowledgement message most prioritized
 * Trigger is taken from queue and sent to appropriate Agent.
 *
 *
 * Number of agent actors is specified as input parameter.
 * Main class initialize Scheduler actor and than sends
 * Start message to Scheduler to run simulation
 */
public class Main {
    public static void main(String...args) {
        if (args.length == 0) {
            System.out.println("Required number of agents as input parameter");
        } else {
            try {
                final int agentsNum = Integer.parseInt(args[0]);
                final Props SupervisorProps = Supervisor.props(agentsNum);

                final ActorSystem system = ActorSystem.create("test-system");
                final ActorRef supervisor = system.actorOf(
                        SupervisorProps,
                        Supervisor.Name
                );
                supervisor.tell(
                        Messages.Start,
                        null
                );

            } catch (NumberFormatException e) {
                System.out.println("Number of workers should be int");
            } catch (IllegalArgumentException e) {
                System.out.println("Illegal argument: " + e.getMessage());
            }
        }
    }
}
