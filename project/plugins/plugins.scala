import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val coffeeScriptSbtRepo = "coffeeScript sbt repo" at "http://repo.coderlukes.com/"
  val coffeeScript = "org.coffeescript" % "coffee-script-sbt-plugin" % "0.9.0"
}
