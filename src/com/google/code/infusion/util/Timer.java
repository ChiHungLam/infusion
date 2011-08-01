package com.google.code.infusion.util;

import java.util.TimerTask;

public class Timer {
  java.util.Timer javaTimer = new java.util.Timer();
  public void schedule(TimerTask task, long delay) {
    javaTimer.schedule(task, delay);
  }
}
