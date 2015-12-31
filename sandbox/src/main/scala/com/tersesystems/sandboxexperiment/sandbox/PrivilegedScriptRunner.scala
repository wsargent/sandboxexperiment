package com.tersesystems.sandboxexperiment.sandbox

import java.io.FilePermission
import java.net.URL
import java.security._
import java.security.cert.Certificate

class PrivilegedScriptRunner extends Executor{

  def run() = {
    // grant expanded permissions
    val perms = new Permissions()
    perms.add(new FilePermission("/usr/bin/env", "execute"))

    val expandedContext = new AccessControlContext(Array(new ProtectionDomain(null, perms)))
    val action = new PrivilegedExceptionAction[String]() {
      override def run(): String = {
        val script = "/usr/bin/env"
        execute(script)
      }
    }

    // We're in the sandbox, so we can't escape from that.
    //
    // This is confusing, because
    // https://docs.oracle.com/javase/8/docs/technotes/guides/security/doprivileged.html#more_privilege
    // says "Sometimes when coding the current method, you want to temporarily extend the permission of the
    // calling method to perform an action" and goes on from there.
    //
    java.security.AccessController.doPrivileged(action, expandedContext)
  }

}
