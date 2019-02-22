package AlgebraicDT

object Boot extends App {

//  case class IntCalculator(a: Int, b: Int){
//    def sum: Int = a + b
//    def product: Int = a * b
//    def differ: Int = a - b
//    def div: Int = a / b
//  }

  // Task 1
  sealed trait IntCalculator
  case class Sum(a: Int, b: Int) extends IntCalculator
  case class Subtract(a: Int, b: Int) extends IntCalculator
  case class Product(a: Int, b: Int) extends IntCalculator
  case class Divide(a: Int, b: Int) extends IntCalculator

  case object Calculator {

    def evaluate(operation: IntCalculator) = operation match {
      case Sum(a, b) => a + b
      case Subtract(a, b) => a - b
      case Product(a, b) => a * b
      case Divide(a, b) => a / b
    }

  }

}
