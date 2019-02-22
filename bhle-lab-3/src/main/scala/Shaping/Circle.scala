package Shaping

object Circle {

  def circleFactory(radius: Double): Circle = {
    Circle(radius)
  }

}

case class Circle(radius: Double) extends Shape{

  override def sides(): Int = 0

  override def perimeter(): Double = 2 * math.Pi * radius

  override def area(): Double = math.Pi * math.pow(radius, 2)

}
