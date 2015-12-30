package com.tersesystems.sandboxexperiment.sandbox

import java.net.URL
import java.security._
import java.security.cert.Certificate

/**
  * Created by wsargent on 12/30/15.
  */
class ReducedPrivilegeScriptRunner extends Executor {

  def run() = {
    // grant NO permissions, this should do nothing at all...
    val perms = new Permissions()

    val classLoader: ClassLoader = null
    val url: URL = null
    val certs: Array[Certificate] = Array[Certificate]()
    val principals: Array[Principal] = Array[Principal]()
    val domain = new ProtectionDomain(new CodeSource(url, certs), perms, classLoader, principals)
    val reducedContext = new AccessControlContext(Array(domain))

    val cwd = System.getProperty("user.dir")
    val script = s"${cwd}/../testscript.sh"

    val action = new PrivilegedExceptionAction[String]() {
      override def run(): String = {
        execute(script)
      }
    }
    java.security.AccessController.doPrivileged(action, reducedContext)
  }

}
