package just.toss

import hedgehog._
import hedgehog.runner._
import org.scalajs.dom.URL

import scala.scalajs.js.URIUtils
import scala.util.Try

object RedirectSpec extends Properties {

  private val Base = "https://just-toss.site/"

  /** What the browser does with the page. */
  sealed trait Outcome
  object Outcome {
    case object NoRedirect               extends Outcome
    final case class GoTo(href: String) extends Outcome

    /** Setting `location.href` to an invalid URL throws an error, so the browser stays on the page. */
    case object Fails extends Outcome
  }

  private def locationOf(suffix: String): String = new URL(Base + suffix).href

  private def outcome(location: String): Outcome =
    Redirect
      .target(Redirect.request(location))
      .fold[Outcome](Outcome.NoRedirect)(site =>
        Try(new URL(site, location).href).fold(_ => Outcome.Fails, Outcome.GoTo(_))
      )

  /** The expected outcomes are the outcomes of the previous implementation with `scala-uri`,
    * except for the inputs marked with "scala-uri failed", where it threw an exception.
    */
  private val examples: List[(String, Outcome)] = {
    import Outcome._
    List(
      ""                                                       -> NoRedirect,
      "#"                                                      -> NoRedirect,
      "#%20"                                                   -> NoRedirect,
      "#example.com"                                           -> GoTo("https://example.com/"),
      "#http://example.com"                                    -> GoTo("http://example.com/"),
      "#https://example.com"                                   -> GoTo("https://example.com/"),
      "#HTTPS://example.com"                                   -> GoTo("https://https//example.com"),
      "#example.com/path?q=1&r=2#frag"                         -> GoTo("https://example.com/path?q=1&r=2#frag"),
      "#example.com:8080/a/b"                                  -> GoTo("https://example.com:8080/a/b"),
      "#localhost:8080"                                        -> GoTo("https://localhost:8080/"),
      "#user:pass@example.com"                                 -> GoTo("https://user:pass@example.com/"),
      "#ftp://example.com"                                     -> GoTo("https://ftp//example.com"),
      "#mailto:a@b.com"                                        -> GoTo("https://mailto:a@b.com/"),
      "#javascript:alert(1)"                                   -> Fails,
      "#//example.com"                                         -> GoTo("https://example.com/"),
      "#/path"                                                 -> GoTo("https://path/"),
      "#example.com/a%20b"                                     -> GoTo("https://example.com/a%20b"),
      "#example.com/a b"                                       -> GoTo("https://example.com/a%20b"),
      "#example.com/%2F"                                       -> GoTo("https://example.com//"),
      "#example.com/?q=a+b"                                    -> GoTo("https://example.com/?q=a+b"),
      "#example.com/?q=a%2Bb"                                  -> GoTo("https://example.com/?q=a+b"),
      "#example.com/한글"                                        -> GoTo("https://example.com/%ED%95%9C%EA%B8%80"),
      "#한국.kr"                                                 -> GoTo("https://xn--3e0b707e.kr/"),
      "#example.com/%E3%81%82"                                 -> GoTo("https://example.com/%E3%81%82"),
      // scala-uri failed
      "#example.com/%"                                         -> GoTo("https://example.com/%"),
      // scala-uri failed
      "#example.com/%zz"                                       -> GoTo("https://example.com/%zz"),
      "#https://example.com/?redirect=https%3A%2F%2Fother.com" -> GoTo("https://example.com/?redirect=https://other.com"),
      "#example.com/path#inner"                                -> GoTo("https://example.com/path#inner"),
      "#  example.com  "                                       -> Fails,
      "#example .com"                                          -> Fails,
      "#httpexample.com"                                       -> GoTo("https://httpexample.com/"),
      "#http:example.com"                                      -> GoTo("http://example.com/"),
      "#https:/example.com"                                    -> GoTo("https://just-toss.site/example.com"),
      "?debug=true#example.com"                                -> NoRedirect,
      "?debug=false#example.com"                               -> GoTo("https://example.com/"),
      "?debug=TRUE#example.com"                                -> GoTo("https://example.com/"),
      "?debug=true&debug=false#example.com"                    -> NoRedirect,
      "?debug=false&debug=true#example.com"                    -> GoTo("https://example.com/"),
      "?debug#example.com"                                     -> GoTo("https://example.com/"),
      "?debug=tr%75e#example.com"                              -> NoRedirect,
      "?debug=true+#example.com"                               -> GoTo("https://example.com/"),
      "?a=1&debug=true#example.com"                            -> NoRedirect,
      "?debug=true"                                            -> NoRedirect,
      "#example.com?x=1"                                       -> GoTo("https://example.com/?x=1"),
      // scala-uri failed
      "#[::1]:8080"                                            -> GoTo("https://[::1]:8080/"),
      "#example.com\\path"                                     -> GoTo("https://example.com/path"),
    )
  }

  private val genDomain: Gen[String] =
    Gen
      .string(Gen.lower, Range.linear(1, 10))
      .list(Range.linear(1, 3))
      .map(labels => (labels :+ "com").mkString("."))

  private val genPath: Gen[String] =
    Gen.string(Gen.alphaNum, Range.linear(0, 10)).list(Range.linear(0, 3)).map(_.map("/" + _).mkString)

  /** Any characters except surrogates, so `encodeURIComponent` can encode them. */
  private val genText: Gen[String] =
    Gen.string(Gen.unicode.filter(c => !Character.isSurrogate(c)), Range.linear(0, 30))

  override def tests: List[Test] =
    examples.map { case (suffix, expected) =>
      example(s"$Base$suffix", outcome(locationOf(suffix)) ==== expected)
    } ++ List(
      property("A domain without a scheme redirects to https", propDomainWithoutScheme),
      property("A site with http or https keeps its scheme", propHttpScheme),
      property("debug=true never redirects", propDebugMode),
      property("Any fragment never throws an exception", propAnyFragment),
      property("percentDecode decodes what encodeURIComponent encodes", propPercentDecode),
    )

  def propDomainWithoutScheme: Property =
    for {
      domain <- genDomain.forAll
      path   <- genPath.forAll
    } yield outcome(locationOf(s"#$domain$path")) ==== Outcome.GoTo(s"https://$domain${if (path.isEmpty) "/" else path}")

  def propHttpScheme: Property =
    for {
      scheme <- Gen.element1("http", "https").forAll
      domain <- genDomain.forAll
      path   <- genPath.forAll
    } yield outcome(locationOf(s"#$scheme://$domain$path")) ==== Outcome.GoTo(
      s"$scheme://$domain${if (path.isEmpty) "/" else path}"
    )

  def propDebugMode: Property =
    for {
      fragment <- genText.forAll
    } yield Redirect.target(Redirect.request(locationOf(s"?debug=true#${URIUtils.encodeURIComponent(fragment)}"))) ==== None

  def propAnyFragment: Property =
    for {
      fragment <- genText.forAll
    } yield Result.assert(Try(Redirect.request(locationOf(s"#$fragment"))).isSuccess)

  def propPercentDecode: Property =
    for {
      text <- genText.forAll
    } yield Redirect.percentDecode(URIUtils.encodeURIComponent(text)) ==== text
}
