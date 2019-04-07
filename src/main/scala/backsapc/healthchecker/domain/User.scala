package backsapc.healthchecker.domain

import java.util.UUID

case class Account(id: UUID, login: String, password: String, email: String)

