package Shaping

trait Rectangular {

  this: Shape =>

  override def sides(): Int = 4

}
