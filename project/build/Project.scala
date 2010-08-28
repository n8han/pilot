import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  val uf_version = "0.1.4"
  
  // unfiltered
  lazy val uf = "net.databinder" %% "unfiltered-server" % uf_version

  lazy val sbt_io = "org.scala-tools.sbt" %% "io" % "0.7.4"
  val databinder_repo = Resolver.url("Databinder Repository") artifacts
    "http://databinder.net/repo/[organization]/[module]/[revision]/[type]s/[artifact](-[classifier]).[ext]"

  // testing
  lazy val uf_spec = "net.databinder" %% "unfiltered-spec" % uf_version % "test"
}
