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
  object LocalPath extends PathExtract(project.info.projectPath.asFile, Directory.dir)

  import dispatch.futures.DefaultFuture._
  def intent = {
    case GET(Path(LocalPath(path),_)) => pilot.Shared.page(
      <div class="prepend-top span-15 append-5 last">
        <h1>{ project.name }</h1>
        <form method="POST">
          <input type="submit" name="action" value="Exit" />
        </form>
        <ul class="directory">{
          import java.io.File
          val (dirs,files) = Directory.children(path).filter { f =>
            f.getName match {
              case "lib_managed" | "target" => false
              case name if name.startsWith(".") => false
              case _ => true
            }
          }.partition(Directory.dir)
          val project_path = project.info.projectPath.asFile.getCanonicalPath
          val prefix = project_path.length
          def relative(d: File) = d.getCanonicalPath match {
            case `project_path` => "/"
            case full => full.substring(prefix)
          }
          val parent_li = Some(path).filter { 
            _.getCanonicalPath != project_path
          }.map { d => new File(d.getParent) }.map { d =>
            <li class="parent"> <a href={ relative(d) }>{ d.getName }</a> </li>
          }
          val dirs_li = dirs map { d =>
            <li class="dir"> <a href={ relative(d) }>{ d.getName }</a> </li>
          }
          val files_li = files map { f =>
            <li class="file">{ f.getName }</li>
          }
          parent_li ++ dirs_li ++ files_li
        }</ul>

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
