package com.tersesystems.sandboxexperiment.sandbox

import java.io.{FilePermission, BufferedReader, InputStreamReader}
import java.net.URL
import java.security.cert.Certificate
import java.security._
import java.util.PropertyPermission

/**
  * This is a simple program that tries to do something very unsafe.
  *
  * If we have a working sandbox, we can add and remove permissions and make
  * this class throw an exception.
  */
class ScriptRunner extends Executor {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def run() = executeNormal()
  //def run() = executeScriptWithReducedPrivileges()
  //def run() = executeScriptWithEnhancedPrivileges()

  private def executeNormal(): String = {
    // Slightly hacked up because we don't have access to Main here...
    val cwd = System.getProperty("user.dir")
    val script = s"${cwd}/../testscript.sh"

    execute(script)
  }

}
