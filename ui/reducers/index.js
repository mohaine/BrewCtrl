import { combineReducers } from 'redux'

import server from './server'
import ui from './ui'

import { syncHistoryWithStore, routerReducer } from 'react-router-redux'

export default combineReducers({
  server,ui,
  routing: routerReducer
})
