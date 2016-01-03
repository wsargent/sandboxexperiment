package com.tersesystems.sandboxexperiment.privlib

import java.io.{FilePermission, BufferedReader, File, InputStreamReader}
import java.security.{ProtectionDomain, AccessControlContext, Permissions, AccessController}

object Executor {

  def execute(file: File) = {
    val canonicalFile = file.getCanonicalFile

    // absolute path is absolute.
    val absolutePath = canonicalFile.toPath.toAbsolutePath.toString

    // If this method is called from the sandbox, then we need to use doPrivileged to
    // make clear we're the stopping point for security checking.
    // This does give away the information that the file EXISTS to a malicious attacker.
    AccessController.doPrivileged(DoPrivilegedAction {
      //System.out.println(s"Executor.getProtectionDomain = ${this.getClass.getProtectionDomain}")

      if (! canonicalFile.exists()) {
        throw new IllegalStateException(s"$canonicalFile does not exist!")
      }
    })

    // This DOESN'T run with enhanced privileges, so the AccessController looks through
    // the entire stack, not stopping with this execute method.  If the sandbox is in the stack
    // and doesn't have access to "execute", then the AccessController will throw exception.
    val runtime = Runtime.getRuntime
    val process = runtime.exec(absolutePath)
    val input = new BufferedReader(new InputStreamReader(process.getInputStream))
    val b = new StringBuffer()
    for (line <- Iterator.continually(input.readLine()).takeWhile(_ != null)) {
      b.append(line)
    }
    b.toString
  }

  def readPropertyWithNoPrivileges(): String = {

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

    // We can create a new protection domain with absolutely no permissions
    // and pass that in if we want to be really strict.
    val perms = new Permissions()
    val weakAccessControlContext = new AccessControlContext(Array(new ProtectionDomain(null, perms)))

    java.security.AccessController.doPrivileged(DoPrivilegedAction {
      System.getProperty("user.dir")
    }, weakAccessControlContext)
  }

}
