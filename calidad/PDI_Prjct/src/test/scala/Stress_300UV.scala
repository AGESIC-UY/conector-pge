import io.gatling.core.Predef._
import scala.concurrent.duration._

class Stress_300UV  extends Simulation {

  val scnConsultaProveedores= new ConsultaProveedores()

  setUp(scnConsultaProveedores.getScenarioWithDuration("Stress_300UV",60)
    inject(
    rampConcurrentUsers(0) to (300) during (15 minutes),
    constantConcurrentUsers(300) during (45 minutes))).
    protocols(scnConsultaProveedores.getHttpProtocol()).
    assertions(
      global.failedRequests.percent.lt(1)
    )
}
