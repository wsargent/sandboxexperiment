package com.tersesystems.sandboxexperiment.security

import java.security.{AccessControlContext, Permission, PrivilegedAction}


// https://docs.oracle.com/javase/7/docs/technotes/guides/security/doprivileged.html
object DoPrivilegedAction {

  def apply[T](permissions: Permission*)(block: => T)(implicit context: AccessControlContext): T = {
    val action = new DoPrivilegedAction[T](block)
    java.security.AccessController.doPrivileged(action, context, permissions: _*)
  }

}

class DoPrivilegedAction[T](block: => T) extends PrivilegedAction[T] {
  override def run(): T = block
}

