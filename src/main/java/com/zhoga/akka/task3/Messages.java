package com.zhoga.akka.task3;

import akka.actor.ActorRef;

import java.util.Objects;

import static com.zhoga.akka.utils.Validations.require;

/**
 * Messages class contains classes which are
 * used as messages for actors
 */
public class Messages {
    public static class MessageToActor {
        public final int id;
        public final String message;

        public MessageToActor(final int id, final String message) {
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
    public static class ForwardMessage {
        public final ActorRef prev;
        public final ActorRef next;

        public ForwardMessage(final ActorRef prev, final ActorRef next) {
            this.prev = prev;
            this.next = next;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ForwardMessage that = (ForwardMessage) o;
            return Objects.equals(prev, that.prev) &&
                    Objects.equals(next, that.next);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prev, next);
        }
    }
}
