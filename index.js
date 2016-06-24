import 'babel-polyfill'
import React from 'react'
import { render } from 'react-dom'
import { Provider } from 'react-redux'
import { createStore } from 'redux'


import App from './components/App'
import Default from './components/Default'
import Configuration from './containers/Configuration'
import Brewery from './containers/Brewery'
import About from './components/About'

import config from 'config'

import { Router, Route,IndexRoute, browserHistory } from 'react-router'
import { syncHistoryWithStore, routerReducer } from 'react-router-redux'
import { setRequestConfig } from './reducers/server.js'
import { requestConfig } from './actions/configuration.js'


import configureStore from './store/configureStore'

const store = configureStore()

const history = syncHistoryWithStore(browserHistory, store)

store.dispatch({
    type: "DISPATCH",
    dispatch: store.dispatch
});

render(<div>
  <Provider store={store}>
        <Router history={history}>
          <Route path="/brewctrl" component={App}>
            <IndexRoute component={Default}/>
            <Route path="about" component={About}/>
            <Route path="configuration" component={Configuration}/>
            <Route path="brew" component={Brewery}/>
          </Route>
        </Router>
  </Provider></div>,
  document.getElementById('root')
)
