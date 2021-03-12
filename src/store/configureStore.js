import { createStore, applyMiddleware, compose } from 'redux'
import thunkMiddleware from 'redux-thunk'
import logger from 'redux-logger'
import { routerMiddleware } from 'connected-react-router'
import { createBrowserHistory } from 'history'

import {createRootReducer} from '../reducers'
import { config } from '../config'


export const history = createBrowserHistory()

export default function configureStore(initialState) {
  let middle;

  if (config.logState) {
    middle = applyMiddleware(thunkMiddleware,routerMiddleware(history), logger);
  } else {
    middle = applyMiddleware(thunkMiddleware,routerMiddleware(history));
  }

  const store = createStore(
    createRootReducer(history),
    initialState,
    compose(middle)
  )

  return store
}
