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
        hashVal <- uri.fragment
        if hashVal.trim.nonEmpty
        site = Url.parse(hashVal)
      } yield site

      val debugMode = (for {
        debugMode <- paramMap.get("debug")
        debugValue <- debugMode.headOption
      } yield debugValue).fold(false) {
        case "true" => true
        case _ => false
      }

      val toSiteString: Uri => String =
        uri =>
          uri.schemeOption
            .filter(_.startsWith("http"))
            .fold(s"https://${uri.toString}")(_ => uri.toString)

      println(
        s"""      event: $event
           |   location: $location
           |        uri: $uri
           |     schema: ${uri.schemeOption}
           | new schema: ${newSite.flatMap(_.schemeOption)}
           |      query: ${uri.query}
           |   fragment: ${uri.fragment}
           |   no query: ${uri.removeQueryString()}
           |   paramMap: ${uri.query.paramMap}
           |    newSite: ${newSite.map(toSiteString)}
           |""".stripMargin)
      newSite
        .filter(_ => !debugMode)
        .fold(())(uri => dom.window.location.href = toSiteString(uri))
    }

    dom.window.onload = relocate
  }
}
