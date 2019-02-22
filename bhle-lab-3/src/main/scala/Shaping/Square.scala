package Shaping


object Square {

  def squareFactory(side: Double): Square = {
    Square(side)
  }

}

case class Square(side: Double) extends Rectangular with Shape {

  override def perimeter(): Double = 4 * side

  override def area(): Double = math.pow(side, 2)

}
