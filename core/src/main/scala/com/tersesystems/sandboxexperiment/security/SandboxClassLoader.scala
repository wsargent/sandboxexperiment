package com.tersesystems.sandboxexperiment.security

import buildinfo.BuildInfo

import java.net.URLClassLoader

/**
 * The sandbox class loader manages sandboxed classes.  For example, you can use it to disable Java serialization.
 *
 * Also, it segments the Main class away from the code, and gives the policy a way to distinguish the
 * normal code from the sandbox code.
 *
 * There's some SBT BuildInfo magic in there that points to the file URL that the sandbox
 * is located in, so we don't have to explicitly jar it up.
 */
class SandboxClassLoader(parent:ClassLoader) extends URLClassLoader(BuildInfo.sandbox.toArray, parent) {
  import SandboxClassLoader._

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    if (forbiddenClasses.contains(name)) {
      throw new IllegalArgumentException("This functionality is disabled")
    }
    super.loadClass(name, resolve)
  }

}

object SandboxClassLoader {


  val miscClasses =
    """
      |java.io.ObjectInputStream
      |java.io.ObjectOutputStream
      |java.io.ObjectStreamField
      |java.io.ObjectStreamClass
      |java.util.logging.Logger
      |java.sql.DriverManager
      |javax.sql.rowset.serial.SerialJavaObject
    """.stripMargin.split("\n")

  // a bit extreme, but see http://www.security-explorations.com/materials/se-2014-02-report.pdf
  val javaClasses =
    """java.lang.Class
      |java.lang.ClassLoader
      |java.lang.Package
      |java.lang.invoke.MethodHandleProxies
      |java.lang.reflect.Proxy
      |java.lang.reflect.Constructor
      |java.lang.reflect.Method
    """.stripMargin.split("\n")

  val forbiddenClasses: Seq[String] = javaClasses ++ miscClasses

}
