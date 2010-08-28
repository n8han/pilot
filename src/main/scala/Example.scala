package com.example

import unfiltered.request._
import unfiltered.response._

/** unfiltered plan */
class App extends unfiltered.Plan {
  import QParams._
  def filter = {
    case GET(_) => view(Map.empty)(<p> What say you? </p>)
    case POST(Params(params, _)) =>
      val vw = view(params)_
      val expected = for { 
        int <- lookup("int") is int("That's not an integer") is required("An integer is required")
        palindrome <- lookup("palindrome") is trimmed is nonempty("Palindrome is empty") is(
          pred { s: String => s.toLowerCase.reverse == s.toLowerCase } ("That's not a palindrome")
        ) is required("A palindrome is required")
      } yield  vw(<p>Yup. { int.get } is an integer and { palindrome.get } is a palindrome. </p>)
      expected(params) orFail { fails =>
        vw(<ul> { fails.map { f => <li>{f.error} </li> } } </ul>)
      }
  }
  def view(params: Map[String, Seq[String]])(body: scala.xml.NodeSeq) = {
    def p(k: String) = params.get(k).flatMap { _.headOption } getOrElse("")
    Html(
     <html><body>
       { body }
       <form method="POST">
         Integer <input name="int" value={ p("int") } ></input>
         Palindrome <input name="palindrome" value={ p("palindrome") } />
         <input type="submit" />
       </form>
     </body></html>
   )
  }
}

/** embedded server */
object Server {
  def main(args: Array[String]) {
    import scala.actors.Actor._
    import java.net.ServerSocket
    import xsbt.IPC
    actor {
      val ss = new ServerSocket(32345)
      println("about to block")
      val socket = ss.accept()
      println("writing")
      val os = socket.getOutputStream
      os.write("compile\n".getBytes)
      os.close()
      println("closing")
      socket.close()
      println("closed")
      ss.close()
    }
    unfiltered.server.Http(8080).filter(new App).run
  }
}
