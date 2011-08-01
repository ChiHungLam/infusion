package java.util;

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