package com.tersesystems.sandboxexperiment.sandbox

import java.io.{ByteArrayInputStream, ObjectInputStream, BufferedReader, InputStreamReader}

/**
 * This is a simple program that tries to do something very unsafe.
 *
 * If we have a working sandbox, we can add and remove permissions and make
 * this class throw an exception.
 */
class ScriptRunner {

  def run() = deserializedEvil()

  //def run() = executeScript()

  private def executeScript(): String = {
    // Slightly hacked up because we don't have access to Main here...
    val cwd = System.getProperty("user.dir")
    val script = s"${cwd}/../testscript.sh"

    // Okay, here we go!
    val runtime = Runtime.getRuntime
    val process = runtime.exec(script)
    val input = new BufferedReader(new InputStreamReader(process.getInputStream))
    val b = new StringBuffer()
    for (line <- Iterator.continually(input.readLine()).takeWhile(_ != null)) {
      b.append(line)
    }
    b.toString
  }

  private def deserializedEvil(): String = {
    val bytes = Array[Byte](20)
    val inputStream = new ByteArrayInputStream(bytes)
    val stream = new ObjectInputStream(inputStream)
    stream.close()
    "evilness"
  }
}
