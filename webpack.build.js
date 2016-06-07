var packageJson = require('./package.json');
var git = require('git-rev-sync');

var config = {
    name: packageJson.name,
    versionHash: git.long(),
    buildTime: new Date(),
    googleAnalyticsId: 'UA-78659637-1'
};

module.exports = {
  entry: __dirname + '/index.js',
  output: {
    path: __dirname + "/public/",
    filename: 'bundle.js'
  },
  externals: [{
    "config": JSON.stringify(config)
  }],
  module: {
    loaders: [
      {
        exclude: /node_modules/,
        loader: 'babel',
        test: /\.jsx?$/,
        query: {
          presets: ['react', 'es2015']
        }
      }
    ]
  }
}
