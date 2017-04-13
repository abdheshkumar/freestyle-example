/**
  * Created by abdhesh on 13/04/17.
  */

import cats.data.Kleisli
import cats.implicits._
import monix.eval.Task
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import freestyle.nondeterminism._
import freestyle.implicits._
import freestyle._
import monix.eval.Task.nondeterminism



class parallelTests extends WordSpec with Matchers {

  "Applicative Parallel Support" should {

    import algebras._

    class NonDeterminismTestShared {



      val buf = scala.collection.mutable.ArrayBuffer.empty[Int]

      def blocker(value: Int, waitTime: Long): Int = {
        Thread.sleep(waitTime)
        buf += value
        value
      }

      val v = MixedFreeS[MixedFreeS.Op]

      import v._

      val program = for {
        a <- z //3
        bc <- (x |@| y).tupled.freeS //(1,2)
        (b, c) = bc
        d <- z //3
      } yield a :: b :: c :: d :: Nil // List(3,1,2,3)

    }

    "allow non deterministic execution when interpreting to scala.concurrent.Future" in {

      import scala.concurrent.ExecutionContext.Implicits.global
      import freestyle.nondeterminism._
      import freestyle.implicits._
      val test = new NonDeterminismTestShared
      import test._

      implicit val interpreter = new MixedFreeS.Handler[Future] {
          def x: Future[Int] = Future(blocker(1, 1000L))

          def y: Future[Int] = Future(blocker(2, 0L))

          def z: Future[Int] = Future(blocker(3, 2000L))
      }

      Await.result(program.exec[Future], Duration.Inf) shouldBe List(3, 1, 2, 3)
      buf.toArray shouldBe Array(3, 2, 1, 3)
    }

    "allow non deterministic execution when interpreting to monix.eval.Task" in {
      import freestyle.nondeterminism._
      import freestyle.implicits._


      import monix.cats._
      import monix.eval.Task.nondeterminism
      import monix.execution.Scheduler.Implicits.global


      val test = new NonDeterminismTestShared
      import test._

      implicit val interpreter = new MixedFreeS.Handler[Task] {
          def x: Task[Int] = Task(blocker(1, 1000L))

          def y: Task[Int] = Task(blocker(2, 0L))

          def z: Task[Int] = Task(blocker(3, 2000L))
      }

      Await.result(program.exec[Task].runAsync, Duration.Inf) shouldBe List(3, 1, 2, 3)
      buf.toArray shouldBe Array(3, 2, 1, 3)
    }

    "allow deterministic programs with FreeS.Par nodes run deterministically" in {

      val test = new NonDeterminismTestShared
      import test._

      implicit val interpreter = new MixedFreeS.Handler[Option] {
          def x: Option[Int] = Option(blocker(1, 1000L))

          def y: Option[Int] = Option(blocker(2, 0L))

          def z: Option[Int] = Option(blocker(3, 2000L))
      }

      program.exec[Option] shouldBe Option(List(3, 1, 2, 3))
      buf.toArray shouldBe Array(3, 1, 2, 3)
    }

    /**
      * Similar example as the one found at
      * http://typelevel.org/cats/datatypes/freeapplicative.html
      */
    "allow validation style algebras derived from FreeS.Par" in {
     type ParValidator[A] = Kleisli[Future, String, A]
      import monix.execution.Scheduler.Implicits.global
      @free
      trait Validation[F[_]] {
        def minSize(n: Int): FreeS.Par[F, Boolean]

        def hasNumber: FreeS.Par[F, Boolean]
      }

      implicit val interpreter = new Validation.Handler[ParValidator] {
          def minSize(n: Int): ParValidator[Boolean] =
          Kleisli(s => Future(s.size >= n))

          def hasNumber: ParValidator[Boolean] =
          Kleisli(s => Future(s.exists(c => "0123456789".contains(c))))
      }

      val validation = Validation[Validation.Op]
      import validation._

      val parValidation = (minSize(3) |@| hasNumber).map(_ :: _ :: Nil)
      val validator = parValidation.exec[ParValidator]

      Await.result(validator.run("a"), Duration.Inf) shouldBe List(false, false)
      Await.result(validator.run("abc"), Duration.Inf) shouldBe List(true, false)
      Await.result(validator.run("abc1"), Duration.Inf) shouldBe List(true, true)
      Await.result(validator.run("1a"), Duration.Inf) shouldBe List(false, true)
    }
  }

}