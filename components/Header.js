import React, { Component, PropTypes } from 'react'

import Mode from '../containers/Mode'
import RouteLink from '../containers/RouteLink'

export default class Header extends Component {
  constructor(props, context) {
    super(props, context)
  }
  render() {
        return (<div>
          <Mode />
          <RouteLink route="">Home</RouteLink>
          <RouteLink route="configuration">Configuration</RouteLink>


          </div>)
  }
}
