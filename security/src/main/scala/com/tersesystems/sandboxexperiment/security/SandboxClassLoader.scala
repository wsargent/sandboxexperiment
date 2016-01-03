package com.tersesystems.sandboxexperiment.security

import buildinfo.BuildInfo

import java.net.URLClassLoader
import scala.collection.JavaConverters._

/**
  * The sandbox class loader manages sandboxed classes -- this means anything that is NOT on the classpath.
  *
  * Classloader delegation uses a parent first strategy, so if you have anything on the classpath, it will be
  * loaded before this class as a chance to come into play.
  *
  * Also, it segments the Main class away from the code, and gives the policy a way to distinguish the
  * normal code from the sandbox code.
  *
  * There's some SBT BuildInfo magic in there that points to the file URL that the sandbox
  * is located in, so we don't have to explicitly jar it up.
  */
class SandboxClassLoader(parent: ClassLoader) extends URLClassLoader(BuildInfo.sandbox.toList.toArray, parent) {

  import SandboxClassLoader._

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    // println(s"loadClass: name = ${name}, resolve = $resolve")

    if (forbiddenClasses.contains(name)) {
      throw new IllegalArgumentException("This functionality is disabled")
    }
    super.loadClass(name, resolve)
  }

  override def findClass(name: String): Class[_] = {
    super.findClass(name)
  }
}


object SandboxClassLoader {

  // There are various classes that have weaker security privileges and only check the immediate parent.
  // The simplest way to deal with them is to not let them be instantiated in the first place.

  val miscClasses =
    """java.io.ObjectInputStream
      |java.io.ObjectOutputStream
      |java.io.ObjectStreamField
      |java.io.ObjectStreamClass
      |java.util.logging.Logger
      |java.sql.DriverManager
      |javax.sql.rowset.serial.SerialJavaObject
    """.stripMargin.split("\n").toSet

  // a bit extreme, but see http://www.security-explorations.com/materials/se-2014-02-report.pdf
  val javaClasses =
    """java.lang.Class
      |java.lang.ClassLoader
      |java.lang.Package
      |java.lang.invoke.MethodHandleProxies
      |java.lang.reflect.Proxy
      |java.lang.reflect.Constructor
      |java.lang.reflect.Method
    """.stripMargin.split("\n").toSet

  val forbiddenClasses: Set[String] = javaClasses ++ miscClasses

}
