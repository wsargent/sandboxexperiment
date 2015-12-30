package com.tersesystems.sandboxexperiment.sandbox

import java.io.{InputStreamReader, BufferedReader}

trait Executor {

  def execute(script: String) = {
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

}
