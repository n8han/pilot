package pilot

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Http

import java.io.File

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
  object Flyable {
    def unapply(p: File) = flyable(p) map { (p, _) }
  }

  import dispatch.futures.DefaultFuture._
  def intent = {
    case GET(Path(LocalPath(path),_)) => 
      page(path)
    case POST(Params(Buttons.Action("Exit",_),_)) => 
      future { Thread.sleep(500); server.stop() }
      ResponseString("Exited")
    case POST(Path(LocalPath(Flyable(path, flyproj)), 
                   Params(Buttons.Action(name,_),_))) => 
      Buttons.all.get(name).foreach { _(flyproj) }
      page(path)
  }
  def page(path: File) =
    pilot.Shared.page(
      <div class="prepend-top span-15 append-5 last">
        <h1>{ project.name }</h1>
        <form method="POST">
          <input type="submit" name="action" value="Exit" />
          { 
            flyable(path).toList.flatMap { _ =>
              Buttons.all.values.toList.map { _.html }
            }
          }
        </form>
        <ul class="directory">{ children_li(path) }</ul>
      </div>
    )
  def flyable(path: File): Option[sbt.BasicScalaProject] =
    project match {
      case project: sbt.BasicScalaProject => Some(project)
      case project => 
        val cur = path.getAbsolutePath
        project.subProjects.values.find { p =>
          cur.startsWith(p.info.projectPath.absolutePath)
        }.flatMap {
          case p: sbt.BasicScalaProject => Some(p)
          case _ => None
        }
    }
  def children_li(path: File) = {
    val (dirs,files) = Directory.children(path).filter { f =>
      f.getName match {
        case "lib_managed" | "src_managed" | "target" => false
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
  }
}

object Buttons {
  import sbt.{BasicScalaProject=>BSP}

  object Action extends Params.Extract("action", Params.first ~> Params.nonempty)
  
  abstract class Button(val name: String) extends (BSP => Unit) {
    val html = <input type="submit" name="action" value={name} />
  }
  object Compile extends Button("Compile") {
    def apply(proj: BSP) { proj.compile.run }
  }
  object Run extends Button("Run") {
    def apply(proj: BSP) { proj.run.apply(Array()).run }
  }
  object Clean extends Button("Clean") {
    def apply(proj: BSP) { proj.clean.run }
  }
  val all = (Map.empty[String, Button] /: (
    Compile :: Run :: Clean :: Nil
  )) { (m, a) => m + (a.name -> a) }
}
