package com.awscidashboard.app

import scala.util.chaining.given

extension [A](a: A) def |>[B](fn: A => B): B = a pipe fn
