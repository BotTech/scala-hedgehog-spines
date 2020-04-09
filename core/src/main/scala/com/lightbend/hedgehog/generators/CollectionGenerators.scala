package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.core.SeedGenerators._
import hedgehog._
import hedgehog.core.{GenT, Seed, Tree}
import hedgehog.predef.Identity

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.collection.{mutable, BuildFrom, Factory}
import scala.reflect.ClassTag
import org.scalactic.TripleEquals._

object CollectionGenerators {

  def genArray[A: ClassTag](gen: Gen[A], range: Range[Int]): Gen[Array[A]] =
    genList(gen, range).map(_.toArray)

  def genList[A](gen: Gen[A], range: Range[Int]): Gen[List[A]] =
    genCollection(gen, range)

  // FIXME: The type inference doesn't work properly here.
  def genCollection[A, CC[B] <: IterableOnce[B]](
      gen: Gen[A],
      range: Range[Int]
    )(implicit f: Factory[A, CC[A]]
    ): Gen[CC[A]] =
    genUnfolded(gen, range)(_.addOne(_), { case (n, _) => n - 1 })

  // FIXME: The type inference doesn't work properly here.
  def genJoined[A, CA[B] <: Iterable[B], CC[C] <: IterableOnce[C]](
      gen: Gen[CA[A]],
      range: Range[Int]
    )(implicit f: Factory[A, CC[A]]
    ): Gen[CC[A]] =
    // The size of the resulting collection may not be exactly within the range but we can't risk chopping up the
    // generated values as that could produce an incorrect result.
    genUnfolded(gen, range)(_.addAll(_), _ - _.size)

  private def genUnfolded[A, B, CC[C] <: IterableOnce[C]](
      gen: Gen[B],
      range: Range[Int]
    )(
      add: (mutable.Builder[A, CC[A]], B) => mutable.Builder[A, CC[A]],
      countDown: (Int, B) => Int
    )(implicit factory: Factory[A, CC[A]]
    ): Gen[CC[A]] = {
    type TreeB = Tree[(Seed, Option[B])]
    type TreeC = Tree[(Seed, Option[CC[A]])]
    def buildTree(seed: Seed, value: Option[CC[A]]): TreeC =
      Tree.TreeApplicative.point(seed -> value)
    @tailrec
    def unfold(z: Seed)(n: Int, builder: mutable.Builder[A, CC[A]], f: Seed => TreeB): TreeC =
      if (n <= 0) buildTree(z, Some(builder.result()))
      else {
        val tree        = f(z)
        val (s, maybeB) = tree.value
        maybeB match {
          case Some(b) =>
            val nextN = countDown(n, b)
            // This is probably going to lead to lots of discards.
            // Fix the passed in generator so that it doesn't produce empty values.
            if (n === nextN) buildTree(z, None)
            else unfold(s)(nextN, add(builder, b), f)
          case None => buildTree(z, None)
        }
      }

    Gen.int(range).flatMap { n =>
      GenT {
        case (size, seed0) => unfold(seed0)(n, factory.newBuilder, gen.run(size, _))
      }
    }
  }

  def genShuffled[A, CC[B] <: IterableOnce[B]](gen: Gen[CC[A]])(implicit bf: BuildFrom[CC[A], A, CC[A]]): Gen[CC[A]] =
    for {
      seed <- genSeed
      xs   <- gen
    } yield shuffle(seed, xs)

  // This is a direct translation from scala.util.Random.shuffle
  private def shuffle[A, CC[B] <: IterableOnce[B]](
      seed: Seed,
      xs: CC[A]
    )(implicit bf: BuildFrom[CC[A], A, CC[A]]
    ): CC[A] = {
    // Beware the 🐉
    val buf = new ArrayBuffer[A] ++= xs

    def swap(i1: Int, i2: Int): Unit = {
      val tmp = buf(i1)
      buf(i1) = buf(i2)
      buf(i2) = tmp
    }

    @tailrec
    def loop(it: Iterable[Int], seed: Seed): Unit =
      it.headOption match {
        case Some(n) =>
          val (nextSeed, k) = seed.chooseLong(0, n.toLong - 1)
          swap(n - 1, k.toInt)
          loop(it.drop(1), nextSeed)
        case None => ()
      }
    loop(buf.length to 2 by -1, seed)

    (bf.newBuilder(xs) ++= buf).result()
  }

