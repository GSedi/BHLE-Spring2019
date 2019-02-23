package TailRecursion

object Boot extends App {

  // Task 1
  sealed trait IntList {

    def length(cnt: Int = 0): Int = this match {
      case End => cnt
      case Node(head, tail) => tail.length(cnt + 1)
    }


    def product(acc: Int = 1): Int = this match {
      case End => acc
      case Node(head, tail) => tail.product(acc * head)
    }


    def double(intList: IntList = End): IntList = this match {
      case End => intList
      case Node(head, tail) => {
//        val newNode: IntList = Node(head * 2, intList)
//        tail.double(newNode)
        Node(head * 2, tail.double(intList))
//        intList.append(head * 2)
//        tail.double(intList)
      }
    }
  }
  case object End extends IntList
  case class Node(head: Int, tail: IntList) extends IntList

  val intList = Node(1, Node(2, Node(3, Node(4, End))))

  assert(intList.length() == 4)
  assert(intList.tail.length() == 3)
  assert(End.length() == 0)
  println(intList.length())


  // Task 2
  assert(intList.product() == 1 * 2 * 3 * 4)
  assert(intList.tail.product() == 2 * 3 * 4)
  assert(End.product() == 1)
  println(intList.product())

  // Task 3
  assert(intList.double() == Node(1 * 2, Node(2 * 2, Node(3 * 2, Node(4 * 2, End)))))
  assert(intList.tail.double() == Node(4, Node(6, Node(8, End))))
  assert(End.double() == End)

}
