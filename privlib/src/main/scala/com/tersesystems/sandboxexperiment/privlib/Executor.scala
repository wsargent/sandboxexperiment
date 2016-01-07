package com.tersesystems.sandboxexperiment.privlib

import java.io.{FilePermission, BufferedReader, File, InputStreamReader}
import java.security._

object Executor {

  import AccessController._

  def execute(file: File) = {
    val canonicalFile = file.getCanonicalFile

    // absolute path is absolute.
    val absolutePath = canonicalFile.toPath.toAbsolutePath.toString

    // If this method is called from the sandbox, then we need to use doPrivileged to
    // make clear we're the stopping point for security checking.
    // This does give away the information that the file EXISTS to a malicious attacker.
    doPrivileged(DoPrivilegedAction {
      if (!canonicalFile.exists()) {
        throw new IllegalStateException(s"$canonicalFile does not exist!")
      }
    }, null, new FilePermission(absolutePath, "read"))

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

}
