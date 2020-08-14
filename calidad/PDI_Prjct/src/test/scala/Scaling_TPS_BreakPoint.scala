

import io.gatling.core.Predef._

import scala.concurrent.duration._


class Scaling_TPS_BreakPoint extends Simulation {


  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenario("Find_BreakPoint").
    inject(atOnceUsers(5000))).
    throttle(
      reachRps(500) in (30 minutes)
    ).
    protocols(scnConsultaProveedores.getHttpProtocol()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
