import { combineReducers } from 'redux'

import server from './server'
import ui from './ui'


export const createRootReducer = () => combineReducers({
  server,ui,
})
