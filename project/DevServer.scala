import com.sun.net.httpserver.{HttpExchange, HttpServer}

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicReference

/** A static file server for development. It serves the files in `root` without caching. */
object DevServer {

  private val contentTypes = Map(
    "html" -> "text/html; charset=utf-8",
    "js"   -> "text/javascript; charset=utf-8",
    "map"  -> "application/json; charset=utf-8",
    "json" -> "application/json; charset=utf-8",
    "ico"  -> "image/x-icon",
  )

  private val running = new AtomicReference[Option[HttpServer]](None)

  def start(root: Path, host: String, port: Int): Unit = {
    stop()
    val absoluteRoot = root.toAbsolutePath.normalize
    val server       = HttpServer.create(new InetSocketAddress(host, port), 0)
    server.createContext("/", (exchange: HttpExchange) => serve(absoluteRoot, exchange))
    server.start()
    running.set(Some(server))
  }

  def stop(): Unit =
    running.getAndSet(None).foreach(_.stop(0))

  private def serve(root: Path, exchange: HttpExchange): Unit = {
    val requested = exchange.getRequestURI.getPath.stripPrefix("/") match {
      case ""   => "index.html"
      case path => path
    }
    val file      = root.resolve(requested).normalize

    val (status, contentType, body) =
      if (file.startsWith(root) && Files.isRegularFile(file))
        (200, contentTypes.getOrElse(extension(file), "application/octet-stream"), Files.readAllBytes(file))
      else
        (404, "text/plain; charset=utf-8", "Not Found".getBytes(StandardCharsets.UTF_8))

    exchange.getResponseHeaders.set("Content-Type", contentType)
    exchange.getResponseHeaders.set("Cache-Control", "no-store")
    exchange.sendResponseHeaders(status, body.length.toLong)
    exchange.getResponseBody.write(body)
    exchange.close()
  }

  private def extension(file: Path): String = {
    val name = file.getFileName.toString
    name.substring(name.lastIndexOf('.') + 1)
  }
}
