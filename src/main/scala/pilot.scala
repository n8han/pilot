package pilot

import unfiltered.request._
import unfiltered.response._
import unfiltered.server.Http

class Processor extends sbt.processor.BasicProcessor {
  val port = 8080
  def apply(p: sbt.Project, s: String) = {
    (p) match {
      case (p: sbt.BasicScalaProject) =>
        val s = Http(port);
        s.filter(new Pilot(p,s)).start()
        val loc = "http://127.0.0.1:%d/" format port
        try {
          import java.net.URI
          val dsk = Class.forName("java.awt.Desktop")
          dsk.getMethod("browse", classOf[URI]).invoke(
            dsk.getMethod("getDesktop").invoke(null), new URI(loc)
          )
        } catch { case e => throw(e) }
        p.log.info("Started Pilot at " + loc)
        s.server.join()
        p.log.info("Finished pilot")
      case _ =>
        p.log.error("Can only pilot a BasicScalaProject")
    }
  }
}

class Pilot(p: sbt.BasicScalaProject, server: Http) extends unfiltered.Plan {
  import dispatch.futures.DefaultFuture._
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
    def apply() { future { Thread.sleep(500); server.stop() } }
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
