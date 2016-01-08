package com.tersesystems.sandboxexperiment.sandbox

import java.io.File
import java.security.AccessController._
import java.security._
import java.util.PropertyPermission

import com.tersesystems.sandboxexperiment.privlib.{DoPrivilegedAction, Executor}

/**
  * This is a simple program that tries to do something very unsafe.
  *
  * If we have a working sandbox, we can add and remove permissions and make
  * this class throw an exception.
  */
class ScriptRunner {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def run() = {
    // Slightly hacked up because we don't have access to Main here...
    val cwd = System.getProperty("user.dir")
    val script = s"$cwd/testscript.sh"
    val file = new File(script)

    // You can unprivilege yourself inside a sandbox, but you can also immediately privilege yourself
    // back again, because you have the same CodeSource and ProtectionDomain.  So the one thing you
    // should NEVER do is call a closure / function inside a doPrivileged block.  Which means no
    // utility methods.
    //
    // And yeah, if you pass in "null" then it really means "the current one", so "noPermissionsContext"
    // is not strictly necessary.
    //
    // Using AccessController.getContext() as the second parameter does something else, depending on
    // where you call it from.

    // No permissions access context
    val perms = new Permissions()
    val noPermissionsContext = new AccessControlContext(Array(new ProtectionDomain(null, perms)))

    doPrivileged(DoPrivilegedAction {
      doPrivileged(DoPrivilegedAction {
        System.getProperty("user.dir")
      }, null, new PropertyPermission("*", "read"))
    }, noPermissionsContext, Array.empty[Permission]: _*)

    try {
      Executor.execute(file)
    } catch {
      case ace: AccessControlException =>
        Console.println("Cannot execute file!")
        ace.printStackTrace()
        "[ERROR]"
    }
  }

}
