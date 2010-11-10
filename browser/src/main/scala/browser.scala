package pilot.browser

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Http

import java.io.File

/** unfiltered plan */
class Browser(server: Http) extends unfiltered.filter.Plan {
  import Directory._
  def intent = {
    case GET(Path("/loading",_)) => pilot.Shared.page(
      <div class="prepend-top"><h1><em>Loading…</em></h1></div>
    )
    case GET(Path(FullPath(path), Jsonp(wrapper, _))) =>
      val result = Process.pilot(path).getOrElse("fail")
      import net.liftweb.json.JsonAST._
      import net.liftweb.json.JsonDSL._
      ResponseString(wrapper.wrap(pretty(render(result))))
    case GET(Path(FullPath(path),_)) => pilot.Shared.page(
      <div class="prepend-top span-22">
        <h1>{ name(path) }</h1>
        <ul class="directory"> {
          val (projs, dirs) = children(path).filter(dir).partition(project)
          def opt[T](t: T) = if (t == null) None else Some(t)
          val all = opt(path.getParent).map { n => (new File(n), "parent") }.toSeq ++
                    projs.map { p => (p, "project") } ++
                    dirs.map { d => (d, "dir") }
          for ((d, cls) <- all) yield 
            <li class={ cls }> <a href={ d.getCanonicalPath }>{ name(d) }</a> </li>
        } </ul>
      </div>
    )
  }
}

/** embedded server */
object BrowserServer {
  def main(args: Array[String]) {
    val server = Http.anylocal.resources(pilot.Shared.resources)
    server.filter(new Browser(server)).run({ server =>
      val home = System.getProperty("user.home")
      val loc = server.url + home.substring(1)
      unfiltered.util.Browser.open(loc) foreach { exc =>
        println("Started Pilot at " + loc)
      }
    }, { _ => Process.stop() })
  }
}

class BrowserLauncher extends xsbti.AppMain {
  def run(configuration: xsbti.AppConfiguration) = {
    BrowserServer.main(Array())
    new Exit(0)
  }
  class Exit(val code: Int) extends xsbti.Exit
}
