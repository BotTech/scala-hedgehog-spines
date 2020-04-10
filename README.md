# Scala Hedgehog Spines

This project contains customisations and extensions to [Scala Hedgehog][].

## Setup

```sbt
libraryDependencies ++= Seq(
  "com.lightbend.hedgehog" %% "spines-akka"      % "0.1.0",
  "com.lightbend.hedgehog" %% "spines-akka-http" % "0.1.0",
  "com.lightbend.hedgehog" %% "spines-cats"      % "0.1.0",
  "com.lightbend.hedgehog" %% "spines-core"      % "0.1.0",
  "com.lightbend.hedgehog" %% "spines-macros"    % "0.1.0",
  "com.lightbend.hedgehog" %% "spines-runner"    % "0.1.0",
  "com.lightbend.hedgehog" %% "spines-sbt"       % "0.1.0",
  "com.lightbend.hedgehog" %% "spines-scalamock" % "0.1.0",
  "com.lightbend.hedgehog" %% "spines-testkit"   % "0.1.0"
)
```

## Runners

The built in runner in Hedgehog is very primitive ([their words] not ours).

If you run `sbt test` then this will use the built in runner. To use one of ours you must either run
the main class directly or configure the custom [SBT Framework][].

We have custom runners for adding additional functionality:
- [TestRunner][]
- [TestRunnerMock][]

These runners use the [Stackable trait pattern][] and so there are various mixins available and it
is easy to add your own.

Here is an example of using the custom test runner:
```sbtshell
server/Test/runMain com.example.FooSpec --seed 52720261678364 --name "genFoo generates foos"
```

### TestRunner

```text
Usage: FooSpec [options]

  -n, --name <value>  Only runs tests with the given name. Specify multiple times to run multiple tests.
  -s, --seed <value>  Sets the seed value to use.
  -h, --help
```

The `TestRunner` will print out the seed at the start of a test run:
```sbtshell
[info] Running tests (seed = 52720261678364):
```

You can use value with the `--seed` option to reproduce test runs.

### TestRunnerMock

This is an extension to `TestRunner` which adds [ScalaMock][] support.

### TestRunnerMock

This is an extension to `TestRunner` which adds [ScalaMock][] support.

### BeforeAndAfter

There are default mixins for performing setup before and after
tests:
- [BeforeAndAfterAll][] - This runs the methods before and after all tests in a single runner.
- [BeforeAndAfterEach][] - This runs the methods before and after each test run, including reruns
  during shrinking.

## SBT Framework

The default SBT Test Framework for Hedgehog does not run the tests in the same way that the runners
do and so the additional functionality from our custom runners is not invoked. To workaround this we
have our own [HedgehogFramework][].

You need to ensure that SBT is configured to use this:
```sbt
testFrameworks := Seq(TestFramework("com.lightbend.hedgehog.sbt.HedgehogFramework"))
```

`hedgehog-sbt` must also be on the classpath.

> ⚠️ There is no error if the test framework is missing from the classpath. The tests just won't run.

## Generators

There are a large number of generators in the [generators][] packages.

If a generator for a common type that you require is missing then feel free to submit a pull request
to add it.

## Implicits

There are a bunch of implicits for extending the test syntax. Take a look at the [syntax tests][]
for what you can use.

### Failures

It is very important that exceptions are not thrown within your tests. If they do then none of the
values will be logged and so reproducing the failure becomes very difficult.

For `Try` you can use [TryImplicits][].

`Future` is particularly troublesome as you also need to await for it to complete.
[FutureImplicits][] make this easy. You must also provide a `Duration` for the maximum time to await
completion. Either mixin the `FutureImplicits` trait and use the implicit `DurationConversions` or
import `FutureImplicits._` and `scala.concurrent.duration._`.

Getting the time to wait for a `Future` to complete can be tricky and making it too small leads to
flaky tests. To aid with knowing the difference between a test that took a little bit too long and
one which was never going to complete, you can add the following VM option:
```text
-Dhedgehog.future.diagnosisDuration=5min
```

