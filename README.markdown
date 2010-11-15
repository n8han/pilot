What is Pilot?
--------------

Pilot is an experimental tool for writing software. It provides a web
browser interface for finding, editing, compiling, and running source
files. Its minimalistic interface is inspired by the Processing
development environment.

[Simple Build Tool][sbt] provides Pilot's build infrastructure while
a [Unfiltered][uf] locally serves its web interface.

[sbt]: http://code.google.com/p/simple-build-tool/
[uf]: https://github.com/n8han/Unfiltered

How to use?
-----------

Download the [Pilot application package][app]. On Mac OS X, it
contains a double-clickable application. Open it and after some time a
web browser will open with Pilot showing the current directory. There
may be a lengthy delay as various libraries are downloaded.

You'll notice a spde-examples folder next to the application. These
samples from the [Spde][spde] project are provided to give you
something to do with Pilot. If you select spde-examples in the Pilot
browser a new window will open to work with that project, which is
composed of many sub-projects. Pilot has to download all of the
dependencies of these projects before you can interact with them, so
again there may be a lengthy delay. (Adding some user feedback to this
process is Pilot's [issue #1][issue]!)

[app]: https://github.com/downloads/n8han/pilot/Pilot-0.1.1.zip
[spde]: http://technically.us/spde/
[issue]: https://github.com/n8han/pilot/issues/issue/1

Once the project interaction window has loaded, you can browse within
the project to open and edit files. If there are sub-projects, you
must be within one of these in order to run a program. When you see
what looks like a "play" button, click it.

###Hey, (fellow) Linux users!

Pilot works just fine in Linux. Just run the start script inside the
app bundle, `Pilot.app/Contents/MacOS/pilot`.
