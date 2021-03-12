import React, { Component } from 'react'

import Sensor from '../components/Sensor'


export default class Sensors extends Component {
  render() {
    let { sensors, configuration, requestUpdateConfiguration } = this.props
    return (
      <div className="container-fluid">
        <div className="panel">
          <div className="panel-heading">
            <h2>Sensors</h2>
          </div>
          <div className="panel-body">
            {sensors && sensors.map(sensor => (<Sensor key={sensor.address} sensor={sensor} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration} />))}
          </div>
        </div>
      </div>
    )
  }
}
