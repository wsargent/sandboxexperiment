package com.tersesystems.sandboxexperiment.sandbox

import java.io.{ObjectInputStream, ByteArrayInputStream}

/**
  * Try to deserialize a class.
  */
class ObjectDeserializer {

  def run() = deserializedEvil()

  private def deserializedEvil(): String = {
    val bytes = Array[Byte](20)
    val inputStream = new ByteArrayInputStream(bytes)
    val stream = new ObjectInputStream(inputStream)
    stream.close()
    "evilness"
  }
}
