import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) {
  val uf_version = "0.1.5-SNAPSHOT"

  lazy val processor = project("processor", "Pilot Processor", new ProcessorProject(_) {
    override def buildScalaVersion = "2.7.7"
    // unfiltered
    lazy val uf = "net.databinder" %% "unfiltered-server" % uf_version
    lazy val df = "net.databinder" %% "dispatch-futures" % "0.7.5"
  })
  lazy val app = project("app", "Pilot Application", new DefaultProject(_) {
    lazy val uf = "net.databinder" %% "unfiltered-server" % uf_version
  })
}

