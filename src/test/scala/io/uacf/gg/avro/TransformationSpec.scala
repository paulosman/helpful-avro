package io.uacf.gg.avro

import io.uacf.gg.avro.EnhancedJsValue._
import io.uacf.gg.avro.EnhancedJsValue.Implicits._

import org.scalatest.{BeforeAndAfter, Matchers, FunSuite}

class TransformationSpec extends FunSuite with BeforeAndAfter with Matchers {

  test("A non nullable field is returned unchanged") {
    val schema = schemaFromFile("src/test/fixtures/user.avsc")
    val field = schema.getField("first_name")
    val payload = "{\"first_name\":\"Paul\"}"
    payload.asAvro(field).toString should equal(payload)
  }

  test("A missing nullable field is added") {
    val schema = schemaFromFile("src/test/fixtures/user.avsc")
    val location = schema.getField("employer").schema.getField("location")
    val payload = "{\"location\":{\"city\":\"Austin\",\"state\":\"TX\",\"country\":\"USA\"}}"
    payload.asAvro(location).toString should include("\"zip\":{\"null\":null}")
  }

  test("A non-type annotated field is type annotated") {
    val schema = schemaFromFile("src/test/fixtures/user.avsc")
    val location = schema.getField("employer").schema.getField("location")
    val payload = "{\"location\":{\"city\":\"Austin\",\"state\":\"TX\",\"country\":\"USA\",\"zip\":\"78702\"}}"
    payload.asAvro(location).toString should include("\"zip\":{\"string\":\"78702\"}")
  }

  test("An array is handled properly") {
    val schema = schemaFromFile("src/test/fixtures/workout_created.avsc")
    val aggregates = schema.getField("aggregates")
    val payload = "{\"aggregates\":[{\"data_type_id\":\"distance\",\"value\":[{\"operator\":\"sum\",\"field\":\"distance\",\"value\":\"1609\"}]}]}"
    payload.asAvro(aggregates).toString should include("\"aggregates\":{\"array\":[{")
  }
}
