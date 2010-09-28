package pilot.browser
import java.lang.{ProcessBuilder,Process => JProcess}
import java.io.{File,BufferedReader,InputStreamReader}

object Process {
  val Serving = """.*Serving: (http://\S+).*""".r
  private var processes: List[JProcess] = Nil
  def pilot(path: File) = {
    val process = new ProcessBuilder("sbt", "pilot").directory(path).start()
    processes = process :: processes
    /* @tailrec */ 
    def handle(reader: BufferedReader): Option[String] =
      reader.readLine() match {
        case null => None
        case Serving(loc) => Some(loc)
        case _ => handle(reader)
      }
    handle(new BufferedReader(new InputStreamReader(process.getInputStream)))
  }
}
