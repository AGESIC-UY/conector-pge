

import io.gatling.core.Predef._

import scala.concurrent.duration._


class Endurance_Mixed extends Simulation {


  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(
    //Scaling 22 TPS Scenario
    scnConsultaProveedores.getScenario("Scn_Scaling_22_TPS").
      inject(atOnceUsers(500)).
      throttle(
        reachRps(22) in (10 minutes),
        holdFor(8 hours)
      ).
      protocols(scnConsultaProveedores.getHttpProtocol()),
    //MTOM Scenario
    scnConsultaProveedores.getScenarioMTOMWithDuration("Scn_Scaling_7_TPS",490).
      inject(rampUsers(7) during (1 minutes)).
      protocols(scnConsultaProveedores.getHttpProtocolMTOM())
  ).assertions(
    global.failedRequests.percent.lt(1)
  )


}
