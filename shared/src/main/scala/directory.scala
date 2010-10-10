package pilot

import java.io.File

object Directory {
  def dir(f: File) = f.isDirectory && !f.getName.startsWith(".")
  def project(f: File) = dir(f) && new File(f, "project/build.properties").exists
  def name(f: File) = if (f.getName == "") "/" else f.getName

  def ul(path: java.io.File) =
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
}

class PathExtract(predicate: File => Boolean) {
  def unapply(path: String) = Some(new File(path)).filter(predicate)
}
object LocalPath extends PathExtract(Directory.dir)
object ProjectPath extends PathExtract(Directory.project)
