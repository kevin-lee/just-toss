package just.toss

import org.scalajs.dom.URL

import java.nio.charset.StandardCharsets
import scala.util.matching.Regex

/** Finds the site to redirect to from the location of the page.
  *
  * The site is in the fragment of the location (e.g. `https://just-toss.site/#example.com`).
  * If the query has `debug=true`, there is no redirect.
  */
object Redirect {

  final case class Request(location: String, fragment: String, debugMode: Boolean, site: Option[String])

  /** One or more percent-encoded bytes (e.g. `%ED%95%9C`). */
  private val PercentEncodedBytes = "(?:%[0-9A-Fa-f]{2})+".r

  private val Scheme = "^([A-Za-z][A-Za-z0-9+.-]*):".r

  /** Decodes percent-encoded UTF-8 bytes.
    *
    * Invalid UTF-8 bytes become U+FFFD (the replacement character), and a `%` that is not followed by
    * two hex digits stays as it is, so this never fails.
    */
  def percentDecode(value: String): String =
    PercentEncodedBytes.replaceAllIn(
      value,
      encoded =>
        Regex.quoteReplacement(
          new String(
            encoded.matched.grouped(3).map(hex => Integer.parseInt(hex.drop(1), 16).toByte).toArray,
            StandardCharsets.UTF_8,
          )
        ),
    )

  /** Adds `https://` to the site if it does not have a scheme that starts with `http`. */
  def toSite(fragment: String): String =
    if (Scheme.findFirstMatchIn(fragment).exists(_.group(1).startsWith("http"))) fragment
    else s"https://$fragment"

  def request(location: String): Request = {
    val url       = new URL(location)
    val fragment  = percentDecode(url.hash.stripPrefix("#"))
    val debugMode = Option(url.searchParams.get("debug")).contains("true")
    Request(location, fragment, debugMode, Option(fragment).filter(_.trim.nonEmpty).map(toSite))
  }

  /** The site to redirect to. It is `None` if there is no site or if it is in debug mode. */
  def target(request: Request): Option[String] =
    request.site.filter(_ => !request.debugMode)
}
