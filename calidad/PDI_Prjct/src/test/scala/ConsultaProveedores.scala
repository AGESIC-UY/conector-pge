

import com.typesafe.config._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class ConsultaProveedores extends Simulation {


  val feeder = csv("urlsConectoresDesc.csv").circular
  val feederRuteo = csv("urlsRuteadores.csv").circular
  val feederMTOM = csv("urlsConectoresMTOM.csv").circular
  val feederRuteoMTOM = csv("urlsRuteadoresMTOM.csv").circular

  //Object for loading configurations
  object config {
    val conf = ConfigFactory.load("config.properties")
    val p = conf.getInt("service.pause")
    val filename = conf.getString("service.filename")
    val filenameMTOM = conf.getString("service.filenameMTOM")
  }

  val httpProtocol = http
    .inferHtmlResources()
    .acceptEncodingHeader("gzip,deflate")
    .contentTypeHeader("text/xml;charset=UTF-8")
    .userAgentHeader("Apache-HttpClient/4.1.1 (java 1.5)")

  val headers_0 = Map("SOAPAction" -> """""""")


  val httpProtocolMTOM = http
    .inferHtmlResources()
    .acceptEncodingHeader("gzip,deflate")
    .contentTypeHeader("""multipart/related; type="application/xop+xml"; start="<rootpart@soapui.org>"; start-info="text/xml"; boundary="----=_Part_0_27477334.1513187882246"""")
    .userAgentHeader("Apache-HttpClient/4.1.1 (java 1.5)")

  val headersMTOM = Map(
    "MIME-Version" -> "1.0",
    "SOAPAction" -> "")


  object service {
    val invocation = group("SOAP_invocation") {
      feed(feeder)
        .exec(http("ConsultaProveedores")
          .post("${endpoint}")
          .headers(headers_0)
          .body(RawFileBody(config.filename)))
        .pause(config.p seconds)
    }
  }



  object serviceMTOM {
    val invocation = group("SOAP_invocation") {
      exitBlockOnFail {
        feed(feederMTOM)
          .exec(http("EnviarExpediente")
            .post("${endpoint}")
            //.post("http://10.255.4.17:9800/SAENovedades/consulta")
            .headers(headersMTOM)
            .body(RawFileBody(config.filenameMTOM))
            .check(status.is(200))
          )
          .pause(config.p seconds)
      }

    }
  }

  object serviceRuteo {
    val invocation = group("SOAP_invocation") {
      feed(feederRuteo)
        .exec(http("ConsultaProveedores")
          .post("${endpoint}")
          .headers(headers_0)
          .body(RawFileBody(config.filename)))
        .pause(config.p seconds)
    }
  }

  object serviceRuteoMTOM {
    val invocation = group("SOAP_invocation") {
      feed(feederRuteoMTOM)
        .exec(http("ConsultaProveedores")
          .post("${endpoint}")
          .headers(headers_0)
          .body(RawFileBody(config.filenameMTOM)))
        .pause(config.p seconds)
    }
  }

/*  object serviceMTOM {
    val invocation = group("SOAP_invocation") {
        feed(feeder)
          .exec(http("ConsultaProveedores")
            .post("${endpoint}")
            .headers(headersMTOM)
            .body(RawFileBody(config.filename))
            .check(regex("54555111605784")))
          .pause(config.p seconds)
      }
  }*/

  /**
    *
    * @param name String scenario name
    * @param iterations int number of repetitions
    * @return scenario
    */
  def getScenario(name : String, iterations : Int): ScenarioBuilder ={
    val scn = scenario(name).repeat(iterations){
      exec(service.invocation)}
    return scn
  }

  /**
    *
    * @param name String scenario name
    * @return
    */
  def getScenario(name : String): ScenarioBuilder ={
    val scn = scenario(name).forever(){
      exec(service.invocation)
    }
    return scn
  }

  def getScenarioWithDuration(name : String, duration : Int): ScenarioBuilder ={
    val scn = scenario(name).during(duration minutes){
      exec(service.invocation)
    }
    return scn
  }

  /**
    * @return httpProtocol
    */
  def getHttpProtocol(): HttpProtocolBuilder ={
     return httpProtocol
  }


  //Methods for MTOM tests

  /**
    * Headers for MTOM tests
    * @return httpProtocolMTOM
    */
  def getHttpProtocolMTOM(): HttpProtocolBuilder ={
    return httpProtocolMTOM
  }


  /**
    * Scenario for MTOM tests
    * @param name String scenario name
    * @return scenario
    */
  def getScenarioMTOM(name : String): ScenarioBuilder ={
    val scn = scenario(name).forever(){
      exec(serviceMTOM.invocation)
    }
    return scn
  }

  /**
    * Scenario for MTOM tests with duration
    * @param name String scenario name
    * @return scenario
    */
  def getScenarioMTOMWithDuration(name : String, duration : Int): ScenarioBuilder ={
    val scn = scenario(name).during(duration minutes){
      exec(serviceMTOM.invocation)
    }
    return scn
  }

  /**
    *
    * @param name
    * @param iterations
    * @return
    */
  def getScenarioMTOM(name : String, iterations : Int): ScenarioBuilder ={
    val scn = scenario(name).repeat(iterations){
      exec(serviceMTOM.invocation)}
    return scn
  }

  def getScenarioRuteo(name : String): ScenarioBuilder ={
    val scn = scenario(name).forever(){
      exec(serviceRuteo.invocation)
    }
    return scn
  }

  def getScenarioRuteo(name : String, iterations : Int): ScenarioBuilder ={
    val scn = scenario(name).repeat(iterations){
      exec(serviceRuteo.invocation)}
    return scn
  }

  def getScenarioRuteoMTOM(name : String, iterations : Int): ScenarioBuilder ={
    val scn = scenario(name).repeat(iterations){
      exec(serviceRuteoMTOM.invocation)}
    return scn
  }

  def getScenarioRuteoWithDuration(name : String, duration : Int): ScenarioBuilder ={
    val scn = scenario(name).during(duration minutes){
      exec(serviceRuteo.invocation)
    }
    return scn
  }

  def getScenarioRuteoMTOMWithDuration(name : String, duration : Int): ScenarioBuilder ={
    val scn = scenario(name).during(duration minutes){
      exec(serviceRuteoMTOM.invocation)
    }
    return scn
  }


}
