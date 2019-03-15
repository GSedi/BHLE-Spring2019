object Boot extends App{

  case class Film( name: String,
                   yearOfRelease: Int,
                   imdbRating: Double)
  case class Director( firstName: String,
                       lastName: String,
                       yearOfBirth: Int,
                       films: Seq[Film])


  val memento = new Film("Memento", 2000, 8.5)
  val darkKnight = new Film("Dark Knight", 2008, 9.0)
  val inception = new Film("Inception", 2010, 8.8)
  val highPlainsDrifter = new Film("High Plains Drifter", 1973, 7.7)
  val outlawJoseyWales = new Film("The Outlaw Josey Wales", 1976, 7.9)
  val unforgiven = new Film("Unforgiven", 1992, 8.3)
  val granTorino = new Film("Gran Torino", 2008, 8.2)
  val invictus = new Film("Invictus", 2009, 7.4)
  val predator = new Film("Predator", 1987, 7.9)
  val dieHard = new Film("Die Hard", 1988, 8.3)
  val huntForRedOctober = new Film("The Hunt for Red October", 1990, 7.6)
  val thomasCrownAffair = new Film("The Thomas Crown Affair", 1999, 6.8)
  val eastwood = new Director("Clint", "Eastwood", 1930,
    Seq(highPlainsDrifter, outlawJoseyWales, unforgiven, granTorino, invictus))
  val mcTiernan = new Director("John", "McTiernan", 1951,
    Seq(predator, dieHard, huntForRedOctober, thomasCrownAffair))
  val nolan = new Director("Christopher", "Nolan", 1970,
    Seq(memento, darkKnight, inception))
  val someGuy = new Director("Just", "Some Guy", 1990,
    Seq())
  val directors = Seq(eastwood, mcTiernan, nolan, someGuy)

  // Task 1
  def filteredCountFilms(numberOfFilms: Int) : Seq[Director] = {
    val filtered = directors.filter(director => director.films.length > numberOfFilms)
    filtered
  }

  // Task 2
  def filteredYear(year: Int): Seq[Director] = {
    directors.filter(director => director.yearOfBirth < year)
  }

  // Task 3
  def filteredYearNumOfFilms(year: Int, numberOfFilms: Int): Seq[Director] = {
    directors.filter(director => director.yearOfBirth < year).filter(director => director.films.length > numberOfFilms)
  }

  // Task 4
  def sordDirectors(ascending: Boolean = true) = {
    if(ascending){
      directors.sortBy(_.yearOfBirth)
    }
    else {
      directors.sortBy(_.yearOfBirth)(Ordering[Int].reverse)
    }
  }

  // Task 5
  def nolanFilms() = nolan.films.map(film => film.name)

  // Task 6
  def  cinephile() = {
    directors.flatMap(dir => dir.films).map(film => film.name)
  }

  // Task 7
  def vintageMcTiernan() = {
    val asd = mcTiernan.films.sortBy(_.yearOfRelease).map(film => film.yearOfRelease)
    asd.headOption
  }

  // Task 8
  def highScoreTable() = {
    directors.flatMap(dir => dir.films).sortBy(_.imdbRating)(Ordering[Double].reverse)
  }

  // Task 9
  def avgScore() = {
    val sum = directors.flatMap(dir => dir.films).map(film => film.imdbRating).foldLeft(0.0)((a, b) => a + b)
    val leng = directors.flatMap(dir => dir.films).map(film => film.imdbRating).length
    sum / leng
  }

  // Task 10
  def tonightsListings() = {
    directors.foreach(dir => dir.films.foreach(f => println(s"Tonight only! ${f.name} by ${dir.firstName + " " + dir.lastName}!")))
  }

  // Task 11
  def fromTheArchives() = {
    val asd = directors.flatMap(dir => dir.films).sortBy(_.yearOfRelease)
    asd.headOption
  }

  // Options

  // Task 1
  def divide(a: Int, b: Int): Option[Int] = {
    if ( b != 0) Some(a/b) else  None
  }

  // Task 2
  def readInt(str: String) =
    if(str matches "\\d+") Some(str.toInt) else None

  def calcOfOptionsUsingFor(opt1: Option[Int], operator:String, opt2: Option[Int]) = {

    operator match {
      case "+" => opt1.flatMap{val1 => opt2.map {val2 => val1 + val2}}
      case "-" => opt1.flatMap{val1 => opt2.map {val2 => val1 - val2}}
      case "*" => opt1.flatMap{val1 => opt2.map {val2 => val1 * val2}}
      case "/" => opt1.flatMap{val1 => opt2.flatMap {val2 => divide(val1, val2)}}
      case _ => None
    }

//    /*val temp =*/ for {
//      value1 <- opt1
//      value2 <- opt2
//    } yield operator match {
//      case "+" => value1 + value2
//      case "-" => value1 - value2
//      case "*" => value1 * value2
//      case "/" => divide(value1, value2).flatten
//      case  _  => None
//    }
////    temp.flatten
  }

  def calculator(operand1: String, operator: String, operand2: String) = {
    val opt1 = readInt(operand1)
    val opt2 = readInt(operand2)
    calcOfOptionsUsingFor(opt1, operator, opt2)
  }

  println(calculator("4", "f", "0"))
  val opt: Option[Int] = None
}
