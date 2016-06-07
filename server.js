
fs = require('fs')

var webpack = require('webpack')
var webpackDevMiddleware = require('webpack-dev-middleware')
var webpackHotMiddleware = require('webpack-hot-middleware')
var config = require('./webpack.config')


var express = require('express')
var timeout = require('connect-timeout');

var app = new(express)()
var port = 3000


function haltOnTimedout(req, res, next){
  if (!req.timedout) next();
}

var compiler = webpack(config)

app.use(webpackDevMiddleware(compiler, {
    noInfo: true,
    publicPath: config.output.publicPath
}))
app.use(webpackHotMiddleware(compiler))

var proxy = require('express-http-proxy');
app.all('/cmd/*',proxy('localhost:2739',{
  forwardPath: function(req, res) {
    return require('url').parse(req.url).path;
  }}));

app.get('/', function(req, res) {
    res.redirect('/brewctrl/');
});

app.use(express.static('web'));

app.get("/brewctrl/*", function(req, res) {
    res.sendFile(__dirname + '/web/brewctrl/index.html')
})

app.get('*', function(req, res) {
    res.redirect('/brewctrl/');
});

app.listen(port, function(error) {
    if (error) {
        console.error(error)
    } else {
        console.info("==> 🌎  Listening on port %s. Open up http://localhost:%s/ in your browser.", port, port)
    }
})
