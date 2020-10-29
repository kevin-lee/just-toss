package just.toss

import org.scalajs.dom
import org.scalajs.dom.raw.Event

import scala.scalajs.js.annotation.JSExportTopLevel

object Main {

  @JSExportTopLevel("main")
  def main(): Unit = {

    val relocate: Event => Unit = event => {
      import io.lemonlabs.uri._
      val location = dom.window.location.href

      val uri = Url.parse(location)
      val paramMap = uri.query.paramMap
      val newSite = for {
        paramValue <- paramMap.get("site")
        site <- paramValue.headOption
        if site.trim.nonEmpty // String.isBlank is not available in Scala.js
      } yield Url.parse(site)

      println(
        s"""      event: $event
           |   location: $location
           |        uri: $uri
           |uri - query: ${uri.removeQueryString()}
           |   paramMap: ${uri.query.paramMap}
           |    newSite: ${newSite.map(_.toString)}
           |""".stripMargin)
      newSite.map(_.toString).fold(())(dom.window.location.href = _)
    }

    dom.window.onload = relocate
  }
}
