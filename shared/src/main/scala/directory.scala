package pilot

import java.io.File

object Directory {
  def file(f: File) = f.exists && !f.getName.startsWith(".")
  def dir(f: File) = file(f) && f.isDirectory
  def project(f: File) = dir(f) && new File(f, "project/build.properties").exists
  def name(f: File) = if (f.getName == "") "/" else f.getName
  def children(path: File) = 
    path.list.toList.sort {
      _.toUpperCase < _.toUpperCase
    }.map { n =>
      new File(path, n)
    }
}

class PathExtract(base: File, predicate: File => Boolean) {
  def this(predicate: File => Boolean) = this(null, predicate)
  def unapply(path: String) = Some(new File(base, path)).filter(predicate)
}
object FullPath extends PathExtract(Directory.dir)
object ProjectPath extends PathExtract(Directory.project)
