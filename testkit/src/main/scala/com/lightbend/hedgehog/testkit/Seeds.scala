package com.lightbend.hedgehog.testkit

import hedgehog.core.Seed

trait Seeds {

  def fromSeed: Seed

  def toSeed: Seed

  def minSeed: Seed

  def maxSeed: Seed
}
