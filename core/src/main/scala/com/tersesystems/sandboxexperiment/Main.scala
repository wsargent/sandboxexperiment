package com.tersesystems.sandboxexperiment

import java.security._

import com.tersesystems.sandboxexperiment.security.{SandboxClassLoader, SandboxSecurityManager, DoPrivilegedAction}
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
 * The main entry point.  Starts up a security manager, then runs a script
 * in a sandboxed class loader.
 */
object Main {

  private val logger = LoggerFactory.getLogger(Main.getClass)

  private val className = "com.tersesystems.sandboxexperiment.sandbox.ScriptRunner"
  //private val className = "com.tersesystems.sandboxexperiment.sandbox.PrivilegedScriptRunner"
  //private val className = "com.tersesystems.sandboxexperiment.sandbox.ThreadSpawner"

  def main(args: Array[String]) {
    val sm = System.getSecurityManager
    if (sm == null) {
      val homeDir = System.getProperty("user.dir")
      val scriptName = s"${homeDir}/../testscript.sh"
      logger.info("Starting security manager in the code")
      System.setSecurityManager(new SandboxSecurityManager(scriptName))
    } else {
      logger.error(s"Predefined security manager ${sm}")
      System.exit(-1)
    }

    try {
      val result = runSandboxCode[String]
      logger.info(s"result = $result")
    } catch {
      case e: AccessControlException =>
        logger.error("Cannot run untrusted code", e)
      case NonFatal(e) =>
        logger.error("Unexpected error", e)
      case other: Throwable =>
        logger.error("Don't know what happened", other)
    }
  }

  private def createSandboxClassLoader: SandboxClassLoader = {
    // http://www.oracle.com/technetwork/java/seccodeguide-139067.html#9
    // Guideline 9-3 / ACCESS-3: Safely invoke java.security.AccessController.doPrivileged

    // You'd think we could call doPrivileged inside some nice lambda, but NO.  It's a security hole,
    // because the security check is stack-based.
    //
    // https://docs.oracle.com/javase/8/docs/technotes/guides/security/doprivileged.html
    // "Also note that the call to doPrivileged should be made in the code that wants to enable its privileges."
    // "Do not be tempted to write a utility class that itself calls doPrivileged as that could lead to security holes."

    AccessController.doPrivileged(
      DoPrivilegedAction(new SandboxClassLoader(this.getClass.getClassLoader)),
      AccessController.getContext,
      new RuntimePermission("createClassLoader")
    )
  }

  /**
   * Uses reflection to instantiate the class which will try to execute shell code.
   */
  private def runSandboxCode[T]: T = {
    // Use a custom class loader to isolate the code...
    val sandboxClassLoader = createSandboxClassLoader
    val scriptRunnerClass = sandboxClassLoader.loadClass(className)
    val method = scriptRunnerClass.getMethod("run")
    val scriptRunnerInstance = scriptRunnerClass.newInstance()
    try {
      method.invoke(scriptRunnerInstance).asInstanceOf[T]
    } catch {
      case e: java.lang.reflect.InvocationTargetException =>
        throw e.getCause
    }
  }

}

