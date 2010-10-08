package pilot.browser

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Http

import java.io.File

/** unfiltered plan */
class Browser(server: Http) extends unfiltered.filter.Plan {
  import Directory._
  def intent = {
    case GET(Path(LocalPath(path), Jsonp(wrapper, _))) =>
      val result = Process.pilot(path).getOrElse("fail")
      import net.liftweb.json.JsonAST._
      import net.liftweb.json.JsonDSL._
      ResponseString(wrapper.wrap(pretty(render(result))))
    case GET(Path(LocalPath(path),_)) => pilot.Shared.page(
      <div class="prepend-top span-15 append-5 last">
        <h1>{ name(path) }</h1>
        <ul class="directory"> {
          val (projs, dirs) = path.list.toList.sort {
            _.toUpperCase < _.toUpperCase
          }.map { n =>
            new File(path, n)
          }.filter(dir).partition(project)
          def opt[T](t: T) = if (t == null) None else Some(t)
          val all = opt(path.getParent).map { n => (new File(n), "parent") }.toSeq ++
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
object BrowserServer {
  def main(args: Array[String]) {
    val server = Http(unfiltered.Port.any, "127.0.0.1").resources(pilot.Shared.resources)
    server.filter(new Browser(server)).run { server =>
      val home = System.getProperty("user.home")
      val loc = server.url + home.substring(1)
      unfiltered.Browser.open(loc) foreach { exc =>
        println("Started Pilot at " + loc)
      }
    }
    Process.stop()
  }
}
