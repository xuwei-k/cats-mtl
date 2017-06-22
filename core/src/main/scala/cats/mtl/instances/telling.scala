package cats
package mtl
package instances

import cats.data.WriterT
import cats.mtl.applicative.Telling

trait TellingInstances extends TellingInstancesLowPriority1 {
  implicit final def tellingInd[M[_], Inner[_], L](implicit
                                                lift: applicative.Layer[M, Inner],
                                                under: Telling[Inner, L]
                                               ): Telling[M, L] = {
    new Telling[M, L] {
      val applicative: Applicative[M] = lift.outerInstance
      val monoid: Monoid[L] = under.monoid

      def tell(l: L): M[Unit] = lift.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = lift.layer(under.writer(a, l))

      def tuple[A](ta: (L, A)): M[A] = lift.layer(under.tuple(ta))
    }
  }
}

private[instances] trait TellingInstancesLowPriority1 {
  implicit final def tellingWriter[M[_], L](implicit L: Monoid[L], M: Applicative[M]): Telling[CurryT[WriterTCL[L]#l, M]#l, L] = {
    new Telling[CurryT[WriterTCL[L]#l, M]#l, L] {
      val applicative = WriterT.catsDataApplicativeForWriterT(M, L)
      val monoid: Monoid[L] = L

      def tell(l: L): WriterT[M, L, Unit] = WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] = WriterT.put(a)(l)

      def tuple[A](ta: (L, A)): WriterT[M, L, A] = WriterT(M.pure(ta))
    }
  }

  implicit final def tellingTuple[L](implicit L: Monoid[L]): Telling[TupleC[L]#l, L] = {
    new Telling[TupleC[L]#l, L] {
      val applicative = cats.instances.tuple.catsStdMonadForTuple2
      val monoid: Monoid[L] = L

      def tell(l: L): (L, Unit) = (l, ())

      def writer[A](a: A, l: L): (L, A) = (l, a)

      def tuple[A](ta: (L, A)): (L, A) = ta
    }
  }
}

object telling extends TellingInstances
