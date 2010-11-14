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

  lazy val bundle = project("bundle", "Pilot App Bundle", new DefaultProject(_) {
    val sbt_launcher = "org.scala-tools" % "sbt-full-launcher" % "0.7.4" % 
      "provided->default" from 
      "http://simple-build-tool.googlecode.com/files/sbt-launch-0.7.4.jar"
    def launchSource = descendents(("src" / "main" / "bundle") ##, "*")
    def bundleOutput = outputPath / "Pilot.app" / "Contents"
    def runScript = bundleOutput / "MacOS" / "pilot"
    def infoplist = bundleOutput / "Info.plist"
    override def cleanAction = super.cleanAction dependsOn cleanTask(bundleOutput)
    lazy val bundle = task {
      import FileUtilities._
      bundleOutput.asFile.mkdirs()
      val launcher_jar = 
        (configurationPath(Configurations.Provided) * "*.jar").get.toList.firstOption
      val launcher_out = bundleOutput / "sbt-launcher.jar"
      val name = launcher_out.name
      (launcher_jar match {
        case Some(jar) => None
        case None => Some("Missing launcher jar, please `update`")
      }) orElse {
        copy(launchSource.get, bundleOutput, log).left.toOption
      } orElse {
        copyFile(launcher_jar.get, launcher_out, log)
        write((bundleOutput / "pilot.launchconfig").asFile, 
"""[app]
  version: %s
  org: net.databinder
  name: pilot-browser
  class: pilot.browser.BrowserLauncher
[scala]
  version: 2.7.7
[repositories]
  local
  maven-local
  scala-tools-releases
  maven-central
[boot]
  directory: boot
""" format (version), log)
      } orElse {
        write(runScript.asFile, 
"""#!/bin/sh
cd `dirname $0`/..
java -jar %s "*remove pilot"
java -jar %s "*pilot is net.databinder pilot-processor %s"
java -jar %s @pilot.launchconfig""" format (name, name, version, name), log)
      } orElse {
        write(infoplist.asFile, """
<plist version="1.0">
<dict>
<key>CFBundleExecutable</key>
<string>pilot</string>
<key>CFBundleIconFile</key>
<string>Default.icns</string>
<key>CFBundleIdentifier</key>
<string>net.databinder.pilot</string>
<key>CFBundleInfoDictionaryVersion</key>
<string>6.0</string>
<key>CFBundleName</key>
<string>Pilot</string>
<key>CFBundlePackageType</key>
<string>APPL</string>
<key>CFBundleShortVersionString</key>
<string>%s</string>
<key>CFBundleVersion</key>
<string>%s</string>
</dict>
</plist>""" format (version, version), log)
      } orElse {
        import Process._
        Some("Unable to make executable").filter { _ =>
          0 != ("chmod a+x " + runScript !)
        }
      }
    }
  })
  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
}
