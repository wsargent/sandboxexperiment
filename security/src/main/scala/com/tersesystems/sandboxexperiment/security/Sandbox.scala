package com.tersesystems.sandboxexperiment.security

import scala.concurrent.{ExecutionContext, Future}

/**
  * A sandbox singleton object to manage operations.
  */
object Sandbox {

  private val sandboxClassLoader: SandboxClassLoader = new SandboxClassLoader(this.getClass.getClassLoader)

  /**
    * Runs the sandbox in a Future using the given execution context (basically
    * a thread out of the thread pool.
    */
  def future[T](className: String)(implicit ec: ExecutionContext): Future[T] = {
    val sandboxClass = createSandboxClass(className)
    Future {
      runSandboxClass[T](sandboxClass)
    }(ec)
  }

  /**
    * Run the sandbox class in the current thread, potentially blocking this thread.
    */
  def blocking[T](className: String): T = {
    val sandboxClass = Sandbox.createSandboxClass(className)
    runSandboxClass[T](sandboxClass)
  }

  /**
    * Creates the sandbox class, given the class name.
    */
  private def createSandboxClass(className: String): Class[_] = {
    val sandboxClass = sandboxClassLoader.loadClass(className)
    checkClassLoader(sandboxClass)
    sandboxClass
  }

  /**
    * Uses reflection to instantiate the class which will try to execute shell code.
    */
  private def runSandboxClass[T](sandboxClass: Class[_]): T = {
    try {
      val method = sandboxClass.getMethod("run")
      val instance = sandboxClass.newInstance()
      method.invoke(instance).asInstanceOf[T]
    } catch {
      case e: java.lang.reflect.InvocationTargetException =>
        throw e.getCause
    }
  }

  private def checkClassLoader(clazz: Class[_]): Unit = {
    val cl = clazz.getClassLoader
    val clString = cl.getClass.getCanonicalName
    if (!clString.equals("com.tersesystems.sandboxexperiment.security.SandboxClassLoader")) {
      throw new IllegalStateException(s"Sandbox leak: incorrect classloader!")
    }
  }

}
