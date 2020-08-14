

import io.gatling.core.Predef._

import scala.concurrent.duration._


class Carga_Objetivo_3UV_MTOM extends Simulation {

  val scnConsultaProveedores= new ConsultaProveedores()

  /*setUp(scnConsultaProveedores.getScenario("Scn_Scaling_7_TPS").
    inject(atOnceUsers(500))).
    throttle(
      reachRps(7) in (30 seconds),
      holdFor(10 minutes)
    ).
    protocols(scnConsultaProveedores.getHttpProtocol()).
    assertions(
      global.failedRequests.percent.lt(1)
    )*/

  /*setUp(scnConsultaProveedores.getScenarioMTOM("Scn_Scaling_7_TPS").
    inject(atOnceUsers(500))).
    throttle(
      reachRps(7) in (30 seconds),
      holdFor(10 minutes)
    ).
    protocols(scnConsultaProveedores.getHttpProtocolMTOM()).
    assertions(
      global.failedRequests.percent.lt(1)
    )*/
/*
  setUp(scnConsultaProveedores.getScenarioMTOMWithDuration("Scn_Scaling_7_TPS",20).
    inject(rampUsers(3) during (10 minutes))).
    protocols(scnConsultaProveedores.getHttpProtocolMTOM()).
    assertions(
      global.failedRequests.percent.lt(1)
    )
  */
  setUp(scnConsultaProveedores.getScenarioMTOM("Carga_Objetivo_3UV_MTOM")
    inject(
    rampConcurrentUsers(0) to (3) during (10 minutes),
    constantConcurrentUsers(3) during (20 minutes))).
    protocols(scnConsultaProveedores.getHttpProtocol()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
