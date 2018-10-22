package io.uacf.gg.avro

import org.scalatest.FunSuite
import scala.io.Source
import spray.json._

class HelpfulAvroSpec extends FunSuite {

  def fromFile(fileName: String) =
    Source.fromFile(fileName).getLines.mkString("\n")

  test("A JSON avro schema can be parsed into an AST representation") {
    val schema = fromFile("src/test/fixtures/workout_created.avsc")
    assert(schema.parseJson.isInstanceOf[JsValue])
  }


}
