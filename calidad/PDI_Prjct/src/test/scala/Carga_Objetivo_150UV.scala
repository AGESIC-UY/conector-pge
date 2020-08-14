

import io.gatling.core.Predef._

import scala.concurrent.duration._


class Carga_Objetivo_150UV extends Simulation {

  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenario("Carga_Objetivo_150UV")
    inject(
      rampConcurrentUsers(0) to (150) during (10 minutes),
      constantConcurrentUsers(150) during (20 minutes))).
    protocols(scnConsultaProveedores.getHttpProtocol()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
