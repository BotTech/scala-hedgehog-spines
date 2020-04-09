package com.lightbend.hedgehog.generators.time

import java.time._
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

import com.lightbend.hedgehog.generators.StringGenerators._
import com.lightbend.hedgehog.implicits.RangeImplicits._
import hedgehog._
import org.scalactic.Requirements._

import scala.jdk.CollectionConverters._
import scala.util.Try

object TimeGenerators {

  // scalastyle:off magic.number
  private[generators] val MinLocalDateTime =
    LocalDateTime.of(Year.MIN_VALUE, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC)

  private[generators] val MaxLocalDateTime =
    LocalDateTime.of(Year.MAX_VALUE, 12, 31, 23, 59, 59, 999999999).toInstant(ZoneOffset.UTC)
  // scalastyle:on magic.number

  private lazy val ZoneIds: List[ZoneId] =
    ZoneId.getAvailableZoneIds.asScala.toList.map(ZoneId.of)

  def genYear: Gen[Year] =
    genChronoFieldInt(ChronoField.YEAR).map(Year.of)

  def genMonth: Gen[Month] =
    genChronoFieldInt(ChronoField.MONTH_OF_YEAR).map(Month.of)

  def genDayOfMonth: Gen[Int] =
    Gen.int(chronoFieldIntRange(ChronoField.DAY_OF_MONTH))

  def genHourOfDay: Gen[Int] =
    Gen.int(chronoFieldIntRange(ChronoField.HOUR_OF_DAY))

  def genMinuteOfHour: Gen[Int] =
    Gen.int(chronoFieldIntRange(ChronoField.MINUTE_OF_HOUR))

  def genSecondOfMinute: Gen[Int] =
    Gen.int(chronoFieldIntRange(ChronoField.SECOND_OF_MINUTE))

  def genNanoOfDay: Gen[Long] =
    Gen.long(chronoFieldRange(ChronoField.NANO_OF_DAY))

  def genNanoOfSecond: Gen[Int] =
    Gen.int(chronoFieldIntRange(ChronoField.NANO_OF_SECOND))

  def genEpochDay: Gen[Long] =
    Gen.long(chronoFieldRange(ChronoField.EPOCH_DAY))

  def genLocalDate: Gen[LocalDate] =
    genEpochDay.map(LocalDate.ofEpochDay)

  def genLocalTime: Gen[LocalTime] =
    genNanoOfDay.map(LocalTime.ofNanoOfDay)

  def genLocalDateTime: Gen[LocalDateTime] =
    for {
      date <- genLocalDate
      time <- genLocalTime
    } yield LocalDateTime.of(date, time)

  def genInstant: Gen[Instant] =
    genInstantLinearFrom(Instant.EPOCH, Instant.MIN, Instant.MAX)

  def genLocalDateTimeInstant: Gen[Instant] =
    genInstantLinearFrom(Instant.EPOCH, MinLocalDateTime, MaxLocalDateTime)

  def genInstantLinear(x: Instant, y: Instant): Gen[Instant] =
    genInstantLinearFrom(x, x, y)

  // TODO: Ought to use a Range[Instant] here but I can't be bothered implementing the type classes for Instant.
  def genInstantLinearFrom(z: Instant, x: Instant, y: Instant): Gen[Instant] =
    for {
      epochSecond    <- Gen.long(Range.linearFrom(z.getEpochSecond, x.getEpochSecond, y.getEpochSecond))
      nanoAdjustment <- Gen.int(Range.linearFrom(z.getNano, x.getNano, y.getNano))
    } yield Instant.ofEpochSecond(epochSecond, nanoAdjustment.toLong)

  def genZonedDateTime: Gen[ZonedDateTime] =
    for {
      // Use an instant rather than LocalDateTime so that we can ensure the proper ordering.
      instant <- genLocalDateTimeInstant
      zone    <- genZoneIdByInstant(instant)
    } yield ZonedDateTime.ofInstant(instant, zone)

  def genZoneId: Gen[ZoneId] =
    Gen.elementUnsafe(ZoneIds)

  private def genZoneIdByInstant(instant: Instant): Gen[ZoneId] = {
    val ids = ZoneIds.flatMap(validOffset(instant, _)).sortBy(_.getTotalSeconds)
    Gen.elementUnsafe(ids)
  }

  private[generators] def validOffset(instant: Instant, zone: ZoneId): Option[ZoneOffset] = {
    // Certain combinations of Instant's and ZoneRules do not work.
    // See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=9063277
    // Different JDK versions also have different behaviours
    val offset = for {
      offset <- Try(zone.getRules.getOffset(instant))
      _      <- Try(ZonedDateTime.ofInstant(instant, zone))
    } yield offset
    offset.toOption
  }

  def genFormattedIsoInstant: Gen[String] =
    genFormattedDateTime(DateTimeFormatter.ISO_INSTANT)

  def genInvalidFormattedIsoInstant: Gen[String] =
    genAnyUnicodeString.map { s =>
      Try(DateTimeFormatter.ISO_INSTANT.parse(s)).failed.map(_ => s).getOrElse(s.replaceAllLiterally("T", ""))
    }

  private def genFormattedDateTime(formatter: DateTimeFormatter): Gen[String] =
    genZonedDateTime.map(_.format(formatter))

  private def genChronoFieldInt(chronoField: ChronoField): Gen[Int] =
    Gen.int(chronoFieldIntRange(chronoField))

  private def chronoFieldIntRange(chronoField: ChronoField): Range[Int] = {
    def toInt(value: Long): Int = {
      require(value >= Int.MinValue)
      require(value <= Int.MaxValue)
      value.toInt
    }
    val range = chronoFieldRange(chronoField)
    Range.linearFrom(toInt(range.origin), toInt(range.x), toInt(range.y))
  }

  private def chronoFieldRange(chronoField: ChronoField): Range[Long] = {
    val chronoRange = chronoField.range()
    val min         = chronoRange.getMinimum
    val max         = chronoRange.getMaximum
    val origin      = if (min <= 0 && 0 <= max) 0 else min
    Range.linearFrom(origin, min, max)
  }
}
