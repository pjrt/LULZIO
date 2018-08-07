package org.supersrsFP.lulzio


import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._
import scala.concurrent.Await

import IOBenchmarks._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class IODeepFlatMapBenchmark {
  @Param(Array("20"))
  var depth: Int = _

  @Benchmark
  def thunkDeepFlatMap(): BigInt = {
    def fib(n: Int): Thunk[BigInt] =
      if (n <= 1) Thunk(n)
      else
        fib(n - 1).flatMap { a =>
          fib(n - 2).flatMap(b => Thunk(a + b))
        }

    fib(depth).unsafeRun()
  }

  @Benchmark
  def futureDeepFlatMap(): BigInt = {
    import scala.concurrent.Future
    import scala.concurrent.duration.Duration.Inf

    def fib(n: Int): Future[BigInt] =
      if (n <= 1) Future(n)
      else
        fib(n - 1).flatMap { a =>
          fib(n - 2).flatMap(b => Future(a + b))
        }

    Await.result(fib(depth), Inf)
  }

  @Benchmark
  def monixDeepFlatMap(): BigInt = {
    import monix.eval.Task

    def fib(n: Int): Task[BigInt] =
      if (n <= 1) Task.eval(n)
      else
        fib(n - 1).flatMap { a =>
          fib(n - 2).flatMap(b => Task.eval(a + b))
        }

    fib(depth).runSyncMaybe.right.get
  }

  @Benchmark
  def scalazDeepFlatMap(): BigInt = {
    import scalaz.zio._

    def fib(n: Int): IO[Nothing, BigInt] =
      if (n <= 1) IO.point[BigInt](n)
      else
        fib(n - 1).flatMap { a =>
          fib(n - 2).flatMap(b => IO.point(a + b))
        }

    unsafeRun(fib(depth))
  }

  @Benchmark
  def catsDeepFlatMap(): BigInt = {
    import cats.effect._

    def fib(n: Int): IO[BigInt] =
      if (n <= 1) IO(n)
      else
        fib(n - 1).flatMap { a =>
          fib(n - 2).flatMap(b => IO(a + b))
        }

    fib(depth).unsafeRunSync
  }

  @Benchmark
  def lulzDeepFlatMap(): BigInt = {

    def fib(n: Int): LULZIO[BigInt] =
      if (n <= 1) LULZIO(n)
      else
        fib(n - 1).flatMap { a =>
          fib(n - 2).flatMap(b => LULZIO(a + b))
        }

    fib(depth).unsafeRunSync()
  }

  @Benchmark
  def javaDeepFlatMap(): BigInt = {

    def fib(n: Int): JIO[BigInt] =
      if (n <= 1) JIO.pure(n)
      else
        fib(n - 1).flatMap { a =>
          fib(n - 2).flatMap(b => SJIO(a + b))
        }

    fib(depth).unsafeRunSync()
  }
}
