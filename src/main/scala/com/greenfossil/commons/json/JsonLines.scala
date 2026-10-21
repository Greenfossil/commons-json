package com.greenfossil.commons.json

import scala.util.{Failure, Try}

/** Newline-delimited JSON (JSONL / NDJSON) helpers.
  *
  * Parsing is per line: a malformed line produces its own
  * [[JsonLineError]] without discarding the rest of a model/event stream.
  */
object JsonLines:

  /** Parse one JSON value per non-blank line. Each line has its own result so a
    * malformed event does not discard the rest of a model/event stream.
    */
  def parse(value: String): Seq[Try[JsValue]] =
    value.linesIterator.zipWithIndex.flatMap { case (line, index) =>
      val trimmed = line.trim
      if trimmed.isEmpty then None
      else
        Some(Try(Json.parse(trimmed)).recoverWith { case error =>
          Failure(JsonLineError(index + 1, line, error))
        })
    }.toSeq

  /** Serialize values as newline-delimited JSON, one compact value per line. */
  def stringify(values: Iterable[JsValue]): String =
    values.map(Json.stringify).mkString("\n")

final case class JsonLineError(line: Int, input: String, cause: Throwable)
  extends JsonException(s"Invalid JSON on line $line: ${cause.getMessage}")
