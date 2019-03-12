package org.backsapce.healthcheckerstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import org.backsapce.healthcheckerstream.api.HealthcheckerStreamService
import org.backsapce.healthchecker.api.HealthcheckerService

import scala.concurrent.Future

/**
  * Implementation of the HealthcheckerStreamService.
  */
class HealthcheckerStreamServiceImpl(healthcheckerService: HealthcheckerService) extends HealthcheckerStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(healthcheckerService.hello(_).invoke()))
  }
}
