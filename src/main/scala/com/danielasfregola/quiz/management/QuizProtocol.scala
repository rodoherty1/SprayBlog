package com.danielasfregola.quiz.management

import spray.json.DefaultJsonProtocol

object Quiz extends DefaultJsonProtocol {
  implicit val format = jsonFormat3(Quiz.apply)
}

case class Quiz(id: String, question: String, correctAnswer: String)

case object QuizCreated

case object QuizAlreadyExists

case object QuizDeleted

