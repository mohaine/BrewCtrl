import React, { Component, PropTypes } from 'react'

import About from '../components/About'

export default class Pump extends Component {
  render() {
    let { pump } = this.props
    return (<div>
        { pump && (<div>Pump {pump.name}</div>) }
       </div>)
  }
}
