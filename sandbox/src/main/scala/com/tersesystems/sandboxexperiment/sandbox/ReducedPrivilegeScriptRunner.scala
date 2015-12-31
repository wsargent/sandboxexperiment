package com.tersesystems.sandboxexperiment.sandbox

import java.security._

class ReducedPrivilegeScriptRunner extends Executor {

  def run() = {

    val cwd = System.getProperty("user.dir")
    val scriptName = s"${cwd}/../testscript.sh"
    val perm = new java.io.FilePermission(scriptName, "execute")

    AccessController.checkPermission(perm)
    val perms = perm.newPermissionCollection()
    //perms.add(perm)

    // will cause NPE in ProtectionDomain!
    //    val classLoader: ClassLoader = null
    //    val url: URL = null
    //    val certs: Array[Certificate] = Array[Certificate]()
    //    val principals: Array[Principal] = Array[Principal]()
    //    val domain = new ProtectionDomain(new CodeSource(url, certs), perms, classLoader, principals)
    //    val reducedContext = new AccessControlContext(Array(domain))

    // http://www.oracle.com/technetwork/java/seccodeguide-139067.html#9
    // Guideline 9-4
    // This SHOULD reduce privileges.
    val reducedContext = new AccessControlContext(Array(new ProtectionDomain(null, perms)))
    val action = new PrivilegedExceptionAction[String]() {
      override def run(): String = {
        execute(scriptName)
      }
    }

    java.security.AccessController.doPrivileged(action, reducedContext)
  }

}
