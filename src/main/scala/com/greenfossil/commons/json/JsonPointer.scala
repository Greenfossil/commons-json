package com.greenfossil.commons.json

import scala.util.{Failure, Success, Try}

/** RFC 6901 JSON Pointer support.
  *
  * The safe surface is `Try`, matching the library's exception-based core:
  * [[Json.parse]] / `JsValue.as[T]` still throw for compatibility, while
  * [[Json.parseTry]], [[JsonPointer.parse]] and [[select]] give callers an explicit result
  * for tool/LLM payload handling.
  */
final case class JsonPointer(tokens: Seq[String]):
  def render: String =
    if tokens.isEmpty then ""
    else tokens.map(JsonPointer.escape).mkString("/", "/", "")

  /** Return the addressed value, or a failure carrying [[JsonAccessError]].
    * A real JSON `null` is a successful result. */
  def select(value: JsValue): Try[JsValue] =
    if tokens.isEmpty then Success(value)
    else
      val path = render
      value.at(path) match
        case _: JsUndefined =>
          Failure(JsonAccessError(path, "existing JSON value", value.at(path).stringify))
        case found => Success(found)

  def selectOpt(value: JsValue): Option[JsValue] = select(value).toOption

  override def toString: String = render

object JsonPointer:
  val root: JsonPointer = JsonPointer(Nil)

  def parse(input: String): Try[JsonPointer] = Try {
    if input.isEmpty then root
    else if !input.startsWith("/") then
      throw JsonPointerError(input, "must be empty or start with '/'")
    else
      val raw = input.drop(1).split("/", -1).toSeq
      JsonPointer(raw.map(unescape))
  }

  def parseOpt(input: String): Option[JsonPointer] = parse(input).toOption

  def escape(token: String): String = token.replace("~", "~0").replace("/", "~1")

  def unescape(token: String): String =
    val out = new StringBuilder
    var i = 0
    while i < token.length do
      token.charAt(i) match
        case '~' =>
          if i + 1 >= token.length then throw JsonPointerError(token, "dangling '~' escape")
          token.charAt(i + 1) match
            case '0' => out.append('~'); i += 2
            case '1' => out.append('/'); i += 2
            case other => throw JsonPointerError(token, s"invalid '~$other' escape")
        case c => out.append(c); i += 1
    out.toString

final case class JsonPointerError(input: String, reason: String)
  extends JsonException(s"Invalid JSON Pointer '$input': $reason")

/** A path lookup failed. `path` is the rendered JSON Pointer; `actual` is a
  * short description of what was there (or `JsUndefined`'s message). */
final case class JsonAccessError(path: String, expected: String, actual: String)
  extends JsonException(s"JSON path '$path' did not resolve to $expected (actual: $actual)")

object JsonLines:
  /** Parse one JSON value per non-blank line. Each line has its own result so a
    * malformed event does not discard the rest of a model/event stream. */
  def parse(value: String): Seq[Try[JsValue]] =
    value.linesIterator.zipWithIndex.flatMap { case (line, index) =>
      val trimmed = line.trim
      if trimmed.isEmpty then None
      else
        Some(Try(Json.parse(trimmed)).recoverWith { case error =>
          Failure(JsonLineError(index + 1, line, error))
        })
    }.toSeq

  def stringify(values: Iterable[JsValue]): String =
    values.map(Json.stringify).mkString("\n")

final case class JsonLineError(line: Int, input: String, cause: Throwable)
  extends JsonException(s"Invalid JSON on line $line: ${cause.getMessage}")
