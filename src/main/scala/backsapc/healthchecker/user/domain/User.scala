package backsapc.healthchecker.user.domain

import java.util.UUID

import backsapc.healthchecker.user.bcrypt.BcryptHash

case class Account(id: UUID, login: String, password: BcryptHash, email: String)
