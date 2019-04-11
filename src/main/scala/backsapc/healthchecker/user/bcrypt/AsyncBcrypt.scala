package backsapc.healthchecker.user.bcrypt

import com.github.t3hnar.bcrypt.BCryptStrOps

import scala.concurrent.{ExecutionContext, Future, blocking}

trait AsyncBcrypt {

  def hash(password: String, rounds: Int = 12): Future[String]

  def verify(password: String, hash: String): Future[Boolean]

}

class AsyncBcryptImpl(implicit executionContext: ExecutionContext) extends AsyncBcrypt {

  override def hash(password: String, rounds: Int): Future[String] =
    Future {
      blocking(password.bcrypt(rounds))
    }

  override def verify(password: String, hash: String): Future[Boolean] =
    Future {
      blocking(password.isBcrypted(hash))
    }
}