

import io.gatling.core.Predef._

class OneThread_30Iterations extends Simulation {

    val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenario("Scn_OneThread_ThirtyIterations",30).inject(atOnceUsers(1))).
    protocols(scnConsultaProveedores.getHttpProtocol()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
