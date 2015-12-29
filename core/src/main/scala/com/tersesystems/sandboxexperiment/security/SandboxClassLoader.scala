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

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    if ("java.io.ObjectInputStream".equals(name)) {
      throw new IllegalArgumentException("This functionality is disabled")
    }
    super.loadClass(name, resolve)
  }

}
