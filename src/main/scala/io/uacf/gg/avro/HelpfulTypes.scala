package io.uacf.gg.avro

import scala.collection.JavaConversions._
import scala.io.Source
import scala.util.{Try, Success, Failure}
import scala.language.implicitConversions
import spray.json._
import org.apache.avro.Schema
import org.apache.avro.Schema.Field

/**
  * Wraps org.avro.Schema.Field instances and provides extra operations.
  *
  * @param field An org.avro.Schema.Field instance
  */
class FieldOps(val field: Field) {

  /**
    * Tests whether this field is a union type that includes the type "null".
    * For example, if the type is ["int", "null"] this method will return true.
    *
    * @return True if the field is nullable, false otherwise.
    */
  def isNullable: Boolean =
    if (field.schema.getType != Schema.Type.UNION) false
    else field.schema.getTypes.exists(_.getType == Schema.Type.NULL)


  /**
    * Tests whether this field is a record type.
    *
    * @return True if the field is a record, false otherwise.
    */
  def isRecord: Boolean = field.schema.getType == Schema.Type.RECORD

  /**
    * Returns the non-null type for union fields. For example, if the type of
    * a field is ["string", "null"], this method will return string.
    *
    * @return An org.avro.Schema.Type instance
    */
  def nullableType: Option[Schema.Type] =
    Try(field.schema.getTypes) match {
      case Success(types) =>
        Some(types.filterNot(_.getType == Schema.Type.NULL).head.getType)
      case Failure(_) =>
        None
    }
}

object FieldOps {

  /**
    * Implicitly convert a field to a FieldOps instance.
    *
    * @param f an org.apache.avro.Schema.Field instance
    *
    * @return a FieldOps instance
    */
  implicit def fieldToFieldOps(f: Field): FieldOps =
    new FieldOps(f)
}

/**
  * EnhancedJsValue contains methods that rewrite JSON to validate against Avro
  * schemas.
  */
class EnhancedJsValue(val value: JsValue) {

  /**
    * Json format for Maps, required below
    */
  private[this] implicit object MapJsonFormat extends JsonFormat[Map[String, Any]] {
    def write(m: Map[String, Any]): JsObject = {
      JsObject(m.mapValues {
        case null => JsNull
        case v: JsNumber => v
        case v: JsString => v
        case v: Int => JsNumber(v)
        case v: Long => JsNumber(v)
        case v: Double => JsNumber(v)
        case v: Map[_, _] => write(v.asInstanceOf[Map[String, JsValue]])
        case v: JsArray => v
        case v: JsObject => v
        case v: Any => JsString(v.toString.replace("\"", "")) // toString is escaping our quotes
      })
    }

    def read(value: JsValue) = ???
  }

  /**
    * Return a modified JsValue that validates against the given Schema.
    *
    * @param schema Schema the Avro schema
    * @return JsValue a JsValue containing JSON that validates against the schema
    */
  def asAvro(schema: Schema): JsValue = {
    val fields = schema.getFields.toList.map(Transformation(_, value))
    fields.toMap.toJson
  }

  /**
    * Return a modified JsValue that validates against the given Field.
    *
    * @param field Field the Avro field
    * @return JsValue a JsValue containing JSON that validates against the field
    */
  def asAvro(field: Field): JsValue = {
    List(Transformation(field, value)).toMap.toJson
  }


  /**
    * Return a modified JsValue that validates against the given Schema.
    *
    * @param schema String the Avro schema as a string
    * @return JsValue a JsValue containing JSON that validates against the schema
    */
  def asAvro(schema: String): JsValue =
    asAvro(new Schema.Parser().parse(schema))
}

/**
  * Convenience methods for working with JSON and Avro files and implicit conversion methods.
  */
object EnhancedJsValue {

  /**
    * Utility method. Read file and return contents as string.
    *
    * @param path String the path to the file
    *
    * @return string the file contents
    */
  private[this] def fromFile(path: String) =
    Source.fromFile(path).getLines.mkString("\n")

  /**
    * Utility method. Parse a schema from a file.
    *
    * @param path String the path to the file
    *
    * @return Schema a Schema instance
    */
  def schemaFromFile(path: String): Schema =
    new Schema.Parser().parse(fromFile(path))

  /**
    * Utility method. Parse JSON from a file.
    *
    * @param path String the path to the file
    *
    * @return JsValue parsed JSON ast
    */
  def jsonFromFile(path: String): JsValue =
    fromFile(path).parseJson

  /**
    * Implicit conversion methods
    */
  object Implicits {

    /**
      * Convert a regular JsValue to an EnhancedJsValue
      */
    implicit def jsValueToEnhancedJsValue(value: JsValue): EnhancedJsValue =
      new EnhancedJsValue(value)

    /**
      * Convert a string into an EnhancedJsValue
      */
    implicit def stringToEnhancedJsValue(value: String): EnhancedJsValue =
      new EnhancedJsValue(value.parseJson)
  }
}
