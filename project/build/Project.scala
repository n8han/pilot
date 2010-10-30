import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) {
  val uf_version = "0.2.2"

  lazy val shared = project("shared", "Pilot Resources", new DefaultProject(_) {
    lazy val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
    lazy val uff = "net.databinder" %% "unfiltered-filter" % uf_version
    lazy val ufjs = "net.databinder" %% "unfiltered-json" % uf_version
  })
  lazy val processor = project("processor", "Pilot Processor", new ProcessorProject(_) {
    // unfiltered
    lazy val df = "net.databinder" %% "dispatch-futures" % "0.7.6"
    override def compileOptions = super.compileOptions ++ Seq(Unchecked)
  }, shared)
  lazy val browser = project("browser", "Pilot Browser", new DefaultProject(_) {
    lazy val df = "net.databinder" %% "dispatch-http" % "0.7.6"
  }, shared)
}

