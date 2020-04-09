package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path.{Segment, Slash, SlashOrEmpty}
import akka.http.scaladsl.model.Uri.{Authority, Host, IPv4Host, IPv6Host, NamedHost, NonEmptyHost, Path}
import com.lightbend.hedgehog.generators.ArbitraryGenerators._
import com.lightbend.hedgehog.generators.ByteGenerators._
import com.lightbend.hedgehog.generators.StringGenerators._
import com.lightbend.hedgehog.implicits.Implicits._
import hedgehog._
import org.scalactic.TripleEquals._

import scala.util.Try

// scalastyle:off number.of.methods

object UriGenerators {

  private val IPv6Bytes         = 16
  private val MaxSchemeLength   = 100
  private val MaxHostLength     = 100
  private val MaxPort           = 65535
  private val MaxUserInfoLength = 100
  private val MaxSegmentLength  = 100
  private val MaxQueryLength    = 1000
  private val MaxFragmentLength = 100

  private val Upper: String              = ('A' to 'Z').mkString
  private val Lower: String              = ('a' to 'z').mkString
  private val Digit: String              = ('0' to '9').mkString
  private val Alpha: String              = Upper + Lower
  private val AlphaNum: String           = Alpha + Digit
  private val HexDigit: String           = Upper + Digit
  private val Unreserved: String         = Alpha + Digit + "-._~"
  private val SubDelims: String          = "!$&'()*+,;="
  private val OtherSchemeChars: String   = "+-."
  private val OtherUserInfoChars: String = ":"
  private val OtherPChars: String        = ":@"
  private val OtherQueryChars: String    = "/?"
  private val OtherFragmentChars: String = OtherQueryChars

  // This is already a hex generator in Hedgehog but we have to know the exact number of elements so redefine it
  // here just in case it ever changes.
  private def genHexDigit: Gen[Char] = Gen.elementUnsafe(HexDigit.toList)

  private def genPctEncoded: Gen[String] =
    for {
      a <- genHexDigit
      b <- genHexDigit
    } yield s"%${a.toString}${b.toString}"

  private def stringFrequency(s: String): (Int, Gen[String]) =
    genElementFrequencyUnsafe(s.toList.map(_.toString))

  private def genSchemeChar: Gen[String] =
    Gen.frequency1(
      stringFrequency(AlphaNum),
      stringFrequency(OtherSchemeChars)
    )

  def genScheme: Gen[String] =
    for {
      head <- Gen.alpha
      tail <- genConcatenatedString(genSchemeChar, Range.linear(0, MaxSchemeLength - 1))
    } yield head.toString + tail

  def genHttpScheme: Gen[String] =
    Gen.element1("http", "https", "ws", "wss")

  def genEmptyHost: Gen[Host.Empty.type] =
    Gen.constant(Host.Empty)

  def genIPv4Host: Gen[IPv4Host] =
    for {
      byte1 <- genByte
      byte2 <- genByte
      byte3 <- genByte
      byte4 <- genByte
    } yield IPv4Host(byte1, byte2, byte3, byte4)

  def genIPv6Host: Gen[IPv6Host] =
    for {
      bytes <- Gen.bytes(Range.singleton(IPv6Bytes))
    } yield IPv6Host(bytes)

  private def genRegName: Gen[String] =
    Gen.frequency1(
      stringFrequency(Unreserved),
      (HexDigit.length * HexDigit.length) -> genPctEncoded,
      stringFrequency(SubDelims)
    )

  def genNamedHost: Gen[NamedHost] =
    genConcatenatedString(genRegName, Range.linear(1, MaxHostLength)).map(NamedHost)

  def genNonEmptyHost: Gen[NonEmptyHost] =
    Gen.choice1(genIPv4Host.widen, genIPv6Host.widen, genNamedHost.widen)

  def genHost: Gen[Host] =
    Gen.choice1(genEmptyHost.widen, genNonEmptyHost.widen)

  def genEmptyAuthority: Gen[Authority] =
    Gen.constant(Authority.Empty)

  private def genUserInfoChoices: Gen[String] =
    Gen.frequency1(
      stringFrequency(Unreserved),
      (HexDigit.length * HexDigit.length) -> genPctEncoded,
      stringFrequency(SubDelims),
      stringFrequency(OtherUserInfoChars)
    )

