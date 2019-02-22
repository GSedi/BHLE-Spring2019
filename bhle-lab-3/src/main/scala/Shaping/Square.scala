package Shaping


case class Square(side: Double) extends Shape{

  override def sides(): Int = 4

  override def perimeter(): Double = 4 * side

  override def area(): Double = math.pow(side, 2)

}
