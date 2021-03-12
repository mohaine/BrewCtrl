import React from 'react'
import { render } from 'react-dom'
import { Provider } from 'react-redux'


import App from './components/App'
import Default from './components/Default'
import Configuration from './containers/Configuration'
import Brewery from './containers/Brewery'
import StepLists from './containers/StepLists'
import About from './components/About'

import { Switch, Route , useRouteMatch} from 'react-router-dom'
import { ConnectedRouter } from 'connected-react-router'


import configureStore, { history } from './store/configureStore'

const store = configureStore()

store.dispatch({
  type: "DISPATCH",
  dispatch: store.dispatch
});


render(<div>
  <Provider store={store}>
    <ConnectedRouter history={history}>
      <App>
        <Route path="/brewctrl">
          <Switch>
            <Route path="/brewctrl/about"><About /></Route>
            <Route path="/brewctrl/configuration" ><Configuration /></Route>
            <Route path="/brewctrl/brew"><Brewery /></Route>
            <Route path="/brewctrl/steplists"><StepLists /></Route>
            <Route><Default /></Route>
          </Switch>
        </Route>
      </App>
    </ConnectedRouter>
  </Provider>

</div>,
  document.getElementById('root')
)
