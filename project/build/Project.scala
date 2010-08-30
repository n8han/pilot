import sbt._

class Project(info: ProjectInfo) extends ProcessorProject(info) {
  val uf_version = "0.1.4"
  
  // unfiltered
  lazy val uf = "net.databinder" %% "unfiltered-server" % uf_version

  val databinder_repo = Resolver.url("Databinder Repository") artifacts
    "http://databinder.net/repo/[organization]/[module]/[revision]/[type]s/[artifact](-[classifier]).[ext]"

  // testing
  lazy val uf_spec = "net.databinder" %% "unfiltered-spec" % uf_version % "test"
}

