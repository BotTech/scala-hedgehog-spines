package com.lightbend.hedgehog.generators.akka

import java.net.URI

import akka.actor.{Address, AddressFromURIString}
import com.lightbend.hedgehog.generators.net.URIGenerators._
import hedgehog._

// scalastyle:off null
@SuppressWarnings(Array("org.wartremover.warts.Null", "scalafix:DisableSyntax.null", "NullParameter"))
object AddressGenerators {

  def genLocalAddress: Gen[Address] =
    for {
      uri      <- genAbsoluteURI
      protocol <- genScheme
      system   <- genHost
    } yield {
      val address = AddressFromURIString(
        copyURI(uri)(scheme = protocol, host = system, port = -1, userInfo = null).toString
      )
      assert(address.hasLocalScope)
      address
    }

  def genGlobalAddress: Gen[Address] =
    for {
      uri      <- genAbsoluteURI
      protocol <- genScheme
      system   <- genUserInfo
      host     <- genHost
      port     <- genPort
    } yield {
      val address = AddressFromURIString(
        copyURI(uri)(scheme = protocol, userInfo = system, host = host, port = port).toString
      )
      assert(address.hasGlobalScope)
      address
    }

  def genAddress: Gen[Address] = Gen.choice1(genLocalAddress, genGlobalAddress)

  def genInvalidAddress: Gen[String] = Gen.choice1(
    genInvalidURI,
    genRelativeURI.map(_.toString),
    genURIWithoutHost.map(_.toString),
    genURIWithoutUserInfoWithPort.map(_.toString),
    genURIWithUserInfoWithoutPort.map(_.toString)
  )

  private def genURIWithoutHost: Gen[URI] =
    genURI.map(copyURI(_)(host = null))

  private def genURIWithoutUserInfoWithPort: Gen[URI] =
    for {
      uri  <- genURI
      port <- genPort
    } yield copyURI(uri)(userInfo = null, port = port)

  private def genURIWithUserInfoWithoutPort: Gen[URI] =
    for {
      uri      <- genURI
      userInfo <- genUserInfo
    } yield copyURI(uri)(userInfo = userInfo, port = 0)
}
// scalastyle:on null
