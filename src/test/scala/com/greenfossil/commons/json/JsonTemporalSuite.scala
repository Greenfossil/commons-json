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

    assertEquals(parsedJson("localDate").as[LocalDate], zonedDateTime.toLocalDate)
    assertEquals(parsedJson("localDate").asOpt[LocalDate], Some(zonedDateTime.toLocalDate))

    assertEquals(parsedJson("localTime").as[LocalTime], zonedDateTime.toLocalTime)
    assertEquals(parsedJson("localTime").asOpt[LocalTime], Some(zonedDateTime.toLocalTime))

    assertEquals(parsedJson("localDT").as[LocalDateTime], zonedDateTime.toLocalDateTime)
    assertEquals(parsedJson("localDT").asOpt[LocalDateTime], Some(zonedDateTime.toLocalDateTime))

    assertEquals(parsedJson("instant").as[Instant], zonedDateTime.toInstant)
    assertEquals(parsedJson("instant").asOpt[Instant], Some(zonedDateTime.toInstant))

    assertEquals(parsedJson("offsetDT").as[OffsetDateTime], zonedDateTime.toOffsetDateTime)
    assertEquals(parsedJson("offsetDT").asOpt[OffsetDateTime], Some(zonedDateTime.toOffsetDateTime))

    assertEquals(parsedJson("offsetTime").as[OffsetTime], zonedDateTime.toOffsetDateTime.toOffsetTime)
    assertEquals(parsedJson("offsetTime").asOpt[OffsetTime], Some(zonedDateTime.toOffsetDateTime.toOffsetTime))

    assertEquals(parsedJson("zonedDT").as[ZonedDateTime], zonedDateTime)
    assertEquals(parsedJson("zonedDT").asOpt[ZonedDateTime], Some(zonedDateTime))
  }
  
  test("seconds deserialization"){
    val parsedJson = Json.parse("""{"time":1714389540}""")
    assertNoDiff((parsedJson \ "time").as[LocalDateTime].toString, "2024-04-29T19:19")
    
  }

  test("String temporal extensions"){
    val instantInSecs = "1714389540".toTemporal("Instant")
    val instantInMillis = "1714389540000".toTemporal("Instant")
    assertEquals(instantInSecs, instantInMillis)
  }

}
