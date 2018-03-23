package com.zhoga.akka.task4;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Messages class contains classes and enum objects
 * which are used as messages for actors
 */
public enum Messages {
    Start, Acknowledgement;

    public static class Trigger {
        public final Duration time;
        public Trigger(final Duration time) {
            this.time = time;
        }
        public Trigger(final long millis) {
            this(new FiniteDuration(millis, TimeUnit.MILLISECONDS));
        }

        public Trigger plusMillis(int millis) {
            return new Trigger(time.plus(new FiniteDuration(millis, TimeUnit.MILLISECONDS)));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Trigger trigger = (Trigger) o;
            return Objects.equals(time, trigger.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(time);
        }
    }

}
