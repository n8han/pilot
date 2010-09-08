package pilot.browser

import unfiltered.request._
import unfiltered.response._
import unfiltered.server.Http

import java.io.File
import java.lang.ProcessBuilder

/** unfiltered plan */
class Browser(server: Http) extends unfiltered.Plan {
  def dir(f: File) =
    f.isDirectory && !f.getName.startsWith(".")

  def project(f: File) = dir(f) && new File(f, "project/build.properties").exists

  class PathExtract(predicate: File => Boolean) {
    def unapply(path: String) = Some(new File(path)).filter(predicate)
  }
  object LocalPath extends PathExtract(dir)
  object ProjectPath extends PathExtract(project)
  def filter = {
    case GET(Path(ProjectPath(path),_)) =>
      new ProcessBuilder("sbt", "pilot").directory(path).start()
      Redirect(path.getParent)
    case GET(Path(LocalPath(path),_)) => Html(
      <html>
      <head><style>{ "li.project a { color: orange }" }</style></head>
      <body>
        <ul> {
          val (projs, dirs) = path.list.toList.sortWith {
            _.toUpperCase > _.toUpperCase
          }.map { n =>
            new File(path, n)
          }.filter(dir).partition(project)
          val all = projs.map { p => (p, "project") } ++
                    dirs.map { d => (d, "dir") }
          for ((d, cls) <- all) yield 
            <li class={ cls }> <a href={ d.getAbsolutePath }>{ d.getName }</a> </li>
          }
        </ul>
      </body></html>
    )
  }
}

/** embedded server */
object Server {
  def main(args: Array[String]) {
    val port = 8080
    val res = new java.net.URL(getClass.getResource("/web/robots.txt"), ".")
    val server = Http(port).resources(res)
    server.filter(new Browser(server)).run { server =>
      val loc = "http://127.0.0.1:%d" format server.port
      println("Pilot started at " + loc)
      val home = System.getProperty("user.home")
      try {
        import java.net.URI
        val dsk = Class.forName("java.awt.Desktop")
        dsk.getMethod("browse", classOf[URI]).invoke(
          dsk.getMethod("getDesktop").invoke(null), new URI(loc + home)
        )
      } catch { case e => () }
    }
  }
}
