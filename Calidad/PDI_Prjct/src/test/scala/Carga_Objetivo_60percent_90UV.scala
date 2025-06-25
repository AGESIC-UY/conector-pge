

import io.gatling.core.Predef._

import scala.concurrent.duration._


class Carga_Objetivo_60percent_90UV extends Simulation {

  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenarioWithDuration("Carga_Objetivo_60percent_90UV",30)
    inject(
      rampConcurrentUsers(0) to (90) during (10 minutes),
      constantConcurrentUsers(90) during (20 minutes))).
    protocols(scnConsultaProveedores.getHttpProtocol()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
