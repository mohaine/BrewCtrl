import { createStore, applyMiddleware, compose } from 'redux'
import thunkMiddleware from 'redux-thunk'
import createLogger from 'redux-logger'
import rootReducer from '../reducers'
import { routerMiddleware } from 'react-router-redux'
import { browserHistory } from 'react-router'

import config from 'config'


const middlewareR = routerMiddleware(browserHistory)

export default function configureStore(initialState) {
  let middle;

  if(config.logState){
    middle = applyMiddleware(thunkMiddleware,middlewareR, createLogger());
  } else {
    middle = applyMiddleware(thunkMiddleware,middlewareR);
  }


  const store = createStore(
    rootReducer,
    initialState,
    compose(middle,
    window.devToolsExtension ? window.devToolsExtension() : f => f)
  )

  if (module.hot) {
    // Enable Webpack hot module replacement for reducers
    module.hot.accept('../reducers', () => {
      const nextRootReducer = require('../reducers').default
      store.replaceReducer(nextRootReducer)
    })
  }

  return store
}
