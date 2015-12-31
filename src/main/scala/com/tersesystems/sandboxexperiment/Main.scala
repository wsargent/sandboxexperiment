package com.tersesystems.sandboxexperiment

import java.security._

import com.tersesystems.sandboxexperiment.security.{SandboxClassLoader, SandboxSecurityManager}
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
  * The main entry point.  Starts up a security manager, then runs a script
  * in a sandboxed class loader.
  */
object Main {

  private val logger = LoggerFactory.getLogger(Main.getClass)

  private val defaultClassName = "com.tersesystems.sandboxexperiment.sandbox.ScriptRunner"

  //private val className = "com.tersesystems.sandboxexperiment.sandbox.PrivilegedScriptRunner"
  //private val className = "com.tersesystems.sandboxexperiment.sandbox.ThreadSpawner"

  def main(args: Array[String]) {

    val className = if (args.length == 0) {
      defaultClassName
    } else {
      args(0)
    }

    val sm = System.getSecurityManager
    if (sm == null) {
      val homeDir = System.getProperty("user.dir")
      val scriptName = s"$homeDir/testscript.sh"
      logger.info("Starting security manager in the code")
      System.setSecurityManager(new SandboxSecurityManager(scriptName))
    } else {
      logger.error(s"Predefined security manager $sm, exiting")
      System.exit(-1)
    }

    try {
      val result = runSandboxCode[String](className)
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
    new SandboxClassLoader(this.getClass.getClassLoader)
  }

  /**
    * Uses reflection to instantiate the class which will try to execute shell code.
    */
  private def runSandboxCode[T](className: String): T = {
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

