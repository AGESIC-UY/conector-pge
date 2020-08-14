

import io.gatling.core.Predef._

import scala.concurrent.duration._


class MTOM_Scaling_Threads extends Simulation {

  val scnConsultaProveedores= new ConsultaProveedores()


  setUp(scnConsultaProveedores.getScenarioMTOMWithDuration("MTOM_Scaling_Threads",16).
    inject(rampUsers(50) during (15 minutes))).
    protocols(scnConsultaProveedores.getHttpProtocolMTOM()).
    assertions(
      global.failedRequests.percent.lt(1)
    )

}
