package backsapc.healthchecker.checker.contracts

trait CheckerWorker {
  def start()

  def stop()

  def restart()
}
