import sbt._

class Project(info: ProjectInfo) extends ProcessorProject(info) {
  val uf_version = "0.1.5-SNAPSHOT"
  
  // unfiltered
  lazy val uf = "net.databinder" %% "unfiltered-server" % uf_version
  
  lazy val df = "net.databinder" %% "dispatch-futures" % "0.7.5"

  // testing
  lazy val uf_spec = "net.databinder" %% "unfiltered-spec" % uf_version % "test"
}

