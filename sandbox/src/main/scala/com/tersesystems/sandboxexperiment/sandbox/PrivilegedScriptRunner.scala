package com.tersesystems.sandboxexperiment.sandbox

import java.io.{File, FilePermission}
import java.net.URL
import java.security._
import java.security.cert.Certificate

import com.tersesystems.sandboxexperiment.privlib.{DoPrivilegedAction, Executor}

class PrivilegedScriptRunner {

  def run() = {
    // something we shouldn't be able to access.
    val env = "/usr/bin/env"

    // grant expanded permissions
    val perms = new Permissions()
    perms.add(new FilePermission(env, "execute"))

    // We're in the sandbox, so calling doPrivileged does nothing here, because it
    // looks at its immediate parent... which is PrivilegedScriptRunner.run(), still
    // in the sandbox.
    val expandedContext = new AccessControlContext(Array(new ProtectionDomain(null, perms)))
    val action = DoPrivilegedAction {
      val file = new File(env)
      Executor.execute(file)
    }
    java.security.AccessController.doPrivileged(action, expandedContext)
  }

}
