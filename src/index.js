import React from 'react'
import { createRoot } from 'react-dom/client'
import { Provider } from 'react-redux'
import { BrowserRouter } from 'react-router-dom'

import App from './containers/App'

import configureStore from './store/configureStore'

const store = configureStore()

const root = createRoot(document.getElementById('root'))
root.render(<div>
  <Provider store={store}>
    <BrowserRouter>
      <App/>
    </BrowserRouter>
  </Provider>

</div>)
