import { createStore, applyMiddleware, compose } from 'redux'
import thunkMiddleware from 'redux-thunk'
import logger from 'redux-logger'

import {createRootReducer} from '../reducers'
import { config } from '../config'


export default function configureStore(initialState) {
  let middle;

  if (config.logState) {
    middle = applyMiddleware(thunkMiddleware, logger);
  } else {
    middle = applyMiddleware(thunkMiddleware);
  }

  const store = createStore(
    createRootReducer(),
    initialState,
    compose(middle)
  )

  return store
}
