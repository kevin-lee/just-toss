if (process.env.NODE_ENV === "production") {
    const opt = require("./just-toss-opt.js");
    opt.main();
    module.exports = opt;
} else {
    var exports = window;
    exports.require = require("./just-toss-fastopt-entrypoint.js").require;
    window.global = window;

    const fastOpt = require("./just-toss-fastopt.js");
    fastOpt.main()
    module.exports = fastOpt;

    if (module.hot) {
        module.hot.accept();
    }
}
