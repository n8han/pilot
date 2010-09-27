import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) {
  val uf_version = "0.2.1-SNAPSHOT"

  lazy val processor = project("processor", "Pilot Processor", new ProcessorProject(_) {
    override def buildScalaVersion = "2.7.7"
    // unfiltered
    lazy val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
    lazy val uff = "net.databinder" %% "unfiltered-filter" % uf_version
    lazy val df = "net.databinder" %% "dispatch-futures" % "0.7.5"
  })
  lazy val browser = project("browser", "Pilot Browser", new DefaultProject(_) {
    lazy val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
    lazy val uff = "net.databinder" %% "unfiltered-filter" % uf_version
    lazy val ufjs = "net.databinder" %% "unfiltered-json" % uf_version
  })
}

