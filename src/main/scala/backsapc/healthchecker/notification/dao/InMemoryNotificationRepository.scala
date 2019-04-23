package backsapc.healthchecker.notification.dao
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConverters._
import scala.concurrent.Future

class InMemoryNotificationRepository extends NotificationRepository {
  private val repo         = new ConcurrentHashMap[Long, Channel]()
  private val currentIndex = new AtomicLong(0)

  override def addNotificationChannel(userId: UUID, email: String, confirmationCode: String): Future[Channel] =
    Future successful {
      val channel =
        Channel(currentIndex.incrementAndGet(), userId, email, isConfirmed = false, isDeleted = false, confirmationCode)
      repo.asScala.find(_._2.userId == userId) match {
        case Some(c) =>
          repo.replace(c._1, c._2.copy(isDeleted = true))
          repo.put(channel.id, channel)
          channel
        case None =>
          repo.put(channel.id, channel)
          channel
      }
    }
  override def deleteNotificationChannel(userId: UUID): Future[Unit] = Future successful {
    repo.asScala.find(_._2.userId == userId) match {
      case Some(c) =>
        repo.replace(c._1, c._2.copy(isDeleted = true))
        ()
      case None => ()
    }
  }
  override def confirmNotificationChannel(userId: UUID, channelId: Long): Future[Channel] =
    repo.asScala.find(c => c._2.userId == userId && c._1 == channelId).map(_._2) match {
      case Some(c) =>
        val channel = c.copy(isConfirmed = true)
        repo.replace(channelId, channel)
        Future successful channel
      case None => Future failed new NoSuchElementException()
    }
  override def getNotificationChannel(userId: UUID): Future[Option[Channel]] =
    Future successful repo.asScala.find(c => c._2.userId == userId && !c._2.isDeleted).map(_._2)

}
