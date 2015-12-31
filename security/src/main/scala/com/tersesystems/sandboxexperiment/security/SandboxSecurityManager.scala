package com.tersesystems.sandboxexperiment.security

import java.security.Policy

/**
 * A passthrough to the security policy.
  *
  * Also prevents sandboxed code from spawning threads.
  *
  * The default securitymanager does NOT stop sandboxed code from spawning threads.
 */
class SandboxSecurityManager(scriptName:String) extends SecurityManager {
  Policy.setPolicy(new SandboxPolicy(scriptName))

  override def checkAccess(threadGroup:ThreadGroup): Unit = {
    super.checkAccess(threadGroup)

    // https://docs.oracle.com/javase/8/docs/api/java/lang/SecurityManager.html#checkAccess-java.lang.ThreadGroup-
    // Applications that want a stricter policy should override this method.
    // If this method is overridden, the method that overrides it should additionally
    // check to see if the calling thread has the RuntimePermission("modifyThreadGroup")
    // permission, and if so, return silently. This is to ensure that code granted that
    // permission (such as the JDK itself) is allowed to manipulate any thread.

    checkPermission(new RuntimePermission("modifyThreadGroup"))
  }

  override def checkAccess(t: Thread): Unit = {
    super.checkAccess(t)

    // https://docs.oracle.com/javase/8/docs/api/java/lang/SecurityManager.html#checkAccess-java.lang.Thread-
    // Applications that want a stricter policy should override this method.
    // If this method is overridden, the method that overrides it should additionally check to see if the
    // calling thread has the RuntimePermission("modifyThread") permission,
    // and if so, return silently. This is to ensure that code granted that permission
    // (such as the JDK itself) is allowed to manipulate any thread.

    checkPermission(new RuntimePermission("modifyThread"))
  }
}
