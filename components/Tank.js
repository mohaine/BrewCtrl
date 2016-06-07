import React, { Component, PropTypes } from 'react'

import About from '../components/About'

export default class Tank extends Component {
  render() {
    let { tank } = this.props
    return (<div>
        { tank && (<div>Tank {tank.name}</div>) }
       </div>)
  }
}
