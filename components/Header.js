import React, { Component, PropTypes } from 'react'

import Mode from '../containers/Mode'

export default class Header extends Component {
  constructor(props, context) {
    super(props, context)
  }
  render() {
        return (<div>
          <Mode />
          </div>)
  }
}
