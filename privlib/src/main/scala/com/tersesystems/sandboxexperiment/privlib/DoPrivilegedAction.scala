package com.tersesystems.sandboxexperiment.privlib

import java.security.{PrivilegedExceptionAction, PrivilegedAction}

object DoPrivilegedAction {
  def apply[T](actionBody: => T): DoPrivilegedAction[T] = {
    new DoPrivilegedAction[T](actionBody)
  }
}

class DoPrivilegedAction[T](block: => T) extends PrivilegedAction[T] {
  override def run(): T = block
}



