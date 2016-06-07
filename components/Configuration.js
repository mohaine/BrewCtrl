import React, { Component, PropTypes } from 'react'

import About from '../components/About'

export default class Brewery extends Component {

  componentDidMount() {
    let { configuration,requestConfiguration, requestConfigurationStatus } = this.props
    if(!configuration){
        requestConfiguration();
    }

  }
  render() {
    let { configuration,requestConfiguration, requestConfigurationStatus } = this.props
    return (<div> Configuration:

      {!configuration && requestConfigurationStatus && requestConfigurationStatus.active && (<div>Loading configuration</div>) }
      { configuration && (<div>
        Version {configuration.version}
      </div>) }
       </div>)
  }
}
