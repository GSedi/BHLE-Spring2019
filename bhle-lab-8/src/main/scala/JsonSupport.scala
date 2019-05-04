import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import models.PathModel
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat


trait JsonSupport {
  implicit val pathModelFormat: RootJsonFormat[PathModel] = jsonFormat1(PathModel)
}
