package com.zhoga.akka.task2;

import akka.actor.ActorRef;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Messages class contains classes and enum objects
 * which are used as messages for actors
 */
public enum Messages {
    PrintCheckpoints;

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

    public static class Checkpoint {
        public final int id;
        public final long time;

        private Checkpoint(final int id, final long time) {
            this.id = id;
            this.time = time;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Checkpoint that = (Checkpoint) o;
            return id == that.id &&
                    time == that.time;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, time);
        }
    }

    public static class Message {
        public final long started;
        public final List<Checkpoint> checkpoints;

        public Message() {
            started = System.currentTimeMillis();
            checkpoints = Collections.unmodifiableList(new LinkedList<Checkpoint>());
        }
        private Message(final long started,
                        final List<Checkpoint> checkpoints) {
            this.started = started;
            this.checkpoints = Collections.unmodifiableList(checkpoints);
        }
        public Message checkpoint(final int id) {
            final List<Checkpoint> newCheckpoints = new LinkedList(checkpoints);
            newCheckpoints.add(new Checkpoint(id, System.currentTimeMillis()));
            return new Message(started, newCheckpoints);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Message message = (Message) o;
            return started == message.started &&
                    Objects.equals(checkpoints, message.checkpoints);
        }

        @Override
        public int hashCode() {
            return Objects.hash(started, checkpoints);
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
