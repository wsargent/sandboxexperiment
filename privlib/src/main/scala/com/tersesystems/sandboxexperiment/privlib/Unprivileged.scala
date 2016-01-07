package com.tersesystems.sandboxexperiment.privlib

import java.security._

/**
  * Used in situations where you want to sandbox some computation.
  */
object Unprivileged {

  /**
    * Runs the given action inside a privileged block with NO permissions.
    */
  def apply[T](action: => T): T = {
    // This SHOULD reduce privileges, but only if a security manager is set.
    if (System.getSecurityManager == null) {
      throw new IllegalStateException("No security manager set!")
    }

    // https://github.com/frohoff/jdk8u-dev-jdk/blob/master/test/java/security/AccessController/LimitedDoPrivileged.java
    // http://www.oracle.com/technetwork/java/seccodeguide-139067.html#9
    // Guideline 9-4
    // We can create a new protection domain with absolutely no permissions
    // and pass that in if we want to be really strict.
    val perms = new Permissions()
    val noContext = new AccessControlContext(Array(new ProtectionDomain(null, perms)))
    AccessController.doPrivileged(DoPrivilegedAction {
      action
    }, noContext, Array.empty[Permission]: _*)
  }

  //def npe = {
    // will cause NPE in ProtectionDomain!
    //    val classLoader: ClassLoader = null
    //    val url: URL = null
    //    val certs: Array[Certificate] = Array[Certificate]()
    //    val principals: Array[Principal] = Array[Principal]()
    //    val domain = new ProtectionDomain(new CodeSource(url, certs), perms, classLoader, principals)
    //    val reducedContext = new AccessControlContext(Array(domain))
  //}
}