package com.lightbend.hedgehog.generators.net

import java.net.URI

import com.lightbend.hedgehog.generators.CharGenerators._
import com.lightbend.hedgehog.generators.StringGenerators._
import com.lightbend.hedgehog.generators.net.InetAddressGenerators._
import hedgehog._

import scala.util.Try

object URIGenerators {

  // FIXME: Investigate why if these are all 1 then why are there discards?
  private val MaxSchemeLength          = 10
  private val MaxPort                  = 65535
  private val MaxUserInfoLength        = 20
  private val MaxQueryLength           = 20
  private val MaxFragmentLength        = 20
  private val MaxOpaquePartLength      = 100
  private val MaxRelativeSegmentLength = 10
  private val MaxRegisteredNameLength  = 20
  private val MaxHostNameLabels        = 5
  private val MaxHostNameLabelLength   = 10
  private val MaxPathSegments          = 5
  private val MaxPChars                = 20
  private val MaxParams                = 5

  def genURI: Gen[URI] =
    Gen.choice1(genAbsoluteURI, genRelativeURI)

  def genAbsoluteURI: Gen[URI] =
    for {
      absolute <- genAbsoluteURIWithoutFragment
      uri      <- withFragment(absolute)
    } yield new URI(uri)

  def genRelativeURI: Gen[URI] =
    for {
      absolute <- genRelativeURIWithoutFragment
      uri      <- withFragment(absolute)
    } yield new URI(uri)

  private def withFragment(uri: String): Gen[String] =
    for {
      // URI deviation from RFC 2396:
      // If the authority is empty then it must be followed by a non-empty path, a query
      // component, or a fragment component.
      fragment <- if (uri.endsWith("//")) genFragment.map(Some(_)) else genFragment.option
    } yield uri + optionalSuffix("#", fragment)

  def genAbsoluteURIWithoutFragment: Gen[String] =
    for {
      scheme     <- genScheme
      schemePart <- Gen.choice1(genHierarchicalPart, genOpaquePart)
    } yield s"$scheme:$schemePart"

  def genRelativeURIWithoutFragment: Gen[String] =
    for {
      path  <- Gen.choice1(genNetPath, genAbsolutePath, genRelativePath)
      query <- genQuery.option
    } yield path + optionalSuffix("?", query)

  def genHierarchicalPart: Gen[String] =
    for {
      path  <- Gen.choice1(genNetPath, genAbsolutePath)
      query <- genQuery.option
    } yield path + optionalSuffix("?", query)

  def genOpaquePart: Gen[String] =
    for {
      uricNoSlash <- genUricNoSlash
      urics       <- genRepeat(genUric, 0, MaxOpaquePartLength - 1)
    } yield uricNoSlash + urics.mkString

  def genUricNoSlash: Gen[String] =
    Gen.choice1(
      genUnreserved,
      genEscaped,
      Gen.element1(";", "?", ":", "@", "&", "=", "+", "$", ",")
    )

  def genNetPath: Gen[String] =
    for {
      authority <- genAuthority
      path      <- genAbsolutePath.option
    } yield s"//$authority${path.getOrElse("")}"

  def genAbsolutePath: Gen[String] =
    for {
      segments <- genPathSegments
    } yield s"/$segments"

  // URI deviation from RFC 2396:
  // The relative path may be empty
  def genRelativePath: Gen[String] = {
    for {
      relativeSegment <- genRelativeSegment
      path            <- genAbsolutePath.option
    } yield relativeSegment + path.getOrElse("")
  }.option.map(_.getOrElse(""))

  def genRelativeSegment: Gen[String] =
    genRepeat(
      Gen.choice1(
        genUnreserved,
        genEscaped,
        Gen.element1(";", "@", "&", "=", "+", "$", ",")
      ),
      1,
      MaxRelativeSegmentLength
    )

  def genScheme: Gen[String] =
    for {
      head <- Gen.alpha
      tail <- genRepeat(Gen.choice1(genAlpha, genDigit, Gen.element1("+", "-", ".")), 0, MaxSchemeLength - 1)
    } yield head.toString + tail

  def genAuthority: Gen[String] =
    Gen.choice1(genServer, genRegisteredName)

  def genRegisteredName: Gen[String] =
    genRepeat(
      Gen.choice1(
        genUnreserved,
        genEscaped,
        Gen.element1("$", ",", ";", ":", "@", "&", "=", "+")
      ),
      1,
      MaxRegisteredNameLength
    )

  def genServer: Gen[String] =
    for {
      userInfo <- genUserInfo.option
      hostPort <- genHostPort.option
    } yield hostPort.map(optionalPrefix(userInfo, "@") + _).getOrElse("")

  def genUserInfo: Gen[String] =
    genRepeat(
      Gen.choice1(
        genUnreserved,
        genEscaped,
        Gen.element1(";", ":", "&", "=", "+", "$", ",")
      ),
      0,
      MaxUserInfoLength
    )

