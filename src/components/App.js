import React, { Component } from 'react'
import Footer from './Footer'
import Header from '../containers/Header'
import Default from '../components/Default'
import Configuration from '../containers/Configuration'
import Brewery from '../containers/Brewery'
import StepLists from '../containers/StepLists'
import About from '../components/About'

import { Switch, Route } from 'react-router-dom'

export default class App extends Component {

  render() {

    return (<div><Header />
      <div className="container-fluid">

        <Route path="/brewctrl">
          <Switch>
            <Route path="/brewctrl/about"><About /></Route>
            <Route path="/brewctrl/configuration" ><Configuration /></Route>
            <Route path="/brewctrl/brew"><Brewery /></Route>
            <Route path="/brewctrl/steplists"><StepLists /></Route>
            <Route><Default /></Route>
          </Switch>
        </Route>

      </div>
      <Footer /></div>)
  }
}
