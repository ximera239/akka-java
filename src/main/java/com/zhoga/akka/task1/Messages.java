package com.zhoga.akka.task1;

import akka.actor.ActorRef;

import java.util.Objects;

/**
 * Messages class contains classes and enum objects
 * which are used as messages for actors
 */
public enum Messages {
    PrintHops;
    public static class MessageToActor {
        public final int id;
        public final Message message;

        public MessageToActor(final int id, final Message message) {
            this.id = id;
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageToActor that = (MessageToActor) o;
            return id == that.id &&
                    Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, message);
        }
    }

    public static class Message {
        public final int numberOfHopsTravelled;

        public Message() {
            this.numberOfHopsTravelled = 0;
        }
        private Message(final int numberOfHopsTravelled) {
            this.numberOfHopsTravelled = numberOfHopsTravelled;
        }
        public Message plusHop() {
            return new Message(numberOfHopsTravelled + 1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Message message = (Message) o;
            return numberOfHopsTravelled == message.numberOfHopsTravelled;
        }

        @Override
        public int hashCode() {
            return Objects.hash(numberOfHopsTravelled);
        }
    }

    public static class ForwardMessage {
        public final ActorRef next;

        public ForwardMessage(final ActorRef next) {
            this.next = next;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ForwardMessage that = (ForwardMessage) o;
            return Objects.equals(next, that.next);
        }

        @Override
        public int hashCode() {
            return Objects.hash(next);
        }
    }
}
