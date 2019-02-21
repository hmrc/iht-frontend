package utils

import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.DefaultAwaitTimeout
import play.api.{Application, Configuration}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration._

trait IntegrationBaseSpec extends UnitSpec
  with GuiceOneServerPerSuite
  with WiremockHelper
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with DefaultAwaitTimeout {

  override implicit def defaultAwaitTimeout: Timeout = 5.seconds

  val localHost = "localhost"
  val localPort: Int = port
  val localUrl  = s"http://$localHost:$localPort"

  val additionalConfiguration: Seq[(String, Any)] = Seq.empty

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Configuration("testserver.port" -> s"$localPort"))
    .configure(Configuration("application.router" -> "testOnlyDoNotUseInAppConf.Routes"))
    .configure(Configuration("microservice.services.iht.port" -> s"${WiremockHelper.wiremockPort}"))
    .configure(Configuration("auditing.consumer.baseUri.port" -> s"${WiremockHelper.wiremockPort}"))
    .configure(Configuration("metrics.enabled" -> true))
    .configure(Configuration(additionalConfiguration: _*))
    .build()

  override def beforeEach() = {
    resetWiremock()
  }

  override def beforeAll() = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll() = {
    stopWiremock()
    super.afterAll()
  }
}
