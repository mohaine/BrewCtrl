import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'

export default class Sensors extends Component {

  updateLocation(sensorToChange, location){
    let { configuration, updateConfiguration } = this.props

    let sensors = configuration.sensors;

    sensors = sensors.map(sensor => {
      if(sensor.address === sensorToChange.address){
        return Object.assign({}, sensor, { location });
      }
      return sensor;
    });

    let newConfig = Object.assign(configuration, { sensors });
    updateConfiguration(newConfig)
  }

  render() {
    let { sensors, configuration } = this.props
  return (<div>
        Sensors

        {sensors &&  sensors.map(sensor=>(<div key={sensor.address}>

          <div className="container-fluid">
            <div className="row">
              <div className="col-sm-1">{sensor.name}</div>
              <div className="col-sm-1">

              <select onChange={(e)=>this.updateLocation(sensor,e.target.value)} value={sensor.location}>
              <option value=""></option>
              {configuration.brewLayout.tanks.map(tank=>(<option key={tank.name} value={tank.name} >{tank.name}</option>))}
              </select>
              </div>
              <div className="col-sm-1"
                style={{
                  color: (sensor.reading? "#000" : "#f00")
                }}
                >{formatTemp(sensor.temperatureC)}</div>
              </div>
          </div>
          </div>))}
       </div>)
  }
}
