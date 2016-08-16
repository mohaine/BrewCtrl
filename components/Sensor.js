
import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import ContentEditable from '../components/ContentEditable'


export default class Sensor extends Component {

  overlayUpdate(overlay){

    if(this.overlay){
      this.overlay = Object.assign({}, this.overlay, overlay);
    }

    let { configuration, requestUpdateConfiguration } = this.props
    let sensorToChange = this.props.sensor;
    let sensors = configuration.sensors;
    sensors = sensors.map(sensor => {
      if(sensor.address === sensorToChange.address){
        return Object.assign({}, sensor, overlay);
      }
      return sensor;
    });
    let newConfig = Object.assign(configuration, { sensors });

    if(this.updateCfgTimer){
      clearTimeout(this.updateCfgTimer);
      this.updateCfgTimer = undefined;
    }

    this.updateCfgTimer = setTimeout(()=> {
      requestUpdateConfiguration(newConfig)
      this.updateCfgTimer = undefined;
    },500);


  }

  updateName(name){
    this.overlayUpdate({name});
  }


  updateLocation( location){
    this.overlayUpdate({location});
  }

  render() {
  let { sensor, configuration } = this.props
  return (<div >
          <div className="container-fluid">
            <div className="row">
              <div className="col-sm-2">
              <ContentEditable onChange={(e)=>this.updateName(e.target.value)} html={sensor.name} />
              </div>
              <div className="col-sm-1">
              <select onChange={(e)=>this.updateLocation(e.target.value)} value={sensor.location}>
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
          </div>)
  }
}
