package Shaping

object Boot extends App {

  println(Draw.apply(Circle(4)))
  println(Draw.apply(Rectangle(4, 5)))
  println(Draw.apply(Square(4)))
}
