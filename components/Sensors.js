import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import ContentEditable from '../components/ContentEditable'
import Sensor from '../components/Sensor'


export default class Sensors extends Component {
  render() {
    let { sensors, configuration,requestUpdateConfiguration} = this.props
  return (<div>
        Sensors
        {sensors &&  sensors.map(sensor=>(<Sensor key={sensor.address} sensor={sensor} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/>))}
       </div>)
  }
}
