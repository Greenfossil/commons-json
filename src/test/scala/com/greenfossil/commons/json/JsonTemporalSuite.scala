/*
 * Copyright 2022 Greenfossil Pte Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greenfossil.commons.json

import scala.language.implicitConversions

class JsonTemporalSuite extends munit.FunSuite {
  import java.time.*

  test("JsTemporal creation") {
    {
      val jDate = java.util.Date()
      val jsTemp = JsTemporal(jDate)
      assertNoDiff(jsTemp.stringify, jDate.toInstant.toEpochMilli.toString)
    }

    {
      val localDate = LocalDate.now
      val jsTemp = JsTemporal(localDate)
      assertNoDiff(jsTemp.stringify, s"\"${localDate.toString}\"")
    }

    {
      val localTime = LocalTime.now
      val jsTemp = JsTemporal(localTime)
      assertNoDiff(jsTemp.stringify, s"\"${localTime.toString}\"")
    }

    {
      val localDT = LocalDateTime.now
      val jsTemp = JsTemporal(localDT)
      assertNoDiff(jsTemp.stringify, s"\"${localDT.toString}\"")
    }

    {
      val instant = Instant.now
      val jsTemp = JsTemporal(instant)
      assertNoDiff(jsTemp.stringify, s"\"${instant.toString}\"")
    }

    {
      val offsetDT = OffsetDateTime.now
      val jsTemp = JsTemporal(offsetDT)
      assertNoDiff(jsTemp.stringify, s"\"${offsetDT.toString}\"")
    }

    {
      val offsetTime = OffsetTime.now
      val jsTemp = JsTemporal(offsetTime)
      assertNoDiff(jsTemp.stringify, s"\"${offsetTime.toString}\"")
    }

    {
      val zonedDT = ZonedDateTime.now
      val jsTemp = JsTemporal(zonedDT)
      assertNoDiff(jsTemp.stringify, s"\"${zonedDT.toString}\"")
    }

  }

  test("Format") {
    val localDate = LocalDate.now
    val jsTemp = JsTemporal(localDate).jsonFormat("yyyy")
    assertNoDiff(jsTemp.stringify, s"\"${localDate.getYear}\"")
  }

  test("Json Serialization") {
    val jsObj = Json.obj(
      "date1" -> LocalDate.now,
      "date2" -> JsTemporal(LocalDate.now, "yyyy"),
      "date3" -> LocalDate.now.jsonFormat("yyyy"),
      "time1" -> LocalTime.now,
      "time2" -> JsTemporal(LocalTime.now, "HH:mm"),
      "time3" -> LocalTime.now.jsonFormat("HH:mm"),
      "instant1" -> Instant.now,
      "instant2" -> JsTemporal(Instant.now, "HH:mm"),
      "instant3" -> Instant.now.jsonFormat("HH:mm"),
      "offsetDT1" -> OffsetDateTime.now,
      "offsetDT2" -> JsTemporal(OffsetDateTime.now, "HH:mm"),
      "offsetDT3" -> OffsetDateTime.now.jsonFormat("HH:mm"),
      "zonedDT1" -> ZonedDateTime.now,
      "zonedDT2" -> JsTemporal(ZonedDateTime.now, "HH:mm"),
      "zonedDT3" -> ZonedDateTime.now.jsonFormat("HH:mm"),
      "jdate1" -> java.util.Date(),
      "jdate2" -> java.util.Date().jsonFormat("yyyy"),
      "jdate3" -> java.util.Date().jsonFormat("yyyy", "Asia/Singapore")
    )

    assert(jsObj("date1").isInstanceOf[JsTemporal])
    assert(jsObj("date2").isInstanceOf[JsTemporal])
    assert(jsObj("date3").isInstanceOf[JsTemporal])
    assertNoDiff(jsObj("date2").stringify, jsObj("date3").stringify)

    assert(jsObj("time1").isInstanceOf[JsTemporal])
    assert(jsObj("time2").isInstanceOf[JsTemporal])
    assert(jsObj("time3").isInstanceOf[JsTemporal])
    assertNoDiff(jsObj("time2").stringify, jsObj("time3").stringify)

    assert(jsObj("instant1").isInstanceOf[JsTemporal])
    assert(jsObj("instant2").isInstanceOf[JsTemporal])
    assert(jsObj("instant3").isInstanceOf[JsTemporal])
    assertNoDiff(jsObj("instant2").stringify, jsObj("instant3").stringify)

    assert(jsObj("date1").isInstanceOf[JsTemporal])
    assert(jsObj("date2").isInstanceOf[JsTemporal])
    assert(jsObj("date3").isInstanceOf[JsTemporal])
    assertNoDiff(jsObj("date2").stringify, jsObj("date3").stringify)
  }

  test("Json DeSerialization") {
    val localDateTime = LocalDate.of(2022, 2, 7).atTime(10, 30, 45, 1)
    val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

    val jsObj = Json.obj(
      "jDate" -> java.util.Date.from(zonedDateTime.toInstant),
      "localDate" -> zonedDateTime.toLocalDate,
      "localTime" -> zonedDateTime.toLocalTime,
      "localDT" -> zonedDateTime.toLocalDateTime,
      "instant" -> zonedDateTime.toInstant,
      "offsetDT" -> zonedDateTime.toOffsetDateTime,
      "offsetTime" -> zonedDateTime.toOffsetDateTime.toOffsetTime,
      "zonedDT" -> zonedDateTime,
    )
    val json = jsObj.stringify

    val parsedJson = Json.parse(json).as[JsObject]

    val u = parsedJson("jDate")
    val x = u.as[java.util.Date]
    assertEquals(x, java.util.Date.from(zonedDateTime.toInstant))
    assertEquals(parsedJson("jDate").asOpt[java.util.Date], Some(java.util.Date.from(zonedDateTime.toInstant)))

    val ldJson = parsedJson("localDate")
    assertEquals(ldJson.as[LocalDate], ldJson.asLocalDate)
    assertEquals(ldJson.asOpt[LocalDate], ldJson.asLocalDateOpt)
    assertEquals(ldJson.asOpt[LocalDate].orNull, ldJson.asLocalDateOrNull)
    assertEquals(ldJson.as[LocalDate], zonedDateTime.toLocalDate)
    assertEquals(ldJson.asOpt[LocalDate], Some(zonedDateTime.toLocalDate))

    val ltJson = parsedJson("localTime")
    assertEquals(ltJson.as[LocalTime], ltJson.asLocalTime)
    assertEquals(ltJson.asOpt[LocalTime], ltJson.asLocalTimeOpt)
    assertEquals(ltJson.asOpt[LocalTime].orNull, ltJson.asLocalTimeOrNull)
    assertEquals(ltJson.as[LocalTime], zonedDateTime.toLocalTime)
    assertEquals(ltJson.asOpt[LocalTime], Some(zonedDateTime.toLocalTime))

    val ldtJson = parsedJson("localDT")
    assertEquals(ldtJson.as[LocalDateTime], ldtJson.asLocalDateTime)
    assertEquals(ldtJson.asOpt[LocalDateTime], ldtJson.asLocalDateTimeOpt)
    assertEquals(ldtJson.asOpt[LocalDateTime].orNull, ldtJson.asLocalDateTimeOrNull)
    assertEquals(ldtJson.as[LocalDateTime], zonedDateTime.toLocalDateTime)
    assertEquals(ldtJson.asOpt[LocalDateTime], Some(zonedDateTime.toLocalDateTime))

    val instantJson = parsedJson("instant")
    assertEquals(instantJson.as[Instant], instantJson.asInstant)
    assertEquals(instantJson.asOpt[Instant], instantJson.asInstantOpt)
    assertEquals(instantJson.asOpt[Instant].orNull, instantJson.asInstantOrNull)
    assertEquals(instantJson.as[Instant], zonedDateTime.toInstant)
    assertEquals(instantJson.asOpt[Instant], Some(zonedDateTime.toInstant))

    val odtJson = parsedJson("offsetDT")
    assertEquals(odtJson.as[OffsetDateTime], odtJson.asOffsetDateTime)
    assertEquals(odtJson.asOpt[OffsetDateTime], odtJson.asOffsetDateTimeOpt)
    assertEquals(odtJson.asOpt[OffsetDateTime].orNull, odtJson.asOffsetDateTimeOrNull)
    assertEquals(odtJson.as[OffsetDateTime], zonedDateTime.toOffsetDateTime)
    assertEquals(odtJson.asOpt[OffsetDateTime], Some(zonedDateTime.toOffsetDateTime))

    val otJson = parsedJson("offsetTime")
    assertEquals(otJson.as[OffsetTime], otJson.asOffsetTime)
    assertEquals(otJson.asOpt[OffsetTime], otJson.asOffsetTimeOpt)
    assertEquals(otJson.asOpt[OffsetTime].orNull, otJson.asOffsetTimeOrNull)
    assertEquals(otJson.as[OffsetTime], zonedDateTime.toOffsetDateTime.toOffsetTime)
    assertEquals(otJson.asOpt[OffsetTime], Some(zonedDateTime.toOffsetDateTime.toOffsetTime))

    val zdtJson = parsedJson("zonedDT")
    assertEquals(zdtJson.as[ZonedDateTime], zdtJson.asZonedDateTime)
    assertEquals(zdtJson.asOpt[ZonedDateTime], zdtJson.asZonedDateTimeOpt)
    assertEquals(zdtJson.asOpt[ZonedDateTime].orNull, zdtJson.asZonedDateTimeOrNull)
    assertEquals(zdtJson.as[ZonedDateTime], zonedDateTime)
    assertEquals(zdtJson.asOpt[ZonedDateTime], Some(zonedDateTime))
  }
  
  test("seconds deserialization"){
    val parsedJson = Json.parse("""{"time":1714389540}""")
    //Drop the time component to avoid test failure due to timezone differences
    assertNoDiff((parsedJson \ "time").as[LocalDate].toString, "2024-04-29")
    
  }

  test("String temporal extensions"){
    val instantInSecs = "1714389540".toTemporal("Instant")
    val instantInMillis = "1714389540000".toTemporal("Instant")
    assertEquals(instantInSecs, instantInMillis)
  }

}
