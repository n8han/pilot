package pilot

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Http

class Processor extends sbt.processor.BasicProcessor {
  def apply(p: sbt.Project, s: String) = {
    val s = Http(unfiltered.Port.any, "127.0.0.1").resources(pilot.Shared.resources)
    s.filter(new Pilot(p,s)).run { server =>
      println("Serving: " + server.url )
    }
  }
}

class Pilot(project: sbt.Project, server: Http) extends unfiltered.filter.Plan {
  import dispatch.futures.DefaultFuture._
  def intent = {
    case GET(Path("/",_)) => pilot.Shared.page(
      <div class="prepend-top span-15 append-5 last">
        <h1>{ project.name }</h1>
        <form method="POST">
          <input type="submit" name="action" value="Exit" />
        </form>
      </div>
    )
    case POST(Path("/", Params(Action("Exit",_),_))) => 
      future { Thread.sleep(500); server.stop() }
      ResponseString("Exited")
  }
}
object Action extends Params.Extract("action", Params.first ~> Params.nonempty)

class Controls(project: sbt.BasicScalaProject, server: Http) {
  abstract class Button(val name: String) extends (() => Unit) {
    val html = <input type="submit" name="action" value={name} />
  }
  object Compile extends Button("Compile") {
    def apply() { project.compile.run }
  }
  object Run extends Button("Run") {
    def apply() { project.run(Array()) }
  }
  object Clean extends Button("Clean") {
    def apply() { project.clean.run }
  }
  val buttons = (Map.empty[String, Button] /: (
    Compile :: Run :: Clean :: Nil
  )) { (m, a) => m + (a.name -> a) }
  val action_panel =
    <span>{ buttons.values.map { _.html } }</span>

  def something[T]: PartialFunction[HttpRequest[T], scala.xml.NodeSeq] = {
    case GET(Path("/",_)) => action_panel
    case POST(Path("/", Params(Action(name,_),_))) => 
      buttons.get(name).foreach { _() }
      action_panel
  }
}
