package pilot

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Http

import java.io.File
import dispatch.futures.DefaultFuture._

class Processor extends sbt.processor.BasicProcessor {
  def apply(p: sbt.Project, s: String) = {
    val s = Http.anylocal.resources(pilot.Shared.resources)
    s.filter(new Pilot(p,s)).run { server =>
      println("Serving: " + server.url )
    }
  }
}

class Pilot(project: sbt.Project, server: Http) extends unfiltered.filter.Plan {
  object LocalPath extends PathExtract(project.info.projectPath.asFile, Directory.file)
  object Flyable {
    def unapply(p: File) = flyable(p) map { (p, _) }
  }

  def intent = {
    case GET(Path(LocalPath(path),_)) => 
      page(path)
    case POST(Params(Action("Exit",_),_)) => 
      future { Thread.sleep(500); server.stop() }
      ResponseString("Exited")
    case POST(Path(LocalPath(Flyable(path, flyproj)),Params(Action(name,params),_))) =>
      params match {
        case Contents(c, _) => sbt.FileUtilities.write(path, c, project.log)
        case _ => ()
      }
      Buttons.all.get(name).foreach { _(flyproj) }
      ResponseString(name)
  }
  def page(file: File) = {
    val path = if (file.isDirectory) file else new File(file.getParent)
    pilot.Shared.page(
      <div class="prepend-top span-22 last">
        <h1>{ 
          (Seq(project.name) ++ 
            flyable(path).filter { _ != project }.map(_.name)
          ).mkString(": ") 
        }</h1>
      </div>
      <div class="prepend-top span-6">
        <form method="POST" class="controls">
          <input type="image" src="/img/Exit.png" name="action" value="Exit" />{ 
            flyable(path).toList.flatMap { _ =>
              Buttons.all.values.toList.map { _.html }
            }
          }<img src="/img/plane.png" class="plane" />
        </form>
        <ul class="directory">{ children_li(path) }</ul>
      </div>
      <div class="prepend-top span-16 last"> {
        Seq(file).filter { !_.isDirectory } flatMap { file =>
          sbt.FileUtilities.readString(file, project.log).right.toSeq.map { str =>
            <h3>{ file.getName }</h3>
            <textarea>{ str }</textarea>
          }
        }
      } </div>
    )
  }
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
      <li class="file"><a href={relative(f)}>{ f.getName }</a></li>
    }
  }
}

object Action extends Params.Extract("action", Params.first ~> Params.nonempty)
object Contents extends Params.Extract("contents", Params.first)

object Buttons {
  import sbt.{BasicScalaProject=>BSP}

  abstract class Button(val name: String) extends (BSP => Unit) {
    val png = "/img/%s.png".format(name)
    val html = <input type="image" src={ png } name="action" value={name} />
  }
  object Compile extends Button("Compile") {
    def apply(proj: BSP) { proj.compile.run }
  }
  object Run extends Button("Run") {
    def apply(proj: BSP) {
      val error = proj.compile.run
      error getOrElse {
        future { proj.run.apply(Array()).run }
        "Running..."
      }
    }
  }
  val all = (Map.empty[String, Button] /: (
    Run :: Compile :: Nil
  )) { (m, a) => m + (a.name -> a) }
}
