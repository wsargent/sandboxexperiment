package com.tersesystems.sandboxexperiment.sandbox


/**
  *
  */
class ThreadSpawner {

  /*
  http://stackoverflow.com/a/17536809/5266

  Instead what it does (by default) is to always grant access unless the ThreadGroup in question is the system thread group in which case the "modifyThreadGroup" permission is checked. The ThreadGroup in question is almost never the system thread group.
   */
  def run() = {
    /*
    Applications that want a stricter policy should override this method. If this method is overridden, the method that overrides it should additionally check to see if the calling thread has the RuntimePermission("modifyThreadGroup") permission, and if so, return silently. This is to ensure that code granted that permission (such as the JDK itself) is allowed to manipulate any thread.
     */
    val t = new Thread(new Runnable() {
      override def run() {
        System.out.println("Thread.run()")
      }
    })
    t.run()
  }

}
