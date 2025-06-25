import io.gatling.core.Predef._

import scala.concurrent.duration._

class Stress_6UV_MTOM extends Simulation {

  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenarioMTOMWithDuration("Stress_6UV_MTOM",75)
    inject(
    rampConcurrentUsers(0) to (6) during (15 minutes),
    constantConcurrentUsers(6) during (60 minutes))).
    protocols(scnConsultaProveedores.getHttpProtocolMTOM()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
