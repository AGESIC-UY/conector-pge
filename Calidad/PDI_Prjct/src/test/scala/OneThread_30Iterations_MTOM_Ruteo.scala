

import io.gatling.core.Predef._

class OneThread_30Iterations_MTOM_Ruteo extends Simulation {

    val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenarioRuteoMTOM("Scn_OneThread_ThirtyIterations",30).inject(atOnceUsers(1))).
    protocols(scnConsultaProveedores.getHttpProtocolMTOM()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