The valid time units are:
| Unit         | Labels                 |
|--------------|------------------------|
| Days         | `d day`                |
| Hours        | `h hour`               |
| Minutes      | `min minute`           |
| Seconds      | `s sec second`         |
| Milliseconds | `ms milli millisecond` |
| Microseconds | `µs micro microsecond` |
| Nanoseconds  | `ns nano nanosecond`   |

```text
  private[this] val timeUnitLabels = List(
  )
```

This property will cause the test to try and wait the additional duration provided if a `Future` is
not yet ready within the timeout specified in the test. If the `Future` does eventually complete
within the additional diagnosis duration it will then suggest what the test timeout should be
increased to. For example:
```text
> Future was not ready within 5 seconds.
> Future eventually completed after waiting an additional 100 milliseconds.
> Consider increasing the original wait time to at least 105 milliseconds.
```

## TestKit

One thing about generators is that they are fairly easy to compose into more complex generators
however they can have surprising behaviour and not generate the values that you think they will.
Using a buggy generator in your tests is going to ruin your day as it takes a long time to track
down if you are even lucky enough to notice it.

To that end, there is a custom TestKit for testing the generators themselves. In fact, all the
generators in this project require complete coverage. Fortunately the TestKit makes testing the
generators pretty straight forward. Take a look at some examples of [generator tests][].

### Probabilities

Often you will compose more complex generators from other generators using built in combinators such
as `Gen.choice1` and `Gen.frequency1`. In order to make sure that your generator is fair and
generates the correct proportions of sets of values you can use [Probabilities][].

These probability values are calculated from the binomial confidence intervals using a 99.99999%
confidence level and 10,000 iterations. Once the confidence interval has been calculated we take the
lower bound and use that as the percentage. Use this [calculator][] to work out the confidence
interval.

Example:
| Confidence Level | `99.99999` | Click compute |
| Numerator (x) | `10000 / 3 * 2 = 6666` | Round down |
| Denominator (N) | `10000` | Click compute
| Proportion (x/N) | `0.6666` | This what we expect if is a binomial distribution |
| Exact Confidence Interval around Proportion | `0.6411` to `0.6914` | This what we expect if is a
binomial distribution |

Therefore if we have a generator which generates values using another generator at a ratio of two to
one then if we see a proportion of at least `0.6411` we can be 99.99999% certain that the true
proportion is indeed `0.6666`.

See [ExceptionGeneratorsSpec][] for and example of how these proportions are used.

[beforeandafterall]: runner/src/main/scala/com/lightbend/hedgehog/runner/BeforeAndAfterAll.scala
[beforeandaftereach]: runner/src/main/scala/com/lightbend/hedgehog/runner/BeforeAndAfterEach.scala
[exceptiongeneratorsspec]: tests/src/test/scala/com/lightbend/hedgehog/generators/ExceptionGeneratorsSpec.scala
[futureimplicits]: core/src/main/scala/com/lightbend/hedgehog/implicits/FutureImplicits.scala
[calculator]: https://statpages.info/confint.html
[generators]: core/src/main/scala/com/lightbend/hedgehog/generators
[generator tests]: tests/src/test/scala/com/lightbend/hedgehog/generators/CharGeneratorsSpec.scala
[hedgehogframework]: sbt/src/main/scala/com/lightbend/hedgehog/sbt/HedgehogFramework.scala 
[probabilities]: testkit/src/main/scala/com/lightbend/hedgehog/testkit/Probabilities.scala
[sbt framework]: #sbt-framework
[scala hedgehog]: https://github.com/hedgehogqa/scala-hedgehog
[scalamock]: https://scalamock.org/
[stackable trait pattern]: https://www.artima.com/scalazine/articles/stackable_trait_pattern.html
[syntax tests]: tests/src/test/scala/com/lightbend/hedgehog/implicits
[test framework argument]: https://www.scala-sbt.org/1.x/docs/Testing.html#Test+Framework+Arguments
[testrunner]: runner/src/main/scala/com/lightbend/hedgehog/runner/TestRunner.scala
[testrunnermock]: scalamock/src/main/scala/com/lightbend/hedgehog/scalamock/TestRunnerMock.scala
[tryimplicits]: core/src/main/scala/com/lightbend/hedgehog/implicits/TryImplicits.scala
[their words]: https://github.com/hedgehogqa/scala-hedgehog#sbt-testing
