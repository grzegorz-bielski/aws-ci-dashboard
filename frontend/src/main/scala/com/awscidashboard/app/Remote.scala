package com.awscidashboard.app

enum Remote[+A]:
  case Initial 
  case Pending 
  case Failure(e: Throwable)
  case Success(a: A)

  def flatMap[B >: A](fn: A => Remote[B]): Remote[B] = 
    this match
      case Success(a) => fn(a)
      case _          => this.asInstanceOf[Remote[B]]

  def map[B >: A](fn: A => B): Remote[B] = 
    flatMap(fn.andThen(Success(_)))

end Remote
