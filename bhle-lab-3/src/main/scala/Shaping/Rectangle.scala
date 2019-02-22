package Shaping

object Rectangle{

  def rectangleFactory(width: Double, height: Double): Rectangle = {
    Rectangle(width, height)
  }

}


case class Rectangle(width: Double, height: Double) extends Rectangular with Shape {

  override def perimeter(): Double = 2 * (width + height)

  override def area(): Double = width * height

}