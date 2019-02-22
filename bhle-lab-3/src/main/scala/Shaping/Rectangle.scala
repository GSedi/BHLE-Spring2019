package Shaping

case class Rectangle(width: Double, height: Double) extends Shape{

  override def sides(): Int = 4

  override def perimeter(): Double = 2 * (width + height)

  override def area(): Double = width * height

}