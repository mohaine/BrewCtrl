import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import ContentEditable from '../components/ContentEditable'
import Sensor from '../components/Sensor'


export default class Sensors extends Component {
  render() {
    let { sensors, configuration,requestUpdateConfiguration} = this.props
  return (
    <div className="container-fluid">
      <div className="panel">
        <div className="panel-heading">
          <h2>Sensors</h2>
        </div>
        <div className="panel-body">
        {sensors &&  sensors.map(sensor=>(<Sensor key={sensor.address} sensor={sensor} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/>))}
        </div>
      </div>
    </div>
    )
  }
}
