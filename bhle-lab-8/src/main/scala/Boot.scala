import java.io.{BufferedWriter, File, FileWriter}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, path, pathEndOrSingleSlash, pathPrefix, post}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{Bucket, GetObjectRequest}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import models.PathModel


object Boot extends App with JsonSupport {

  val log = LoggerFactory.getLogger("Boot")

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(30 seconds)

  val awsCreds = new BasicAWSCredentials(
    "AKIATPFNSO5RHADH5HLY",
    "bx9UcTivNgKY4FbpC4bxugDCp/fG9krp5NZ0Qu15"
  )

  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard
    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    .withRegion(Regions.EU_CENTRAL_1)
    .build

  val task1Bucket = "bhle-lab-8-bucket"
  val task2Bucket = "bhle-lab-8-bucket-2"

  if (s3Client.doesBucketExistV2(task1Bucket)) {
    log.info("Bucket exists")
  }
  else {
    s3Client.createBucket(task1Bucket)
    log.info("Bucket added")
  }
  if (s3Client.doesBucketExistV2(task2Bucket)) {
    log.info("Bucket exists")
  }
  else {
    s3Client.createBucket(task2Bucket)
    log.info("Bucket added")
  }

  val route =
    pathPrefix("lab8") {
      path("healthcheck") {
        pathEndOrSingleSlash {
          get {
            complete {
              log.info("Received healthcheck, replying with OK")
              "OK"
            }
          }
        }
      }~
        path("task1") {
          get {
            parameters("fileName".as[String]) {fileName =>
              complete {
                downloadFile(fileName)
              }
            }
          } ~
          post {
            entity(as[PathModel]) { pathModel =>
              complete {
                uploadFile(pathModel.path)
              }
            }
          }
        }~
        pathPrefix("task2") {
          path("out"){
            get {
              complete {
                val PATH = "src/main/resources/out/"
                val file = new File(PATH)
//                s3Client.putObject(task2Bucket, "out", file)
                uploadOut(file)
                "OK"
              }
            }
          }~
          path("in"){
            get {
              complete {
//                val PATH = "src/main/resources/in/"
//                val file = new File(PATH)
//                file.mkdir()
                downloadIn()
//                "OK"
              }
            }
          }
        }
    }


  def uploadFile(path: String): String = {
    val PATH = "src/main/resources/s3/"
    val newFilePath = PATH.concat(path)
    val file = new File(newFilePath)
//    if(file.createNewFile()) {
//      log.info(s"Putting file: ${file.getAbsolutePath}")
//      s3Client.putObject(task1Bucket, file.getName, file)
//      return "OK"
//    }
    file.createNewFile()
    log.info(s"Putting file: ${file.getAbsolutePath}")
    s3Client.putObject(task1Bucket, path, file)
    "OK"

//    "File already exists"
  }

  def downloadFile(filePath: String): String = {
    val PATH = "src/main/resources/s3/"
    val newFilePath = PATH.concat(filePath)
    val file = new File(newFilePath)
    val fileName = file.getName
    var ok = false
    try {
      val objects = s3Client.listObjects(task1Bucket)
      objects.getObjectSummaries.forEach(obj => {
        val key = obj.getKey
        log.info(key)
        if(filePath.contains("/")){
          if(key == filePath) {
//            file.createNewFile()
            s3Client.getObject(
              new GetObjectRequest(task1Bucket , key), file)
            ok = true
          }
        }
        else {
          val arr=key.split("/")
          val bucketFileName = arr(arr.length-1)
          if(bucketFileName == fileName){
            s3Client.getObject(
              new GetObjectRequest(task1Bucket , key), file)
            ok = true
          }
        }
      })
    }
    catch {
      case e: Exception =>
        return e.getMessage
    }
    if(!ok) return "File not found"
    "OK"
  }

  def uploadOut(f: File): Array[File] ={
    val PATH = "src/main/resources/out/"
    val these = f.listFiles
    these.foreach(realFile => {
      if (! realFile.isDirectory) {
        val arr = realFile.getAbsolutePath.split("/")
        var ok = true
        var realFilePath = ""
        arr.reverse.foreach(name => {
          if (name == "out") ok = false
          if (ok) {
            realFilePath = name + "/" + realFilePath
          }
        })
        val key = realFilePath.dropRight(1)
        s3Client.putObject(task2Bucket, key, realFile)
        log.info(key)
      }
    })
    these ++ these.filter(_.isDirectory).flatMap(uploadOut)
  }

  def downloadIn(): String ={
    val PATH = "src/main/resources/in/"
    try {
      val objects = s3Client.listObjects(task2Bucket)
      objects.getObjectSummaries.forEach(obj => {
        val key = obj.getKey
        val filePath = PATH.concat(key)
        log.info(filePath)
        val file = new File(filePath)
        log.info(file.getAbsolutePath)
        s3Client.getObject(
          new GetObjectRequest(task2Bucket , key), file)
      })
    }
    catch {
      case e: Exception =>
        return e.getMessage
    }
    "OK"
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  log.info("Listening on port 8080...")
}
