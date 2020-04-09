package com.lightbend.hedgehog.generators.time

import java.time._
import java.time.temporal.ChronoField

import com.lightbend.hedgehog.generators.time.TimeGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.GeneratorTests._
import hedgehog._
import hedgehog.runner._

import scala.util.Try

object TimeGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  override def tests: List[Test] =
    chronoFieldGenTests("genYear", genYear.map(_.getValue.toLong), ChronoField.YEAR, 0) ++
      chronoFieldGenTests(
        "genMonth",
        genMonth.map(_.getValue.toLong),
        ChronoField.MONTH_OF_YEAR,
        Month.JANUARY.getValue.toLong
      ) ++
      chronoFieldGenTests("genDayOfMonth", genDayOfMonth.map(_.toLong), ChronoField.DAY_OF_MONTH, 1) ++
      chronoFieldGenTests("genHourOfDay", genHourOfDay.map(_.toLong), ChronoField.HOUR_OF_DAY, 0) ++
      chronoFieldGenTests("genMinuteOfHour", genMinuteOfHour.map(_.toLong), ChronoField.MINUTE_OF_HOUR, 0) ++
      chronoFieldGenTests("genSecondOfMinute", genSecondOfMinute.map(_.toLong), ChronoField.SECOND_OF_MINUTE, 0) ++
      chronoFieldGenTests("genNanoOfDay", genNanoOfDay, ChronoField.NANO_OF_DAY, 0) ++
      chronoFieldGenTests("genNanoOfSecond", genNanoOfSecond.map(_.toLong), ChronoField.NANO_OF_SECOND, 0) ++
      chronoFieldGenTests("genEpochDay", genEpochDay, ChronoField.EPOCH_DAY, 0) ++
      scaledTimeTests("genLocalDate", genLocalDate, LocalDate.MIN, LocalDate.MAX)(scaleLocalDate) ++
      scaledTimeTests("genLocalTime", genLocalTime, LocalTime.MIN, LocalTime.MAX)(scaleLocalTime) ++
      scaledTimeTests("genLocalDateTime", genLocalDateTime, LocalDateTime.MIN, LocalDateTime.MAX)(scaleLocalDateTime) ++
      scaledTimeTests("genInstant", genInstant, Instant.MIN, Instant.MAX)(scaleInstant) ++
      test("genInstantLinear", genAnyInstantLinear)
        .addProp(_ + "generates instants inside bounds", propInstantLinearInsideBounds)
        .tests ++
      scaledTimeTests("genZonedDateTime", genZonedDateTime.map(_.toInstant), MinLocalDateTime, MaxLocalDateTime)(
        scaleInstant
      ) ++
      scaledTimeTests(
        "genFormattedIsoInstant",
        genFormattedIsoInstant.map(Instant.parse),
        MinLocalDateTime,
        MaxLocalDateTime
      )(scaleInstant) ++
      test("genInvalidFormattedIsoInstant", genInvalidFormattedIsoInstant)
        .addGenProp(_ + " produces invalid ISO instants", propInvalidISOInstant)
        .tests ++ List(
      example(
        "validOffset works around https://bugs.java.com/bugdatabase/view_bug.do?bug_id=9063277",
        test9063277Workaround
      ),
      property("validOffset returns all zones of a valid ZonedDateTime", propValidOffsetsOfZonedDateTime),
      property("validOffset returns all zones which are valid for ZonedDateTimes", propValidOffsetsForZonedDateTime)
    )

  private def chronoFieldGenTests(name: String, gen: Gen[Long], chronoField: ChronoField, origin: Long) =
    test(name, gen).addLinearNumericRangeTests(origin, chronoField.range.getMinimum, chronoField.range.getMaximum).tests

  private def scaledTimeTests[A: Ordering](name: String, gen: Gen[A], min: A, max: A)(scale: A => A) =
    test(name, gen).addLinearRangeTests(min, max)(scale).tests

  private def scaleLocalDate(localDate: LocalDate): LocalDate =
    LocalDate.ofEpochDay(minScaled(localDate.toEpochDay))

  private def scaleLocalTime(localTime: LocalTime): LocalTime =
    LocalTime.ofNanoOfDay(minScaled(localTime.toNanoOfDay))

  private def scaleLocalDateTime(localDateTime: LocalDateTime): LocalDateTime =
    LocalDateTime.of(scaleLocalDate(localDateTime.toLocalDate), scaleLocalTime(localDateTime.toLocalTime))

  private def scaleInstant(instant: Instant): Instant =
    Instant.ofEpochSecond(minScaled(instant.getEpochSecond), minScaled(instant.getNano.toLong))

  private def test9063277Workaround: Result =
    validOffset(MaxLocalDateTime, ZoneId.of("NZ")).isEmpty ==== true

  private def propInstantLinearInsideBounds: Property = forAll {
    for {
      x       <- genInstant
      y       <- genInstant
      instant <- genInstantLinear(x, y)
    } yield instant.inside(x, y)
  }

  private def propInvalidISOInstant(gen: Gen[String]): Property =
    gen.forAll.map { s =>
      Result.assert(Try(Instant.parse(s)).isFailure)
    }

  private def propValidOffsetsOfZonedDateTime: Property = forAll {
    for {
      instant <- genLocalDateTimeInstant
      zone    <- genZoneId
    } yield {
      if (Try(ZonedDateTime.ofInstant(instant, zone)).isSuccess) {
        Result.assert(validOffset(instant, zone).isDefined)
      } else Result.success
    }
  }

  private def propValidOffsetsForZonedDateTime: Property = forAll {
    for {
      instant <- genLocalDateTimeInstant
      zone    <- genZoneId
    } yield {
      if (validOffset(instant, zone).isDefined) {
        testValidOffsetForZonedDateTime(instant, zone)
      } else Result.success
    }
  }

  private def testValidOffsetForZonedDateTime(instant: Instant, zone: ZoneId): Result =
    Result.assert(Try(ZonedDateTime.ofInstant(instant, zone)).isSuccess)

  private def genAnyInstantLinear: Gen[Instant] =
    for {
      x       <- genInstant
      y       <- genInstant
      instant <- genInstantLinear(x, y)
    } yield instant
}
