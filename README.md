# helpful-avro

> "Be conservative in what you do, be liberal in what you accept from others" - Jon Postel

Helpful Avro is a library designed to make working with Avro easier. The primary goal of helpful avro is to allow developers to send messages in raw JSON and attempt to validate them against Avro schemas.

## Usage

To use Helpful Avro, just import the implicit conversion method and call `asAvro` on your string, passing it the Avro schema as a string:

```scala
import java.io.File
import spray.json._
import org.apache.avro.Schema
import scala.io.Source
import io.uacf.gg.avro.AvroJsValue.Implicits._

val schema = Schema.Parser().parse(new File("user.avsc"))
val msg = Source.fromFile("user.json").getLines.mkString("\n")
val payload = msg.parseJson

val avro = msg.asAvro(schema) // returns a JsValue representing avro compatible JSON
println(avro.prettyPrint)
```

## Examples

Take the following Avro schema for example:

```json
{
  "name": "Person",
  "namespace": "org.example",
  "type": "record",
  "fields": [
    {"name": "first_name", "type": "string"},
    {"name": "last_name", "type": "string"},
    {"name": "age", "type": ["null", "int"]},
    {"name": "username", "type": ["null", "string"]}
  ]
}
```

Helpful Avro will take the following message payload:

```json
{
  "first_name": "Paul",
  "last_name": "Osman",
  "username": "paulosman"
}
```

And convert it into the following avro compatible message before validating:

```json
{
  "first_name": "Paul",
  "last_name": "Osman",
  "username": {"string": "paulosman"},
  "age": {"null": null}
}
```

### Command Line Example

To demonstrate the above, you can run `helpful-avro` from the command-line:

```bash
$ sbt "run -s ../examples/person.avsc -p ../examples/person.json"
```

### Embedded Types

Helpful Avro will work with schemas that include embedded types:

```json
{
  "name": "Person",
  "namespace": "org.example",
  "type": "record",
  "fields": [
    {"name": "first_name", "type": "string"},
    {"name": "last_name", "type": "string"},
    {"name": "age", "type": ["null", "int"]},
    {"name": "username", "type": ["null", "string"]},
    {"name": "employer", "type": {
      "type": "record",
      "name": "EmployerRecord",
      "fields": [
        {"name": "company", "type": "string"},
        {"name": "position", "type": ["null", "string"]},
        {"name": "start_date", "type": ["null", "string"]}
      ]
    }}
  ]
}
```

For the above schema, the following JSON payload will validate:

```json
{
  "first_name": "Paul",
  "last_name": "Osman",
  "employer": {
    "company": "Under Armour",
    "position": "Staff Software Engineer"
  }
}
```

By being transformed into:

```json
{
  "first_name": "Paul",
  "last_name": "Osman",
  "age": {"null": null},
  "username": {"null": null},
  "employer": {
    "company": "Under Armour",
    "position": {"string": "Staff Software Engineer"},
    "start_date": {"null": null}
  }
}
```

## Motivation

Avro is a wonderful system for serializing data and ensuring data compatibility. Because Avro deals with types, it can seem awkward to users coming from dynamically typed languages, where JSON is commonly used to represent data. We want the safety provided by static type systems, but we also want developers to be able to write idiomatic JSON payloads.

Helpful Avro is an attempt to implement a type inference layer on top of JSON, in an attempt to make it fit a bit more naturally with Avro. It will bend over backwards trying to get a JSON payload to validate against an Avro schema by trying to infer type information about fields in a payload when none is provided.

For example, in Avro, nullable fields are specified by setting the `type` to a union of `"null"` and the fields type (i.e. `"string"`). In JSON, you'd simply omit a nullable field. Similarly, in Avro you have to give Avro a type hint, so the value of such a field would be `{"string": "value"}` indicating that `"value"` is the `"string"` value of the field, or `{"null": null}` indicating that `null` is the `"null"` value of the field. In JSON, this type information would just be omitted. Helpful Avro will attempt to be the automagic glue that bridges idiomatic JSON and Avro.

## Contributing

Contributions are very welcome! Please simply follow these steps:

* If you have access to the repo, create a branch, otherwise fork it first.
* Commit your changes and push them to your feature branch.
* Create a new Pull Request / Merge Request.

If you'd like to discuss a feature, please open an issue on Github.
