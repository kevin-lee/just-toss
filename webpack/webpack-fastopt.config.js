const { merge } = require('webpack-merge');
const core = require('./webpack-core.config.js')

const generatedConfig = require("./scalajs.webpack.config.js");
const entries = {};
entries[Object.keys(generatedConfig.entry)[0]] = "scalajs";

module.exports = merge(core, {
  performance: {
    hints: false,
  },
  devtool: "eval-cheap-module-source-map",
  devServer: generatedConfig.devServer,
  entry: entries,
  module: {
    noParse: (content) => {
      return content.endsWith("-fastopt.js");
    },
    rules: [
      {
        test: /\-fastopt.js$/,
        use: [ require.resolve('./fastopt-loader.js') ]
      }
    ]
  }
})
