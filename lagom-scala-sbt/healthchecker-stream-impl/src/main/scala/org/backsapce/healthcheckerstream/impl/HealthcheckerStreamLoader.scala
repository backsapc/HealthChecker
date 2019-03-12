package org.backsapce.healthcheckerstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import org.backsapce.healthcheckerstream.api.HealthcheckerStreamService
import org.backsapce.healthchecker.api.HealthcheckerService
import com.softwaremill.macwire._

class HealthcheckerStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new HealthcheckerStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new HealthcheckerStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[HealthcheckerStreamService])
}

abstract class HealthcheckerStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[HealthcheckerStreamService](wire[HealthcheckerStreamServiceImpl])

  // Bind the HealthcheckerService client
  lazy val healthcheckerService: HealthcheckerService = serviceClient.implement[HealthcheckerService]
}
