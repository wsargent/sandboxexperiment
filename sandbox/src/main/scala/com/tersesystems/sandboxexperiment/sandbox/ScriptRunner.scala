package com.tersesystems.sandboxexperiment.sandbox

import java.io.{File, FilePermission, BufferedReader, InputStreamReader}
import java.net.URL
import java.security.cert.Certificate
import java.security._
import java.util.PropertyPermission

import com.tersesystems.sandboxexperiment.privlib.Executor

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
