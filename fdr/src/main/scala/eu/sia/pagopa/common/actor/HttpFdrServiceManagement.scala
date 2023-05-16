package eu.sia.pagopa.common.actor

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, ContentType => _}
import eu.sia.pagopa.ActorProps
import eu.sia.pagopa.common.message._
import eu.sia.pagopa.common.repo.re.model.Re
import eu.sia.pagopa.common.util.NodoLogger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object HttpFdrServiceManagement extends HttpBaseServiceManagement {

  def retrieveFlow(
                        sessionId: String,
                        testCaseId: Option[String],
                        action: String,
                        receiver: String,
                        payload: String,
                        actorProps: ActorProps,
                        re: Re
                      )(implicit log: NodoLogger, ec: ExecutionContext, as: ActorSystem) = {

    val (url, timeout) = loadServiceConfig(action, receiver)

    val simpleHttpReq = SimpleHttpReq(
      sessionId,
      action,
      ContentTypes.`application/json`,
      HttpMethods.GET,
      s"$url",
      Some(payload),
      Seq(),
      Some(receiver),
      re,
      timeout.seconds,
      None,
      testCaseId
    )

    callService(simpleHttpReq, action, receiver, actorProps, false)
  }

  def retrievePaymentsFlow(
                    sessionId: String,
                    testCaseId: Option[String],
                    action: String,
                    receiver: String,
                    payload: String,
                    actorProps: ActorProps,
                    re: Re
                  )(implicit log: NodoLogger, ec: ExecutionContext, as: ActorSystem) = {

    val (url, timeout) = loadServiceConfig(action, receiver)

    val simpleHttpReq = SimpleHttpReq(
      sessionId,
      action,
      ContentTypes.`application/json`,
      HttpMethods.GET,
      s"$url",
      Some(payload),
      Seq(),
      Some(receiver),
      re,
      timeout.seconds,
      None,
      testCaseId
    )

    callService(simpleHttpReq, action, receiver, actorProps, false)
  }

  def readConfirm(
                            sessionId: String,
                            testCaseId: Option[String],
                            action: String,
                            receiver: String,
                            payload: String,
                            actorProps: ActorProps,
                            re: Re
                          )(implicit log: NodoLogger, ec: ExecutionContext, as: ActorSystem) = {

    val (url, timeout) = loadServiceConfig(action, receiver)

    val simpleHttpReq = SimpleHttpReq(
      sessionId,
      action,
      ContentTypes.`application/json`,
      HttpMethods.PUT,
      s"$url",
      Some(payload),
      Seq(),
      Some(receiver),
      re,
      timeout.seconds,
      None,
      testCaseId
    )

    callService(simpleHttpReq, action, receiver, actorProps, false)
  }

}
