package com.tersesystems.sandboxexperiment

import java.security._

import com.tersesystems.sandboxexperiment.security.{SandboxClassLoader, SandboxSecurityManager}
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
  * The main entry point.  Starts up a security manager, then runs a script
  * in a sandboxed class loader.
  */
object Main extends SandboxCheck {

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

    val useThread = true
    if (useThread) {
      runSandboxInDifferentThread(className)
    } else {
      try {
        runSandboxCodeInSameThread(className)
      } catch {
        case e: AccessControlException =>
          logger.error("Cannot run untrusted code", e)
        case NonFatal(e) =>
          logger.error("Unexpected error", e)
        case other: Throwable =>
          logger.error("Don't know what happened", other)
      }
    }

  }

  /**
    * Uses reflection to instantiate the class which will try to execute shell code.
    */
  private def runSandboxCodeInSameThread(className: String): Unit = {
    // Use a custom class loader to isolate the code...
    val sandboxClassLoader = createSandboxClassLoader
    val scriptRunnerClass = sandboxClassLoader.loadClass(className)
    val method = scriptRunnerClass.getMethod("run")
    val scriptRunnerInstance = scriptRunnerClass.newInstance()
    checkClassLoader(scriptRunnerInstance.getClass)
    try {
      val result = method.invoke(scriptRunnerInstance).asInstanceOf[String]
      logger.info(s"result = $result")
    } catch {
      case e: java.lang.reflect.InvocationTargetException =>
        throw e.getCause
    }
  }

  private def runSandboxInDifferentThread(className: String): Unit = {
    val t = new SandboxThread(className)
    t.start()
    t.join()
  }

}

class SandboxThread(className: String) extends Thread(new SandboxRunnable(className), "sandbox-thread")

class SandboxRunnable(className: String) extends Runnable with SandboxCheck {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def run() = {
    val cl = createSandboxClassLoader
    val sandboxClass = cl.loadClass(className)
    val method = sandboxClass.getMethod("run")
    val scriptRunnerInstance = sandboxClass.newInstance()
    checkClassLoader(scriptRunnerInstance.getClass)
    try {
      val result = method.invoke(scriptRunnerInstance).asInstanceOf[String]
      logger.info(s"result = $result")
    } catch {
      case e: java.lang.reflect.InvocationTargetException =>
        throw e.getCause
    }
  }

}

trait SandboxCheck {

  def checkClassLoader(clazz: Class[_]) = {
    val cl = clazz.getClassLoader
    val clString = cl.getClass.getCanonicalName
    if (!clString.equals("com.tersesystems.sandboxexperiment.security.SandboxClassLoader")) {
      throw new IllegalStateException(s"Sandbox leak: incorrect classloader!")
    }
  }

  def createSandboxClassLoader: SandboxClassLoader = {
    new SandboxClassLoader(this.getClass.getClassLoader)
  }

}
