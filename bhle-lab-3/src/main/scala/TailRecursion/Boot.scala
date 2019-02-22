package TailRecursion

object Boot extends App {

  // Task 1
  sealed trait IntList {

    def length(cnt: Int = 0): Int = this match {
      case End => cnt
      case Node(head, tail) => tail.length(cnt + 1)
    }
  }
  case object End extends IntList
  case class Node(head: Int, tail: IntList) extends IntList

  val intList = Node(1, Node(2, Node(3, Node(4, End))))

  assert(intList.length() == 4)
  assert(intList.tail.length() == 3)
  assert(End.length() == 0)
  println(intList.length())
}
