/**
  * Created by abdhesh on 13/04/17.
  */
import cats.arrow.FunctionK
import freestyle._
import freestyle.implicits._

object algebras {

  @free
  trait SCtors1[F[_]] {
    def x(a: Int): FreeS[F, Int]
    def y(a: Int): FreeS[F, Int]
  }

  @free
  trait SCtors2[G[_]] {
    def i(a: Int): FreeS[G, Int]
    def j(a: Int): FreeS[G, Int]
  }

  @free
  trait SCtors3[H[_]] {
    def o(a: Int): FreeS[H, Int]
    def p(a: Int): FreeS[H, Int]
  }

  @free
  trait SCtors4[F[_]] {
    def k(a: Int): FreeS[F, Int]
    def m(a: Int): FreeS[F, Int]
  }

  @free
  trait MixedFreeS[F[_]] {
    def x: FreeS.Par[F, Int]
    def y: FreeS.Par[F, Int]
    def z: FreeS[F, Int]
  }

  @free
  trait S1[F[_]] {
    def x(n: Int): FreeS[F, Int]
  }

  @free
  trait S2[F[_]] {
    def y(n: Int): FreeS[F, Int]
  }

}

object modules {

  import algebras._

  @module
  trait M1[F[_]] {
    val sctors1: SCtors1[F]
    val sctors2: SCtors2[F]
  }

  @module
  trait M2[G[_]] {
    val sctors3: SCtors3[G]
    val sctors4: SCtors4[G]
  }

  @module
  trait O1[H[_]] {
    val m1: M1[H]
    val m2: M2[H]
  }

  @module
  trait O2[F[_]] {
    val o1: O1[F]
    val x = 1
    def y = 2
  }

  @module
  trait O3[F[_]] {
    def x = 1
    def y = 2
  }

  @module
  trait StateProp[F[_]] {
    val s1: S1[F]
    val s2: S2[F]
  }

}

object interps {

  import algebras._

  implicit val optionHandler1: FunctionK[SCtors1.Op, Option] = new SCtors1.Handler[Option] {
    def x(a: Int): Option[Int] = Some(a)
    def y(a: Int): Option[Int] = Some(a)
  }

  implicit val listHandler1: FunctionK[SCtors1.Op, List] = new SCtors1.Handler[List] {
    def x(a: Int): List[Int] = List(a)
    def y(a: Int): List[Int] = List(a)
  }

  implicit val optionHandler2: FunctionK[SCtors2.Op, Option] = new SCtors2.Handler[Option] {
    def i(a: Int): Option[Int] = Some(a)
    def j(a: Int): Option[Int] = Some(a)
  }

  implicit val listHandler2: FunctionK[SCtors2.Op, List] = new SCtors2.Handler[List] {
    def i(a: Int): List[Int] = List(a)
    def j(a: Int): List[Int] = List(a)
  }

  implicit val optionHandler3: FunctionK[SCtors3.Op, Option] = new SCtors3.Handler[Option] {
    def o(a: Int): Option[Int] = Some(a)
    def p(a: Int): Option[Int] = Some(a)
  }

  implicit val listHandler3: FunctionK[SCtors3.Op, List] = new SCtors3.Handler[List] {
    def o(a: Int): List[Int] = List(a)
    def p(a: Int): List[Int] = List(a)
  }

  implicit val optionHandler4: FunctionK[SCtors4.Op, Option] = new SCtors4.Handler[Option] {
    def k(a: Int): Option[Int] = Some(a)
    def m(a: Int): Option[Int] = Some(a)
  }

  implicit val listHandler4: FunctionK[SCtors4.Op, List] = new SCtors4.Handler[List] {
    def k(a: Int): List[Int] = List(a)
    def m(a: Int): List[Int] = List(a)
  }

}