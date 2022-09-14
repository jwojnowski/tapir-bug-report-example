package me.wojnowski.tapir

import org.http4s.blaze.server.BlazeServerBuilder
import sttp.model.StatusCode
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZIOAppArgs
import zio.interop.catz._

object Main extends zio.ZIOAppDefault {
  val matcher1 =
    oneOfVariantValueMatcher(
      statusCode(StatusCode.unsafeApply(418))
        .and(plainBody[String])
    )(_ => false)

  val matcher2 =
    oneOfVariantValueMatcher(
      statusCode(StatusCode.InternalServerError)
        .and(plainBody[String])
    )(_ => true)

  val buggyEndpoint =
    endpoint
      .get
      .in("some" / "path1")
      .out(emptyOutput)
      .errorOut(oneOf(matcher1))
      .errorOutVariant(matcher2)

  val workingEndpoint =
    endpoint
      .get
      .in("some" / "path2")
      .out(emptyOutput)
      .errorOut(oneOf(matcher1, matcher2))

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val routes =
      ZHttp4sServerInterpreter[Any]().from(List[ZServerEndpoint[Any, Any]](
      buggyEndpoint.zServerLogic { _ =>
        ZIO.fail("fail") // results in net::ERR_EMPTY_RESPONSE after timeout
      },
      workingEndpoint.zServerLogic { _ =>
        ZIO.fail("fail") // results in an expected response: status 500, body `"fail"`
      }
    )).toRoutes

    BlazeServerBuilder[Task]
      .bindHttp(port = 8080, host = "0.0.0.0")
      .withHttpApp(routes.orNotFound)
      .resource
      .toScopedZIO *> ZIO.never
  }
}
