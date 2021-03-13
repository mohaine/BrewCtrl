import React from 'react'
import { render } from 'react-dom'
import { Provider } from 'react-redux'


import App from './containers/App'
import { ConnectedRouter } from 'connected-react-router'


import configureStore, { history } from './store/configureStore'

const store = configureStore()

render(<div>
  <Provider store={store}>
    <ConnectedRouter history={history}>
      <App/>
    </ConnectedRouter>
  </Provider>

</div>,
  document.getElementById('root')
)
