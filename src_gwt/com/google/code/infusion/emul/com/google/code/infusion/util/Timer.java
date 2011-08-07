package com.google.code.infusion.util;

import java.util.TimerTask;

/**
 * This is a wrapper for timer because in development mode, the original
 * runtime library is used, not the super source emulation.
 */
public class Timer {
  public void schedule(final TimerTask task, long delay) {
    com.google.gwt.user.client.Timer t = new com.google.gwt.user.client.Timer() {
      public void run() {
        task.run();
      }
    };
    t.schedule((int) delay);
  }
  
}