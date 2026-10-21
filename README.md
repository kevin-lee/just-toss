# just-toss

A small Scala.js web page that redirects to the site in the URL fragment.

| URL | Result |
|---|---|
| `https://just-toss.site/#example.com` | Redirects to `https://example.com` |
| `https://just-toss.site/#http://example.com` | Redirects to `http://example.com` |
| `https://just-toss.site/?debug=true#example.com` | Does not redirect. Prints the parsed values to the browser console |

- If the site has no scheme, `https://` is added. This also applies to `localhost:8080` and `user:pass@example.com`.
- The `http` and `https` schemes can be in any case, e.g. `#HTTPS://example.com` redirects to `https://example.com/`.
- Other schemes are not supported, so `#ftp://example.com` does not redirect.

## Requirements

- Java 17
- sbt

The build does not use npm. Scala.js writes `main.js`, and sbt copies it with the files in `public/` into the site folder.

Node.js is only needed to run the tests, because Scala.js runs them on Node.js.

## Development

```sh
sbt dev
```

This builds the site into `target/dev-site`, starts a dev server and rebuilds when a file in `src/` or `public/` changes. Open http://localhost:8888 and refresh the page after a rebuild.

To test the page without a redirect, use http://localhost:8888/?debug=true#example.com and open the browser console.

Press Enter in sbt to stop watching for changes. The dev server stops when sbt exits.

## Test

```sh
sbt test
```

The tests are in `src/test/scala/just/toss/RedirectSpec.scala` and use hedgehog. Most examples there are the results of the previous implementation with `scala-uri`, so they check that the redirects stay the same. The examples marked with "scala-uri failed" or "fixed" are the inputs where the previous implementation threw an exception or gave a wrong result.

## Production build

```sh
sbt build
```

This writes the optimized site to `build/`.

## Commands

| Command | What it does |
|---|---|
| `sbt dev` | Starts the dev server, then rebuilds on every change |
| `sbt devBuild` | Builds the development site into `target/dev-site` once |
| `sbt build` | Builds the production site into `build/` |
| `sbt test` | Runs the tests |
| `start` | Builds the development site and starts the dev server. Use it in the sbt shell, because the server stops when sbt exits |
| `stop` | Stops the dev server |

The dev server is in `project/DevServer.scala`. It uses the HTTP server in the JDK, and it listens on `0.0.0.0:8888`.

## Publish

Run the "[M] Publish Site" GitHub Actions workflow manually. It runs `sbt clean build publishToGitHubPages`, which pushes `build/` to the `gh-pages` branch.
