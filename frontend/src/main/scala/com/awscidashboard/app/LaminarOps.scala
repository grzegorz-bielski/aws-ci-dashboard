package com.awscidashboard.app

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.keys.CompositeKey
import com.raquo.laminar.nodes.ReactiveElement

object LaminarOps:
  extension [K, E <: ReactiveElement.Base](ck: CompositeKey[K, E])
    def :?=(value: Option[String]): Setter[E] = ck(value.toSeq*)
    def :?!=[T](value: Option[T], onNone: => String): Setter[E] =
        :?= (value.fold(Some(onNone))(_ => None))