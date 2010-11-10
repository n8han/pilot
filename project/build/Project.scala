import sbt._
import org.coffeescript.CoffeeScriptCompile

class Project(info: ProjectInfo) extends ParentProject(info) {
  val uf_version = "0.2.2"

  lazy val shared = project("shared", "Pilot Resources", 
                            new DefaultProject(_) with CoffeeScriptCompile 
  {
    def jsOutput = "src_managed" / "main" / "resources"
    override def cleanAction = super.cleanAction dependsOn cleanTask(jsOutput)
    override def coffeeScriptCompiledOuputDirectory = jsOutput.asFile.getPath
    override def mainResources = 
      super.mainResources +++ descendents(jsOutput ##, "*")
    override def compileAction = super.compileAction dependsOn compileCoffeeScript
    lazy val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
    lazy val uff = "net.databinder" %% "unfiltered-filter" % uf_version
    lazy val ufjs = "net.databinder" %% "unfiltered-json" % uf_version
  })
  val dispatch_version = "0.7.6"
  lazy val processor = project("processor", "Pilot Processor", new ProcessorProject(_) {
    // unfiltered
    lazy val df = "net.databinder" %% "dispatch-futures" % dispatch_version
    override def compileOptions = super.compileOptions ++ Seq(Unchecked)
  }, shared)
  lazy val browser = project("browser", "Pilot Browser", new DefaultProject(_) {
    lazy val df = "net.databinder" %% "dispatch-http" % dispatch_version
    val launchInterface = 
      "org.scala-tools.sbt" % "launcher-interface" % "0.7.4" % "provided"
    // commons-codec causes a strict dependency conflict in the launcher
    override def ivyXML =
      <dependencies> <exclude module="commons-codec" /> </dependencies>
  }, shared)
}

