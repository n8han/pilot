package pilot.browser

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Http

import java.io.File

/** unfiltered plan */
class Browser(server: Http) extends unfiltered.filter.Plan {
  def dir(f: File) = f.isDirectory && !f.getName.startsWith(".")
  def project(f: File) = dir(f) && new File(f, "project/build.properties").exists
  def name(f: File) = if (f.getName == "") "/" else f.getName

  class PathExtract(predicate: File => Boolean) {
    def unapply(path: String) = Some(new File(path)).filter(predicate)
  }
  object LocalPath extends PathExtract(dir)
  object ProjectPath extends PathExtract(project)
  def intent = {
    case GET(Path(LocalPath(path), Jsonp(wrapper, _))) =>
      val result = Process.pilot(path).getOrElse("fail")
      import net.liftweb.json.JsonAST._
      import net.liftweb.json.JsonDSL._
      ResponseString(wrapper.wrap(pretty(render(result))))
    case GET(Path(LocalPath(path),_)) => Browser.page(
      <div class="prepend-5 prepend-top span-10 append-5 last">
        <h1>{ name(path) }</h1>
        <ul class="directory"> {
          val (projs, dirs) = path.list.toList.sortWith {
            _.toUpperCase < _.toUpperCase
          }.map { n =>
            new File(path, n)
          }.filter(dir).partition(project)
          val all = Option(path.getParent).map { n => (new File(n), "parent") }.toSeq ++
                    projs.map { p => (p, "project") } ++
                    dirs.map { d => (d, "dir") }
          for ((d, cls) <- all) yield 
            <li class={ cls }> <a href={ d.getAbsolutePath }>{ name(d) }</a> </li>
          }
        </ul>
      </div>
    )
  }
}

/** embedded server */
object Browser {
  def page(content: scala.xml.NodeSeq) = Html(
    <html>
      <head>
        <link rel="stylesheet" href="/css/blueprint/screen.css" type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href="/css/blueprint/print.css" type="text/css" media="print"/>
        <!--[if lt IE 8]><link rel="stylesheet" href="/css/blueprint/ie.css" type="text/css" media="screen, projection"/><![endif]-->
        <link rel="stylesheet" href="/css/pilot.css" type="text/css" />
        <script type="text/javascript" src="/js/jquery-1.4.2.min.js"></script>
        <script type="text/javascript" src="/js/browser.js"></script>
       </head>
      <body>
        <div class="container"> { content }</div>
      </body>
    </html>
  )
      
  def main(args: Array[String]) {
    val port = 8080
    val res = new java.net.URL(getClass.getResource("/web/robots.txt"), ".")
    val server = Http(port).resources(res)
    server.filter(new Browser(server)).run { server =>
      val home = System.getProperty("user.home")
      val loc = "http://127.0.0.1:%d%s" format (server.port, home)
      unfiltered.Browser.open(loc) foreach { exc =>
        println("Started Pilot at " + loc)
      }
    }
  }
}
