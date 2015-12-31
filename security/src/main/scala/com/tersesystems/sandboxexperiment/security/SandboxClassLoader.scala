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
class SandboxClassLoader(parent: ClassLoader) extends URLClassLoader(BuildInfo.sandbox.toList.toArray, parent)

