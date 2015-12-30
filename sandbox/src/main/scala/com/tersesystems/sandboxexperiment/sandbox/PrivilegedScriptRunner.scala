package com.tersesystems.sandboxexperiment.sandbox

import java.io.FilePermission
import java.net.URL
import java.security._
import java.security.cert.Certificate

class PrivilegedScriptRunner extends Executor{

  def run() = {
    // grant expanded permissions
    val perms = new Permissions()
    perms.add(new FilePermission("<<ALL FILES>>", "execute"))

    val classLoader: ClassLoader = null
    val url: URL = null
    val certs: Array[Certificate] = Array[Certificate]()
    val principals: Array[Principal] = Array[Principal]()
    val domain = new ProtectionDomain(new CodeSource(url, certs), perms, classLoader, principals)
    val reducedContext = new AccessControlContext(Array(domain))

    val action = new PrivilegedExceptionAction[String]() {
      override def run(): String = {
        val script = "/usr/bin/env"
        execute(script)
      }
    }
    java.security.AccessController.doPrivileged(action, reducedContext)
  }

}
