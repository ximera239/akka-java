package com.zhoga.akka.utils;

import akka.actor.Props;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class Defaults {
    public static final Duration TERMINATE_AFTER_DURATION = new FiniteDuration(5, TimeUnit.SECONDS);
}
