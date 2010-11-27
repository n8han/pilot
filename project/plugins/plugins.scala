import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val coffeeScriptSbtRepo = "coffeeScript sbt repo" at "http://repo.coderlukes.com/"
  val coffeeScript = "org.coffeescript" % "coffee-script-sbt-plugin" % "0.9.0"
  val jcoffeescript = "org.jcoffeescript" % "jcoffeescript" % "0.9.0" from
    "http://cloud.github.com/downloads/yeungda/jcoffeescript/jcoffeescript-0.9.0.jar"
  val g8lib = "net.databinder" %% "giter8-library" % "0.2.0-SNAPSHOT"
}
