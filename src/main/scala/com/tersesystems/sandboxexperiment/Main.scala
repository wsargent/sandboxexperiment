package com.tersesystems.sandboxexperiment

import java.security._

import com.tersesystems.sandboxexperiment.security.{Sandbox, SandboxSecurityManager}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

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
    if (sm != null) {
      logger.error(s"Predefined security manager $sm, exiting")
      System.exit(-1)
    }

    val homeDir = System.getProperty("user.dir")
    val scriptName = s"$homeDir/testscript.sh"
    logger.info("Starting security manager in the code")
    System.setSecurityManager(new SandboxSecurityManager(scriptName))

    val useThread = true
    try {
      if (useThread) {
        runInDifferentThread(className)
      } else {
        runInSameThread(className)
      }
    } catch {
      case e: AccessControlException =>
        logger.error("Cannot run untrusted code", e)
      case NonFatal(e) =>
        logger.error("Unexpected error", e)
      case other: Throwable =>
        logger.error("Don't know what happened", other)
    }
  }

  private def runInDifferentThread(className: String): Unit = {
    // Set up an execution context from a thread pool
    implicit val sandboxExecutionContext = {
      import java.util.concurrent.Executors

      import scala.concurrent._
      val numWorkers = sys.runtime.availableProcessors
      val pool = Executors.newFixedThreadPool(numWorkers)
      ExecutionContext.fromExecutorService(pool)
    }

    val future: Future[String] = Sandbox.future[String](className)
    future.onComplete {
      case Success(result) =>
        logger.info(s"result = ${result}")
        System.exit(0)
      case Failure(e) =>
        logger.error(e.getMessage, e)
        System.exit(-1)
    }
  }

  private def runInSameThread(className: String): Unit = {
    val result = Sandbox.blocking[String](className)
    logger.info(s"result = ${result}")
  }
}
