package pilot

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Http

class Processor extends sbt.processor.BasicProcessor {
  def apply(p: sbt.Project, s: String) = {
    (p) match {
      case (p: sbt.BasicScalaProject) =>
        val s = Http(unfiltered.Port.any).resources(pilot.Shared.resources)
        s.filter(new Pilot(p,s)).run { server =>
          println("Serving: http://127.0.0.1:%d/" format server.port )
        }
      case _ =>
        p.log.error("Can only pilot a BasicScalaProject")
    }
  }
}

class Pilot(project: sbt.BasicScalaProject, server: Http) extends
    unfiltered.filter.Plan {
  import dispatch.futures.DefaultFuture._
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
  object Exit extends Button("Exit") {
    def apply() { future { Thread.sleep(500); server.stop() } }
  }
  val buttons = (Map.empty[String, Button] /: (
    Compile :: Run :: Clean :: Exit :: Nil
  )) { (m, a) => m + (a.name -> a) }
  object Action extends Params.Extract("action", Params.first ~> Params.nonempty)
  val action_panel = pilot.Shared.page(
    <h1> { project.name } </h1>
    <form method="POST">
      { buttons.values.map { _.html } }
    </form>
  )
  def intent = {
    case GET(Path("/",_)) => action_panel
    case POST(Path("/", Params(Action(name,_),_))) => 
      buttons.get(name).foreach { _() }
      action_panel
  }
}
