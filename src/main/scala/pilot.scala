import unfiltered.request._
import unfiltered.response._

class Pilot extends unfiltered.Plan {
  abstract class Button(val name: String) extends (() => Unit) {
    val html = <input type="submit" name="action" value={name} />
  }
  object Compile extends Button("Compile") {
    def apply() { println("compile it") }
  }
  object Run extends Button("Run") {
    def apply() { println("rern") }
  }
  object Exit extends Button("Exit") {
    def apply() { println("xit") }
  }
  val buttons = (Map.empty[String, Button] /: (
    Compile :: Run :: Exit :: Nil
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

object Runner {
  def main(args: Array[String]) {
    unfiltered.server.Http(8080).filter(new Pilot).run
  }
}
