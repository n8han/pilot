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
  def project_paths = project.subProjects.values.map { sp =>
    sp.info.projectPath.asFile.getCanonicalPath
  }
  def flyable(path: File): Option[sbt.BasicScalaProject] =
    project match {
      case project: sbt.BasicScalaProject => Some(project)
      case project => 
        val cur = path.getCanonicalPath
        project.subProjects.values.find { sp =>
          cur.startsWith(sp.info.projectPath.asFile.getCanonicalPath)
        }.flatMap {
          case p: sbt.BasicScalaProject => Some(p)
          case _ => None
        }
    }
  def children_li(path: File) = {
    val (all_dirs,files) = Directory.children(path).filter { f =>
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
    val parents = Some(path).filter { 
      _.getCanonicalPath != project_path
    }.map { d => new File(d.getParent) }
    val (subpjs, dirs) = all_dirs.partition { d =>
      project_paths.contains(d.getCanonicalPath)
    }
    ( parents.map { (_, "parent") } ++
      subpjs.map { (_, "subproject") } ++
      dirs.map { (_, "dir") }
    ).map { case (d, cl) =>
        <li class={ cl }> <a href={ relative(d) }>{ d.getName }</a> </li>
    } ++ files.map { f =>
      <li class="file">{ f.getName }</li>
    }
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
