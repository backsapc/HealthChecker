package backsapc.healthchecker.user.bcrypt

import com.github.t3hnar.bcrypt.BCryptStrOps

import scala.concurrent.{ExecutionContext, Future, blocking}

class BcryptHash(val hash: String) extends AnyVal
object BcryptHash {
  def apply(hash: String): BcryptHash = new BcryptHash(hash)

  def unapply(arg: BcryptHash): Option[String] = Some(arg.hash)
}

trait AsyncBcrypt {

  def hash(password: String, rounds: Int = 12): Future[BcryptHash]

  def verify(password: String, hash: BcryptHash): Future[Boolean]

}

class AsyncBcryptImpl(implicit executionContext: ExecutionContext) extends AsyncBcrypt {

  override def hash(password: String, rounds: Int): Future[BcryptHash] =
    Future {
      blocking(BcryptHash(password.bcrypt(rounds)))
    }

  override def verify(password: String, hash: BcryptHash): Future[Boolean] =
    Future {
      blocking(password.isBcrypted(hash.hash))
    }
}