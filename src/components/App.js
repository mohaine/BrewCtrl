import React, { Component } from 'react'
import Footer from './Footer'
import Header from '../containers/Header'
import Default from '../components/Default'
import Configuration from '../containers/Configuration'
import Brewery from '../containers/Brewery'
import StepLists from '../containers/StepLists'
import About from '../components/About'

import { Routes, Route } from 'react-router-dom'

export default class App extends Component {

  render() {
    let { location, route } = this.props

    if (location && location.pathname && !location.pathname.startsWith("/brewctrl")) {
      route("")
    }

    return (<div><Header />
      <div className="container-fluid">
        <Routes>
          <Route path="/brewctrl/about" element={<About />} />
          <Route path="/brewctrl/configuration" element={<Configuration />} />
          <Route path="/brewctrl/brew" element={<Brewery />} />
          <Route path="/brewctrl/steplists" element={<StepLists />} />
          <Route path="*" element={<Default />} />
        </Routes>

      </div>
      <Footer /></div>)
  }
}
