package com.tersesystems.sandboxexperiment.security

import java.security.{AccessControlContext, Permission, PrivilegedAction}

object DoPrivilegedAction {
  def apply[T](permissions: Permission*)(block: => T)(implicit context: AccessControlContext): T = {
    // https://docs.oracle.com/javase/7/docs/technotes/guides/security/doprivileged.html
    //
    // http://www.oracle.com/technetwork/java/seccodeguide-139067.html#9
    // Guideline 9-3 / ACCESS-3: Safely invoke java.security.AccessController.doPrivileged
    //
    // The two-argument overloads of doPrivileged allow changing of privileges to that of a previous acquired context.
    // A null context is interpreted as adding no further restrictions. Therefore, before using stored contexts,
    // make sure that they are not null (AccessController.getContext never returns null).

    if (context == null) {
      throw new SecurityException("Missing AccessControlContext")
    }
    val action = new DoPrivilegedAction[T](block)
    java.security.AccessController.doPrivileged(action, context, permissions: _*)
  }

}

class DoPrivilegedAction[T](block: => T) extends PrivilegedAction[T] {
  override def run(): T = block
}

