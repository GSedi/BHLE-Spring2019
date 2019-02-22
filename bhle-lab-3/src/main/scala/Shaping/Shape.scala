package Shaping

//sealed trait Shape {
trait Shape {
  def sides(): Int

  def perimeter(): Double

  def area(): Double
}
