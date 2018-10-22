package io.uacf.gg.avro

import io.uacf.gg.avro.EnhancedJsValue._
import io.uacf.gg.avro.EnhancedJsValue.Implicits._
import scopt.OptionParser

case class CommandLineConfig(schemaFile: String = "", payloadFile: String = "")

object Main extends App {
  val parser = new OptionParser[CommandLineConfig]("helpful-avro") {
    head("helpful-avro", "1.0")
    opt[String]('s', "schemaFile") required() valueName "schemaFile" action { (x, c) =>
      c.copy(schemaFile = x)
    } text "file containing avro schema"
    opt[String]('p', "payloadFile") required() valueName "payloadFile" action { (x, c) =>
      c.copy(payloadFile = x)
    } text "file containing payload"
  }

  parser.parse(args, CommandLineConfig()) match {
    case Some(config) =>
      val schema = schemaFromFile(config.schemaFile)
      val payload = jsonFromFile(config.payloadFile)
      println(payload.prettyPrint)
      println(payload.asAvro(schema).prettyPrint)
    case None =>
  }

}
