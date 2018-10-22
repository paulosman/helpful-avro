package io.uacf.gg.avro

import org.apache.avro.Schema.Field
import spray.json.JsValue

import scala.collection.JavaConversions._
import scala.util.Try

/**
  * Encapsulate logic of transformations. In order to get a payload to validate against an avro schema, this
  * object provides transformations of the following type:
  *
  * - Missing nullable field is turned into a field with a value of null
  * - Missing type information for nullable field is turned into a field with a type annotation
  */
object Transformation {
  import FieldOps._

  /**
    * Transform a nullable field, represent it as the field name mapped to a Map of the type name to the value.
    *
    * @param field The Schema we want to validate our payload against
    * @param value The JsValue representing the payload
    * @return A list of String to Any that can be turned into a Map
    */
  def transformNullable(field: Field, value: JsValue): (String, Any) = {
    val typeName = field.nullableType.getOrElse("null").toString.toLowerCase
    val newValue = Try(value.asJsObject.fields(field.name))

    if (newValue.isSuccess && newValue.get.toString != "null") {
      field.name -> Map[String, Any](typeName -> newValue.get)
    } else {
      field.name -> Map[String, Any]("null" -> null)
    }
  }

  /**
    * Transform a non-nullable field, this actually isn't a transformation, just represent it as the required type
    *
    * @param field The Schema we want to validate our payload against
    * @param value The JsValue representing the payload
    * @return A list of String to Any that can be turned into a Map
    */
  def transformNonNullable(field: Field, value: JsValue): (String, Any) =
    field.name -> value.asJsObject.fields(field.name)

  /**
    * Recursively perform necessary transformations on a record
    *
    * @param field The Schema we want to validate our payload against
    * @param value The JsValue representing the payload
    * @return A list of String to Any that can be turned into a Map
    */
  def transformRecord(field: Field, value: JsValue): (String, Any) = {
    val newValue = value.asJsObject.fields(field.name)
    val fields = field.schema.getFields.toList.map(Transformation(_, newValue))
    field.name -> fields.toMap
  }

  /**
    * Transform a JsValue into one that conforms to the provided Schema
    *
    * @param field The Schema we want to validate our payload against
    * @param value The payload value
    * @return A (String, Any) representing the field name and value
    */
  def apply(field: Field, value: JsValue): (String, Any) =
    field match {
      case f if f.isNullable => transformNullable(f, value)
      case f if f.isRecord => transformRecord(f, value)
      case _ => transformNonNullable(field, value)
    }
}