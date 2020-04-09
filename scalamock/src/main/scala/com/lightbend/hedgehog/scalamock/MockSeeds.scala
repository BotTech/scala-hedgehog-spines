package com.lightbend.hedgehog.scalamock

import com.lightbend.hedgehog.testkit.Seeds
import hedgehog.core.Seed
import org.scalamock.MockFactoryBase

trait MockSeeds extends Seeds {
  this: MockFactoryBase =>

  @SuppressWarnings(
    Array("org.wartremover.warts.Any", "org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Null")
  )
  def fromSeed: Seed = {
    val seed = stub[Seed]
    (seed.chooseLong _).when(*, *).onCall((from, _) => (seed, from))
    (seed.chooseDouble _).when(*, *).onCall((from, _) => (seed, from))
    seed
  }

  @SuppressWarnings(
    Array("org.wartremover.warts.Any", "org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Null")
  )
  def toSeed: Seed = {
    val seed = stub[Seed]
    (seed.chooseLong _).when(*, *).onCall((_, to) => (seed, to))
    (seed.chooseDouble _).when(*, *).onCall((_, to) => (seed, to))
    seed
  }

  @SuppressWarnings(
    Array("org.wartremover.warts.Any", "org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Null")
  )
  def minSeed: Seed = {
    val seed = stub[Seed]
    (seed.chooseLong _).when(*, *).onCall((from, to) => (seed, math.min(from, to)))
    (seed.chooseDouble _).when(*, *).onCall((from, to) => (seed, math.min(from, to)))
    seed
  }

  @SuppressWarnings(
    Array("org.wartremover.warts.Any", "org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Null")
  )
  def maxSeed: Seed = {
    val seed = stub[Seed]
    (seed.chooseLong _).when(*, *).onCall((from, to) => (seed, math.max(from, to)))
    (seed.chooseDouble _).when(*, *).onCall((from, to) => (seed, math.max(from, to)))
    seed
  }
}
