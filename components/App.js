import React, { Component, PropTypes } from 'react'
import Footer from './Footer'
import Header from '../containers/Header'
import { Router, Route, browserHistory } from 'react-router'


export default class App extends Component {
  render() {
    const {children} = this.props
    return  (<div><Header/>
      <div className="container-fluid">
        {children}
      </div>
      <Footer/></div>)
  }
}
