package eu.sia.pagopa

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.RequestContext
import org.mockito.MockitoSugar.mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Future

//@org.scalatest.Ignore
class ConfigUnitTests() extends BaseUnitTest with BeforeAndAfterAll{

  def toAnswerWithArgs[T](f: InvocationOnMock => T) = new Answer[T] {
    override def answer(i: InvocationOnMock): T = f(i)
  }
  val requestContext = mock[RequestContext](toAnswerWithArgs((i)=>{
    val res = i.getArgument(0).asInstanceOf[ToResponseMarshallable].value.asInstanceOf[HttpResponse]
    Future.successful(akka.http.scaladsl.server.RouteResult.Complete.apply(res))
  })
  )

}
