

import io.gatling.core.Predef._

import scala.concurrent.duration._


class Resilience_Test extends Simulation {


  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenario("Resilience").
  //  inject(constantUsersPerSec(23) during(20 minutes))).
    inject(atOnceUsers(500))).
    throttle(
      reachRps(160) in (5 minutes),
      holdFor(5 minutes),
      jumpToRps(22),
      holdFor(5 minutes),
      reachRps(160) in (5 minutes),
      holdFor(5 minutes),
      jumpToRps(22),
      holdFor(5 minutes)
    ).
    protocols(scnConsultaProveedores.getHttpProtocol()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
