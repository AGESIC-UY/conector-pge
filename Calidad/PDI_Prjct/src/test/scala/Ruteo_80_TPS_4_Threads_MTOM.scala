

import io.gatling.core.Predef._

import scala.concurrent.duration._


class Ruteo_80_TPS_4_Threads_MTOM extends Simulation {


  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(
    //Scaling 80 TPS Scenario
    scnConsultaProveedores.getScenarioRuteoWithDuration("Scn_Scaling_80_TPS",60).
    inject(atOnceUsers(500)).
    throttle(
      reachRps(80) in (15 minutes),
      holdFor(45 minutes)
    ).
    protocols(scnConsultaProveedores.getHttpProtocol()),
    //MTOM Scenario
    scnConsultaProveedores.getScenarioRuteoMTOMWithDuration("Scn_Scaling_4_TPS",60).
      inject(atOnceUsers(50)).
      throttle(
        reachRps(4) in (15 minutes),
        holdFor(45 minutes)
      ).
    protocols(scnConsultaProveedores.getHttpProtocolMTOM())
  ).assertions(
    global.failedRequests.percent.lt(1)
  )

}
