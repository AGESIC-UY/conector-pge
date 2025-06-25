

import io.gatling.core.Predef._

import scala.concurrent.duration._


class Carga_Objetivo_3UV_MTOM extends Simulation {

  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenarioMTOMWithDuration("Carga_Objetivo_3UV_MTOM",30)
    inject(
    rampConcurrentUsers(0) to (3) during (10 minutes),
    constantConcurrentUsers(3) during (20 minutes))).
    protocols(scnConsultaProveedores.getHttpProtocolMTOM()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
