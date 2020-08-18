
import React, { Component, PropTypes } from 'react'

import { formatTemp } from '../util/tempature'
import ContentEditable from '../components/ContentEditable'


export default class Sensor extends Component {

  overlayUpdate(overlay) {

    if (this.overlay) {
      this.overlay = Object.assign({}, this.overlay, overlay);
    }

    let { configuration, requestUpdateConfiguration } = this.props
    let sensorToChange = this.props.sensor;
    let sensors = configuration.sensors;
    sensors = sensors.map(sensor => {
      if (sensor.address === sensorToChange.address) {
        return Object.assign({}, sensor, overlay);
      }
      return sensor;
    });
    let newConfig = Object.assign(configuration, { sensors });

    if (this.updateCfgTimer) {
      clearTimeout(this.updateCfgTimer);
      this.updateCfgTimer = undefined;
    }

    this.updateCfgTimer = setTimeout(() => {
      requestUpdateConfiguration(newConfig)
      this.updateCfgTimer = undefined;
    }, 500);


  }

  updateName(name) {
    this.overlayUpdate({ name });
  }


  updateLocation(location) {
    this.overlayUpdate({ location });
  }

  render() {
    let { sensor, configuration } = this.props

    let tankNames = Array.from(new Set(configuration.brewLayout.tanks.map(tank => tank.name)));

    return (<div >
      <div className="card">
        <div className="card-body">
          <h5 className="card-title">
            <ContentEditable onChange={(e) => this.updateName(e.target.value)} html={sensor.name} placeholder="click to enter a name" />
            <strong className="hoverable" onClick={() => this.remove()} style={{ float: "right", padding: "0px 4px 0px 4px", marginLeft: "20px" }}> &#215; </strong>
          </h5>
          <div>
            Sensor location is <select onChange={(e) => this.updateLocation(e.target.value)} value={sensor.location}>
              <option value=""></option>
              {tankNames.map(tank => (<option key={tank} value={tank} >{tank}</option>))}
            </select>
            <span>
              <span> and is </span>
              {sensor.reading && (<span> reading {formatTemp(sensor.temperatureC)}</span>)}
              {!sensor.reading && (<span> not currently reading</span>)}
            </span>
          </div>

        </div>
      </div>
    </div>)
  }
}
