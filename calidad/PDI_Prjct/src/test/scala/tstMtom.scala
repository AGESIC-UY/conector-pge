
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class tstMtom extends Simulation {

  val httpProtocol = http
    .inferHtmlResources()
    .acceptEncodingHeader("gzip,deflate")
    .connectionHeader("close")
    .contentTypeHeader("""multipart/related; type="application/xop+xml"; start="<rootpart@soapui.org>"; start-info="text/xml"; boundary="----=_Part_0_27477334.1513187882246"""")
    .userAgentHeader("Apache-HttpClient/4.1.1 (java 1.5)")

  /*val headers_0 = Map(
    "MIME-Version" -> "1.0",
    "SOAPAction" -> """"http://servicios.pge.red.uy/agesic/artee/EnviarExpediente/ServicioEnviarExpediente/EnviarExpediente"""")
*/
  val headers_0 = Map(
    "MIME-Version" -> "1.0",
    "SOAPAction" -> """"http://DESKTOP-KNT9DTC:8088/mockCustomBinding_ServicioEnviarExpediente"""")

 // val uri1 = "http://10.255.4.54:8080/agesic-wsa/http/wsaRouter"
  val uri1 = "http://DESKTOP-KNT9DTC:8088/mockCustomBinding_ServicioEnviarExpediente"
  val r = new scala.util.Random


  /*val scn =
    scenario("tstMtom").during(11 minutes){
      exitBlockOnFail{
        exec(_.set("id", 100 + r.nextInt((900))))
          .exec(
            http("Invocation")
              .post(uri1)
              .headers(headers_0)
              .body(ElFileBody("tstMtom_0000_request.txt"))
              .check(regex("54555111605784"))
              .check(status.is(500)))
      }
    }*/

  val scn =
    scenario("tstMtom").during(11 minutes){
        exec(_.set("id", 100 + r.nextInt((900))))
          .exec(
            http("Invocation")
              .post(uri1)
              .headers(headers_0)
             // .body(ElFileBody("tstMtom_0000_request2.txt"))
            .check(status.is(200)))
    }


  setUp(scn.inject(rampUsers(7) during (1 minutes))).protocols(httpProtocol)
}