  // FIXME: The type inference doesn't work properly here.
  def genPerturbed[A, CA <: Iterable[A], CB <: Iterable[Int], CC[D] <: IterableOnce[D]](
      gen: Gen[CC[CA]],
      genPerturbations: Gen[CB]
    )(implicit caFactory: Factory[A, CA],
      ccFactory: Factory[CA, CC[CA]]
    ): Gen[CC[CA]] =
    for {
      xs            <- gen
      perturbations <- genPerturbations
    } yield perturb[A, CA, CC](xs, perturbations)

  /**
    * If you have trouble with the type inference it is because [[Gen]] is invariant and so the type checker will not go
    * searching for the LUB regardless of whether the collection itself is covariant in `A`. The solution is to make
    * sure that `gen` has the correct type first. You may want to use
    * [[com.lightbend.hedgehog.Implicits.GeneratorSyntax.widen]].
    */
  def genPerturbedN[A, CA[B] <: Iterable[B], CB <: Iterable[Int], CC[D] <: IterableOnce[D]](
      gen: Gen[CC[CA[A]]],
      genPerturbations: Gen[CB]
    )(implicit caFactory: Factory[A, CA[A]],
      ccFactory: Factory[CA[A], CC[CA[A]]]
    ): Gen[CC[CA[A]]] =
    for {
      xs            <- gen
      perturbations <- genPerturbations
    } yield perturb[A, CA[A], CC](xs, perturbations)

  private def perturb[A, CA <: Iterable[A], CC[D] <: IterableOnce[D]](
      xs: CC[CA],
      perturbations: IterableOnce[Int]
    )(implicit caFactory: Factory[A, CA],
      ccFactory: Factory[CA, CC[CA]]
    ): CC[CA] = {
    // Beware the 🐉
    val it: Iterator[CA]        = xs.iterator
    val perturbs: Iterator[Int] = perturbations.iterator

    def perturbNext(next: CA, nextBuilder: mutable.Builder[A, CA]): CA =
      if (perturbs.hasNext) {
        // This could be expensive.
        val nextSize   = next.size
        val resultSize = nextSize + perturbs.next()
        take(resultSize, next, it, nextBuilder)
      } else next

    @tailrec
    def loop(nextBuilder: mutable.Builder[A, CA], builder: mutable.Builder[CA, CC[CA]]): CC[CA] =
      if (it.isEmpty) {
        val last = nextBuilder.result()
        if (last.isEmpty) builder.result()
        else builder.addOne(last).result()
      } else {
        val next = {
          val withRemainder = nextBuilder.addAll(it.next()).result()
          nextBuilder.clear()
          withRemainder
        }
        val result = perturbNext(next, nextBuilder)
        loop(nextBuilder, builder.addOne(result))
      }

    loop(caFactory.newBuilder, ccFactory.newBuilder)
  }

  private def take[A, CA <: Iterable[A]](n: Int, next: CA, more: Iterator[CA], builder: mutable.Builder[A, CA]): CA = {
    @tailrec
    def loop(n: Int, next: CA, builder: mutable.Builder[A, CA]): CA = {
      // This could be expensive. We might already know the size.
      val nextSize = next.size
      if (n <= nextSize || more.isEmpty) {
        val (head, tail) = next.splitAt(n)
        val result       = builder.addAll(head).result()
        builder.clear()
        val _ = builder.addAll(tail)
        result
      } else loop(n - nextSize, more.next(), builder.addAll(next))
    }

    loop(n, next, builder)
  }

  // TODO: Generate finite lazy lists that are not determined by the result of the generator.
  def genLazyList[A](gen: Gen[A]): Gen[LazyList[A]] = {
    def unfold[S, B](z: S)(f: S => Tree[(S, Option[B])]): LazyList[B] = {
      val tree        = f(z)
      val (s, maybeB) = tree.value
      maybeB match {
        case Some(h) => h #:: unfold(s)(f)
        case None    => LazyList.empty
      }
    }
    def lift(seed: Seed)(l: LazyList[A]): Tree[(Seed, Option[LazyList[A]])] =
      Tree((seed, Some(l)), Identity(hedgehog.predef.LazyList()))
    GenT[LazyList[A]] {
      case (size, seed0) =>
        lift(seed0)(unfold(seed0)(seed => gen.run(size, seed)))
    }
  }
}
