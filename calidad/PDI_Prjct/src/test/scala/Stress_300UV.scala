import io.gatling.core.Predef._
import scala.concurrent.duration._

class Stress_300UV  extends Simulation {

  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenario("Stress_300UV")
    inject(
    rampConcurrentUsers(0) to (300) during (15 minutes),
    constantConcurrentUsers(300) during (45 minutes))).
    protocols(scnConsultaProveedores.getHttpProtocol()).
    assertions(
      global.failedRequests.percent.lt(1)
    )
}
