package pilot.browser
import java.lang.{ProcessBuilder,Process => JProcess}
import java.io.{File,BufferedReader,InputStreamReader,BufferedWriter,OutputStreamWriter}

object Process {
  val Serving = """.*Serving: (http://\S+).*""".r
  private var servers: List[String] = Nil
  def pilot(path: File) = {
    val process = new ProcessBuilder("sbt", "pilot").directory(path).start()
    /* @tailrec */ 
    def handle(reader: BufferedReader): Option[String] =
      reader.readLine() match {
        case null => None
        case Serving(loc) => 
          servers = loc :: servers
          Some(loc)
        case _ => handle(reader)
      }
    handle(new BufferedReader(new InputStreamReader(process.getInputStream)))
  }
  def stop() {
    import dispatch._
    import Http._
    servers foreach { s =>
      try { (new Http)(s << Map("action" -> "Exit") >|) } catch {
        case e: java.net.ConnectException => ()
      }
    }
  }
}
