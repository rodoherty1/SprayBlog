package com.danielasfregola.quiz.management

import akka.actor._
import akka.util.Timeout
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.duration._
import scala.language.postfixOps

class RestInterface extends HttpServiceActor with RestApi {
  override def receive: Receive = runRoute(routes)

}

trait RestApi extends HttpService with ActorLogging {
  actor: Actor =>

  implicit val timeout = Timeout(10 seconds)

  var quizzes = Vector[Quiz]()

  def routes: Route =
    get {
      complete("Hey Sweetie ... xx ... Let's watch some TV ? :-)")
    } ~
      pathPrefix("quizzes") {
        pathEnd {
          post {
            entity(as[Quiz]) { quiz => requestContext =>
              val responder = createResponder(requestContext)
              createQuiz(quiz) match {
                case true => responder ! QuizCreated
                case _ => responder ! QuizAlreadyExists
              }
            }
          }
        } ~
          path(Segment) { id =>
            delete { requestContext =>
              val responder = createResponder(requestContext)
              deleteQuiz(id)
              responder ! QuizDeleted
            }
          }
      }

  private def createQuiz(quiz: Quiz): Boolean = {
    val doesNotExist = !quizzes.exists(_.id == quiz.id)
    if (doesNotExist) quizzes = quizzes :+ quiz
    doesNotExist
  }

  private def deleteQuiz(id: String): Unit = {
    quizzes = quizzes.filterNot(_.id == id)
  }

  private def createResponder(requestContext: RequestContext) = {
    context.actorOf(Props(new Responder(requestContext)))
  }
}

class Responder(requestContext: RequestContext) extends Actor with ActorLogging {

  def receive = {

    case QuizCreated =>
      requestContext.complete(StatusCodes.Created)
      killYourself()

    case QuizDeleted =>
      requestContext.complete(StatusCodes.OK)
      killYourself()

    case QuizAlreadyExists =>
      requestContext.complete(StatusCodes.Conflict)
      killYourself()

  }

  private def killYourself() = self ! PoisonPill

}