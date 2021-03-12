import { combineReducers } from 'redux'
import { connectRouter } from 'connected-react-router'

import server from './server'
import ui from './ui'


export const createRootReducer = (history) => combineReducers({
  router: connectRouter(history),
  server,ui,
})
