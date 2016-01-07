package com.tersesystems.sandboxexperiment.privlib

import java.security._

object DoPrivilegedAction {
  def apply[T](actionBody: => T): DoPrivilegedAction[T] = {
    if (System.getSecurityManager == null) {
      throw new IllegalStateException("No security manager set!")
    }
    new DoPrivilegedAction[T](actionBody)
  }
}

class DoPrivilegedAction[T](block: => T) extends PrivilegedAction[T] {
  override def run(): T = block
}

