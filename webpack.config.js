var path = require('path')
var webpack = require('webpack')

var packageJson = require('./package.json');
var git = require('git-rev-sync');

var config = {
    name: packageJson.name,
    versionHash: git.long(),
    buildTime: new Date(),
    logState: true
};

module.exports = {
  devtool: 'cheap-module-eval-source-map',
  entry: [
    'webpack-hot-middleware/client',
    './index'
  ],
  output: {
    path: path.join(__dirname, 'dist'),
    filename: 'bundle.js',
    publicPath: '/'
  },
  plugins: [
    new webpack.optimize.OccurenceOrderPlugin(),
    new webpack.HotModuleReplacementPlugin()
  ],
  externals: [{
    "config": JSON.stringify(config)
  }],
  module: {
    loaders: [
      {
        test: /\.js$/,
        loaders: [ 'babel' ],
        exclude: /node_modules/,
        include: __dirname
      }
    ]
  }
}
