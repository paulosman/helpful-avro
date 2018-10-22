package io.uacf.gg.avro

import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.apache.avro.Schema

import io.uacf.gg.avro.EnhancedJsValue._
import io.uacf.gg.avro.FieldOps._

class TypesSpec extends FunSuite with BeforeAndAfter with Matchers {

  var schema: Schema = _

  before {
    schema = schemaFromFile("src/test/fixtures/user.avsc")
  }

  test("A union type with null is nullable") {
    val ageField = schema.getField("age")
    ageField.isNullable should be (true)
  }

  test("A non-union type is not nullable") {
    val firstNameField = schema.getField("first_name")
    firstNameField.isNullable should be (false)
  }

  test("A union type without null is not nullable") {
    val favColorField = schema.getField("favorite_color")
    favColorField.isNullable should be (false)
  }

  test("We can extract the non-null type of a nullable field") {
    val ageField = schema.getField("age")
    ageField.nullableType shouldEqual Some(Schema.Type.INT)
  }

  test("We cannot extract the non-null type of a non-nullable field") {
    val firstNameField = schema.getField("first_name")
    firstNameField.nullableType.isDefined should be (false)
  }
}
