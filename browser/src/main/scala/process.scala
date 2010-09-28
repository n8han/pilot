package pilot.browser
import java.lang.ProcessBuilder
import java.io.{File,BufferedReader,InputStreamReader}

object Process {
  val Serving = """.*Serving: (http://\S+).*""".r
  def pilot(path: File) = {
    val process = new ProcessBuilder("sbt", "pilot").directory(path).start()
    /* @tailrec */ def handle(reader: BufferedReader): Option[String] =
      reader.readLine() match {
        case null => None
        case Serving(loc) => Some(loc)
        case _ => handle(reader)
      }
    handle(new BufferedReader(new InputStreamReader(process.getInputStream)))
  }
}
