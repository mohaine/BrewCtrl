import { combineReducers } from 'redux'

import server from './server'

import { syncHistoryWithStore, routerReducer } from 'react-router-redux'

export default combineReducers({
  server,
  routing: routerReducer
})
