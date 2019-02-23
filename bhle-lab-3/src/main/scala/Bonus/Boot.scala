package Bonus

object Boot extends App {

  // Bonus Task 1
  // Task 1
  sealed trait IntList {

    def map(f: Int => Int): IntList = this match {
      case End => End
      case Node(head, tail) => {
        Node(f(head), tail.map(f))
      }
    }


  }
  case object End extends IntList
  case class Node(head: Int, tail: IntList = End) extends IntList

  val intList = Node(1, Node(2, Node(3, Node(4, End))))

  assert(intList.map(x => x * 3) == Node(1 * 3, Node(2 * 3, Node(3 * 3, Node(4 * 3, End)))))
  assert(intList.map(x => 5 - x) == Node(5 - 1, Node(5 - 2, Node(5 - 3, Node(5 - 4, End)))))

//  def printList(intList: IntList): Unit = intList match {
//    case End => println()
//    case Node(head, tail) =>
//      print(s"$head ")
//      printList(tail)
//  }
//  printList(asd)


  sealed trait GenericList[A] {

    def length(cnt: Int = 0): Int = this match {
      case GenericEnd() => cnt
      case GenericNode(head, tail) => tail.length(cnt + 1)
    }

    def map[B](f: A => B): GenericList[B] = this match {
      case GenericEnd() => GenericEnd()
      case GenericNode(head, tail) => {
        GenericNode(f(head), tail.map(f))
      }

    }
  }

  case class GenericEnd[T]() extends GenericList[T]
  case class GenericNode[T](head: T, tail: GenericList[T]) extends GenericList[T]


  val genericList: GenericList[Int] = GenericNode(1, GenericNode(2, GenericNode(3, GenericEnd())))

  assert(genericList.map(x => x + 8) == GenericNode(1 + 8, GenericNode(2 + 8, GenericNode(3 + 8, GenericEnd()))))
  assert(genericList.map(x => x.toString) == GenericNode("1", GenericNode("2", GenericNode("3", GenericEnd()))))

}
