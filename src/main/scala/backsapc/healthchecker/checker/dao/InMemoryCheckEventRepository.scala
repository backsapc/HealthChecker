package backsapc.healthchecker.checker.dao
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import backsapc.healthchecker.checker.domain.{ CheckEvent, CheckEventStatus }

import scala.collection.JavaConverters._
import scala.concurrent.Future

class InMemoryCheckEventRepository extends CheckerJobRepository {
  private val repo         = new ConcurrentHashMap[Long, CheckEvent]()
  private val currentIndex = new AtomicLong(0)

  override def createCheckEvents(checkIds: Seq[UUID]): Future[Seq[CheckEvent]] = {
    val checkEvents = checkIds.map(
      check =>
        CheckEvent(currentIndex.incrementAndGet(), check, OffsetDateTime.now(), CheckEventStatus.NotStarted, None)
    )
    checkEvents.foreach(checkEvent => repo.put(checkEvent.id, checkEvent))
    Future successful checkEvents
  }

  override def getNotStartedCheckEvents(count: Int): Future[Seq[CheckEvent]] =
    Future successful { repo.asScala.filter(_._2.status == CheckEventStatus.NotStarted).take(count).values.toSeq }

  override def updateCheckEventsAsStarted(ids: Seq[Long]): Future[Seq[CheckEvent]] =
    Future successful repo.asScala.values
      .filter(checkEvent => ids.contains(checkEvent.id))
      .map(check => {
        val newValue = check.copy(status = CheckEventStatus.Started)
        repo.replace(check.id, newValue)
        newValue
      })
      .toSeq

  override def updateCheckEventAsCompleted(id: Long, isSuccessful: Boolean, message: String): Future[Unit] =
    Future successful {
      repo.asScala
        .get(id)
        .map(
          check =>
            repo.replace(check.id,
                         check.copy(status = if (isSuccessful) CheckEventStatus.Successful else CheckEventStatus.Failed,
                                    message = Some(message)))
        )
      ()
    }

  override def notStartedExist(): Future[Boolean] =
    Future successful repo.asScala.exists(_._2.status == CheckEventStatus.NotStarted)
}
