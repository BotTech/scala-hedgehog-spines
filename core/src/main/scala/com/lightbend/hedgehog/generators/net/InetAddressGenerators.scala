package com.lightbend.hedgehog.generators.net

import java.net.InetAddress

import hedgehog.{Gen, Range}

object InetAddressGenerators {

  private val Inet4AddressSize = 4
  private val Inet6AddressSize = 16

  def genRawInet4Address: Gen[InetAddress] =
    genRawInetAddressFrom(Inet4AddressSize)

  def genRawInet6Address: Gen[InetAddress] =
    genRawInetAddressFrom(Inet6AddressSize)

  def genRawInetAddress: Gen[InetAddress] =
    Gen.choice1(
      genRawInet4Address,
      genRawInet6Address
    )

  private def genRawInetAddressFrom(size: Int): Gen[InetAddress] =
    Gen.bytes(Range.singleton(size)).map(InetAddress.getByAddress)
}
