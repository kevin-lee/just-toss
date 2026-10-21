package just.toss

import org.scalajs.dom

object Main {

  def main(args: Array[String]): Unit =
    dom.window.onload = event => {
      val request = Redirect.request(dom.window.location.href)

      println(
        s"""      event: $event
           |   location: ${request.location}
           |   fragment: ${request.fragment}
           |  debugMode: ${request.debugMode}
           |    newSite: ${request.site}
           |""".stripMargin)

      Redirect.target(request).foreach(site => dom.window.location.href = site)
    }
}
