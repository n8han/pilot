package pilot

object Shared {
  def resources = new java.net.URL(getClass.getResource("/web/robots.txt"), ".")
}
