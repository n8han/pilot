package pilot

import java.io.File

object Directory {
  def dir(f: File) = f.isDirectory && !f.getName.startsWith(".")
  def project(f: File) = dir(f) && new File(f, "project/build.properties").exists
  def name(f: File) = if (f.getName == "") "/" else f.getName
}
class PathExtract(predicate: File => Boolean) {
  def unapply(path: String) = Some(new File(path)).filter(predicate)
}
object LocalPath extends PathExtract(Directory.dir)
object ProjectPath extends PathExtract(Directory.project)
