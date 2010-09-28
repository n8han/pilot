package pilot

import unfiltered.response._

object Shared {
  def resources = new java.net.URL(getClass.getResource("/web/robots.txt"), ".")

  def page(content: scala.xml.NodeSeq) = Html(
    <html>
      <head>
        <link rel="stylesheet" href="/css/blueprint/screen.css" type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href="/css/blueprint/print.css" type="text/css" media="print"/>
        <!--[if lt IE 8]><link rel="stylesheet" href="/css/blueprint/ie.css" type="text/css" media="screen, projection"/><![endif]-->
        <link rel="stylesheet" href="/css/pilot.css" type="text/css" />
        <script type="text/javascript" src="/js/jquery-1.4.2.min.js"></script>
        <script type="text/javascript" src="/js/browser.js"></script>
       </head>
      <body>
        <div class="container"> { content }</div>
      </body>
    </html>
  )
}
