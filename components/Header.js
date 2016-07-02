import React, { Component, PropTypes } from 'react'

import Mode from '../containers/Mode'
import RouteLink from '../containers/RouteLink'

export default class Header extends Component {
  constructor(props, context) {
    super(props, context)
  }
  render() {
        return (<div>
          <div style={{float: "right"}}>
            <RouteLink route="brew">Brew</RouteLink> &nbsp;
            <RouteLink route="configuration">Configuration</RouteLink> &nbsp;
            <RouteLink route="steplists">Step Lists</RouteLink>
          </div>
          <Mode />
        </div>)
  }
}
