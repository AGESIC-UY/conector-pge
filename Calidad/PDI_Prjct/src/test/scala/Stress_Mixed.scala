

import io.gatling.core.Predef._

import scala.concurrent.duration._


class Stress_Mixed extends Simulation {


  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(
    // 300 UV, mensajes simples
    scnConsultaProveedores.getScenarioWithDuration("Stress_300UV_simple",60).
      inject(
        rampConcurrentUsers(0) to (300) during (15 minutes),
        constantConcurrentUsers(300) during (45 minutes)).
      protocols(scnConsultaProveedores.getHttpProtocol()),
    //MTOM Scenario
    scnConsultaProveedores.getScenarioMTOMWithDuration("Stress_6UV_MTOM",60).
      inject(
      rampConcurrentUsers(0) to (6) during (15 minutes),
      constantConcurrentUsers(6) during (45 minutes)).
      protocols(scnConsultaProveedores.getHttpProtocolMTOM())
  ).assertions(
    global.failedRequests.percent.lt(1)
  )


}