  def genNonEmptyUserInfo: Gen[String] =
    genConcatenatedString(genUserInfoChoices, Range.linear(1, MaxUserInfoLength))

  def genEmptyUserInfo: Gen[String] =
    Gen.constant("")

  def genUserInfo: Gen[String] =
    Gen.choice1(genEmptyUserInfo, genNonEmptyUserInfo)

  def genNonEmptyAuthority: Gen[Authority] =
    for {
      host     <- genHost
      port     <- genPort
      userInfo <- if (host.isEmpty && port === 0) genNonEmptyUserInfo else genUserInfo
    } yield Authority(host, port, userInfo)

  def genAuthority: Gen[Authority] =
    Gen.choice1(genEmptyAuthority, genNonEmptyAuthority)

  def genPort: Gen[Int] = Gen.int(Range.linear(0, MaxPort))

  def genEmptyPath: Gen[Path.Empty.type] =
    Gen.constant(Path.Empty)

  def genSlash: Gen[Slash] = Gen.sized { size =>
    genPath.resize(Size(math.max(1, size.value - 1))).map(Slash)
  }

  def genSlashOrEmpty: Gen[SlashOrEmpty] =
    Gen.choice1(genEmptyPath.widen, genSlash.widen)

  private def genPChar: Gen[String] =
    Gen.frequency1(
      stringFrequency(Unreserved),
      (HexDigit.length * HexDigit.length) -> genPctEncoded,
      stringFrequency(SubDelims),
      stringFrequency(OtherPChars)
    )

  def genSegment: Gen[Segment] =
    for {
      head <- genConcatenatedString(genPChar, Range.linear(1, MaxSegmentLength))
      tail <- genSlashOrEmpty
    } yield Segment(head, tail)

  def genNonSlash: Gen[Path] =
    Gen.choice1(genEmptyPath.widen, genSegment.widen)

  def genSingleSlash: Gen[Path] = Gen.sized { size =>
    genNonSlash.resize(Size(math.max(1, size.value - 1))).map(Slash)
  }

  def genSingleSlashOrEmpty: Gen[Path] =
    Gen.choice1(genEmptyPath.widen, genSingleSlash.widen)

  def genNonSlashSlash: Gen[Path] =
    Gen.choice1(genSingleSlashOrEmpty, genSegment.widen)

  def genPath: Gen[Path] =
    Gen.choice1(genSlashOrEmpty.widen, genSegment.widen)

  def genPathForAuthority(authority: Authority): Gen[Path] =
    if (authority.isEmpty) genNonSlashSlash
    else genSingleSlashOrEmpty

  private def genQueryParts: Gen[String] =
    Gen.frequency1(
      stringFrequency(Unreserved),
      (HexDigit.length * HexDigit.length) -> genPctEncoded,
      stringFrequency(SubDelims),
      stringFrequency(OtherPChars),
      stringFrequency(OtherQueryChars)
    )

  def genRawQuery: Gen[String] =
    genConcatenatedString(genQueryParts, Range.linear(0, MaxQueryLength))

  private def genFragmentParts: Gen[String] =
    Gen.frequency1(
      stringFrequency(Unreserved),
      (HexDigit.length * HexDigit.length) -> genPctEncoded,
      stringFrequency(SubDelims),
      stringFrequency(OtherPChars),
      stringFrequency(OtherFragmentChars)
    )

  def genFragment: Gen[String] =
    genConcatenatedString(genFragmentParts, Range.linear(0, MaxFragmentLength))

  def genUri: Gen[Uri] =
    for {
      authority   <- genAuthority
      path        <- genPathForAuthority(authority)
      scheme      <- genScheme
      queryString <- genRawQuery.option
      fragment    <- genFragment.option
    } yield Uri(scheme, authority, path, queryString, fragment)

  def genHttpUri: Gen[Uri] =
    genHttpScheme.flatMap(scheme => genUri.map(_.copy(scheme = scheme)))

  // TODO: Do this without discarding.
  def genInvalidUri: Gen[String] =
    genAnyUnicodeString.filter(s => Try(Uri(s)).isFailure)
}

// scalastyle:on number.of.methods
