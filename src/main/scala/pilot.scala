package pilot

import unfiltered.request._
import unfiltered.response._
import unfiltered.server.Http

class Processor extends sbt.processor.BasicProcessor {
  def apply(project: sbt.Project, args: String) {
    project match {
      case p: sbt.BasicScalaProject =>
        val server = Http(8080)
        server.filter(new Pilot(p,server)).run
      case _ => project.log.error("Can only pilot a BasicScalaProject")
    }
  }
}

class Pilot(p: sbt.BasicScalaProject, server: Http) extends unfiltered.Plan {
  abstract class Button(val name: String) extends (() => Unit) {
    val html = <input type="submit" name="action" value={name} />
  }
  object Compile extends Button("Compile") {
    def apply() { p.compile.run }
  }
  object Run extends Button("Run") {
    def apply() { p.run(Array()) }
  }
  object Clean extends Button("Clean") {
    def apply() { p.clean.run }
  }
  object Exit extends Button("Exit") {
    def apply() { 
      try { server.stop() } catch {
        case exc: InterruptedException => ()
      }
    }
  }
  val buttons = (Map.empty[String, Button] /: (
    Compile :: Run :: Clean :: Exit :: Nil
  )) { (m, a) => m + (a.name -> a) }
  object Action extends Params.Extract("action", Params.first ~> Params.nonempty)
  val action_panel = new Html(
    <html>
      <form method="POST">
        { buttons.values.map { _.html } }
      </form>
    </html>
  )
  def filter = {
    case GET(Path("/",_)) => action_panel
    case POST(Path("/", Params(Action(name,_),_))) => 
      buttons.get(name).foreach { _() }
      action_panel
  }
}
