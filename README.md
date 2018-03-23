**Trial tasks set made with usage of java/akka**

**Task 1**

Build an application with n‐actors (n can should be specifiable as input to the application). Each actor should be assigned an id between 1 to n (unique). In the main program a message should be sent to the actor with id 1. The message object should contain a field numberOfHopsTravelled (int, initialized to 0).

Actor 1 forwards the message to the actor with id 2 when receiving the message from actor 1, but before doing so, increases the numberOfHopsTravelled by one in the message. This continues in this way until the message reaches actor n. When actor n receives the message, it prints out the numberOfHopsTravelled field in the received message.


**Task 2**

Adapt task 1, so that we can measure the messaging time between each actor. However, the messaging time should still only be printed out by the last actor (n). The output should be in the following format:

actor 1, message received <time‐in‐milli‐seconds> 
actor 2, message received <time‐in‐milli‐seconds> 

...

actor n, message received <time‐in‐milli‐seconds>

Hint: You are allowed to adapt/extend the messages being sent among the actors to achieve this.


**Task 3**

Create an n‐actor system (unique ids from 1 to n), where each actor i can only send and receive messages to/from actor i‐1 and i+1 (actor n can communicate with actor n‐1 and actor 0).

In the main program, we send a String message (“hello from: x ”) to one random actor x. That random actor sends the message to its two neighbors. Each neighbor receiving the message just forwards it to that neighbor which did not send the message to it, after introducing a random delay between 1 and 100ms.

When an actor has received a message from both of its neighbors, it prints out its own Id and the message it received.


**Task 4**

Create a system with n agents (consisting of actors with unique ids from 1 to n). Furthermore create one additional actor which we refer to as a “scheduler”. Messages are passed between the agents and the scheduler, containing a time (between 0 and +inf). There are two types of messages: 1.) Trigger 2.) Acknowledgement. Each agent receiving a Trigger message from the scheduler and sends a new Trigger message to the scheduler (with time of received trigger + random number between 0 and100. It also sends an Acknowledgement message to the scheduler after that. The agent repeats such behavior for max 10 times (meaning during the whole program it receives 10 Trigger messages from scheduler, sends 9 Trigger messages back to the Scheduler and sends 10 Acknowledgement messages to back 10 scheduler; there is one less Trigger message sent back, in order to eventually stop the “simulation”).

The scheduler upon receiving a Trigger messages stores the message and actor reference it in a sorted Trigger queue (sorted by time). Whenever the Scheduler receives an Acknowledgement message and there are still more Triggers in the sorted queue, it sends out the first Trigger message to the corresponding agent actor.

Each agent should write down in the console log, its id and Trigger message time whenever it receives a new Trigger message from the scheduler.

The scheduler is initialized with one Trigger message per agent (with random time between 0 and 100). After initialization a Start message is sent to the scheduler, which initiates the “simulation”: The scheduler removes the first message in the sorted Trigger queue and sends the first Trigger to the corresponding agent.



**Implementation details**

Code was written with usage of Java, Oracle HotSpot JDK version 1.8 build 102

Sbt is used to manage dependencies

To build fat jar you need to use

```
sbt assembly
```

 