package com.zhoga.akka.utils;

import java.util.Objects;

public enum TestMessages {
    GetWorkerConfigurationMessage,
    PreStartIsFinished;

    public static class WorkerConfigurationMessage {
        public final Object message;

        public WorkerConfigurationMessage(final Object message) {
            this.message = message;
        }

        public boolean isEmpty() {
            return message == null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WorkerConfigurationMessage that = (WorkerConfigurationMessage) o;
            return Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }
    }
}
