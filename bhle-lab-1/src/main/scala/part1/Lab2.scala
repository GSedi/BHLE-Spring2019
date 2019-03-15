package part1

// Theoretical questions: why do we need abstraction
  /*
      At a higher level, Abstraction is a process of
      hiding the implementation details and showing only functionality to the user.
   */
// How `traits` in Scala are used?
  /*
    Interface + Abstract
   */


trait Animal {
  // Is this abstract or concrete (implemented) member?
  def name: String

  // Is this abstract or concrete (implemented) member?
  def makeSound(): String
}

trait Walks {

  // What does this line mean?
  this: Animal =>

  // Is this abstract or concrete (implemented) member?
  // Why `name` parameter is available here?
  /*
    Mixses two trait, to make available methods in extendend class
    without importing it
   */
  def walk: String = s"$name is walking"

}


// Can Dog only extend from `Walks`?
// Try to fix Dog, so it extends proper traits
// Implement Dog class so it passes tests
case class Dog(dogName:String) extends Animal with Walks{
  override def makeSound(): String = "Whooof"

  override def name: String = dogName
  override def walk: String = super.walk
}

// Implement Cat class so it passes tests
case class Cat(catName: String) extends Animal with Walks{
  override def makeSound(): String = "Miiyaaau"

  override def name: String = catName
  override def walk: String = super.walk
}

object Lab2 extends App {

  // Here we will test Dog and Cat classes

  val dog1 = Dog("Ceasar")
  val dog2 = Dog("Laika")

  assert(dog1.name == "Ceasar")
  assert(dog2.name == "Laika")

  assert(dog1.makeSound() == "Whooof")
  assert(dog2.makeSound() == "Whooof")

  assert(dog1.walk == "Ceasar is walking")
  assert(dog2.walk == "Laika is walking")

  val cat1 = Cat("Tosha")
  val cat2 = Cat("Chocolate")

  assert(cat1.name == "Tosha")
  assert(cat2.name == "Chocolate")

  assert(cat1.makeSound() == "Miiyaaau")
  assert(cat2.makeSound() == "Miiyaaau")

  assert(cat1.walk == "Tosha is walking")
  assert(cat2.walk == "Chocolate is walking")

}