  def genHostPort: Gen[String] =
    for {
      host <- genHost
      port <- genPort.option
    } yield host + optionalSuffix(":", port.map(_.toString))

  // URI deviation from RFC 2396:
  // IPv6 addresses are valid.
  def genHost: Gen[String] =
    Gen.choice1(genHostName, genIPv4Address, genIPv6Address)

  def genHostName: Gen[String] =
    for {
      domainLabel <- genRepeat(genDomainLabel.map(_ + "."), 0, MaxHostNameLabels - 1)
      // URI deviation from RFC 2396:
      // If there is only one label then it may start with a number.
      topLabel <- if (domainLabel.isEmpty) genDomainLabel else genTopLabel
      period   <- Gen.constant(".").option
    } yield domainLabel + topLabel + period.getOrElse("")

  def genDomainLabel: Gen[String] =
    Gen.choice1(
      genAlphaNum,
      for {
        head <- genAlphaNum
        mid  <- genAlphaNumDash
        last <- genAlphaNum
      } yield head + mid + last
    )

  def genTopLabel: Gen[String] =
    Gen.choice1(
      genAlpha,
      for {
        head <- genAlpha
        mid  <- genAlphaNumDash
        last <- genAlphaNum
      } yield head + mid + last
    )

  private def genAlphaNumDash =
    genRepeat(Gen.choice1(genAlphaNum, Gen.constant("-")), 0, MaxHostNameLabelLength - 2)

  // URI deviation from RFC 2396:
  // Strict format of IPv4 addresses.
  def genIPv4Address: Gen[String] =
    genRawInet4Address.map(_.getHostAddress)

  def genIPv6Address: Gen[String] =
    genRawInet6Address.map(a => s"[${a.getHostAddress}]")

  // TODO: The BNF is wider than this.
  def genPort: Gen[Int] =
    Gen.int(Range.linear(0, MaxPort))

  def genPath: Gen[String] =
    Gen.choice1(genAbsolutePath, genOpaquePart).option.map(_.getOrElse(""))

  def genPathSegments: Gen[String] =
    genSegment.list(Range.linear(1, MaxPathSegments)).map(_.mkString("/"))

  def genSegment: Gen[String] =
    for {
      init <- genPChars
      tail <- genRepeat(genParam.map(";" + _), 0, MaxParams)
    } yield init + tail

  def genParam: Gen[String] = genPChars

  private def genPChars: Gen[String] =
    genRepeat(genPChar, 0, MaxPChars)

  def genPChar: Gen[String] =
    Gen.choice1(
      genUnreserved,
      genEscaped,
      Gen.element1(":", "@", "&", "=", "+", "$", ",")
    )

  def genQuery: Gen[String] = genRepeat(genUric, 0, MaxQueryLength)

  def genFragment: Gen[String] = genRepeat(genUric, 0, MaxFragmentLength)

  def genUric: Gen[String] =
    Gen.choice1(genReserved, genUnreserved, genEscaped)

  // URI deviation from RFC 2396:
  // Added for RFC 2732.
  def genReserved: Gen[String] =
    Gen.element1(";", "/", "?", ":", "@", "&", "=", "+", "$", ",", "[", "]")

  def genUnreserved: Gen[String] =
    Gen.choice1(genAlphaNum, genMark)

  def genMark: Gen[String] =
    Gen.element1("-", "_", ".", "!", "~", "*", "'", "(", ")")

  // URI deviation from RFC 2396:
  // Support "other" characters wherever escaped characters are supported.
  def genEscaped: Gen[String] =
    Gen.choice1(
      for {
        h1 <- Gen.hexit
        h2 <- Gen.hexit
      } yield s"%${h1.toString}${h2.toString}",
      genOther
    )

  private def genOther: Gen[String] =
    genVisibleNonUSASCIIChar.map(_.toString)

  def genAlphaNum: Gen[String] =
    Gen.alphaNum.map(_.toString)

  def genAlpha: Gen[String] =
    Gen.alpha.map(_.toString)

  def genDigit: Gen[String] =
    Gen.digit.map(_.toString)

  // TODO: Do this without discarding.
  def genInvalidURI: Gen[String] =
    genAnyUnicodeString.filter(s => Try(new URI(s)).isFailure)

  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  def copyURI(
      uri: URI
    )(
      scheme: String = uri.getScheme,
      userInfo: String = uri.getUserInfo,
      host: String = uri.getHost,
      port: Int = uri.getPort,
      path: String = uri.getPath,
      query: String = uri.getQuery,
      fragment: String = uri.getFragment
    ): URI =
    new URI(scheme, userInfo, host, port, path, query, fragment)

  private def optionalSuffix(separator: String, suffix: Option[String]): String =
    suffix.map(separator + _).getOrElse("")

  private def optionalPrefix(prefix: Option[String], separator: String): String =
    prefix.map(_ + separator).getOrElse("")

  private def genRepeat(gen: Gen[String], min: Int, max: Int) =
    gen.list(Range.linear(min, max)).map(_.mkString)
}
