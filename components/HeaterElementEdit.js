
import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import {emptyGpios} from '../util/gpio'
import ContentEditable from '../components/ContentEditable'


export default class HeaterElementEdit extends Component {

  constructor(props) {
      super(props);
      this.state = {
        maxDuty: props.control.maxDuty,
        fullOnAmps: props.control.fullOnAmps,
        hasDuty: props.control.hasDuty
      }
  }


  overlayUpdate(overlay,remove){
    if(this.overlay){
      this.overlay = Object.assign({}, this.overlay, overlay);
    }
    let { configuration, requestUpdateConfiguration } = this.props
    let brewLayout = configuration.brewLayout
    let tanks = brewLayout.tanks.slice();
    if(overlay){
      tanks = tanks.map(tank => {
        if(tank.heater && tank.heater.id === this.props.control.id){
          let heater = Object.assign({}, tank.heater, overlay)
          return Object.assign({}, tank, {heater});
        }
        return tank;
      });
    } else if(remove){
      tanks = tanks.map(tank => {
        if(tank.heater && tank.heater.id === this.props.control.id){
          return Object.assign({}, tank, {heater:undefined});
        }
        return tank;
      });
    }

    // Create new object tree
    brewLayout = Object.assign({}, brewLayout, {tanks});
    configuration = Object.assign({}, configuration, {brewLayout});

    if(this.updateCfgTimer){
      clearTimeout(this.updateCfgTimer);
      this.updateCfgTimer = undefined;
    }

    this.updateCfgTimer = setTimeout(()=> {
      requestUpdateConfiguration(configuration)
      this.updateCfgTimer = undefined;
    },500);
  }

  remove(){
    this.overlayUpdate(null,true);
  }

  updateName(name){
    this.overlayUpdate({name});
  }

  updateGpio(io){
    io = parseInt(io,10)
    this.overlayUpdate({io});
  }
  updateFullOnAmps(fullOnAmps){
    fullOnAmps = parseInt(fullOnAmps,10)
    this.setState({fullOnAmps})
    this.overlayUpdate({fullOnAmps});
  }
  updateMaxDuty(maxDuty){
    maxDuty = parseInt(maxDuty,10)
    this.setState({maxDuty})
    this.overlayUpdate({maxDuty});
  }
  
  updateHasDuty(hasDuty){
    hasDuty = hasDuty ? true : false
    this.setState({hasDuty})
    this.overlayUpdate({hasDuty});
  }
  render() {
  let { control, configuration } = this.props
  let {maxDuty,fullOnAmps,hasDuty} = this.state
  let gpios = emptyGpios(configuration)
  gpios.push(control.io)
  gpios.sort((a,b)=> a-b)
  return (<div>
          <div className="container-fluid">
            <div className="row">
              <div className="col-sm-1">
              <select onChange={(e)=>this.updateGpio(e.target.value)} value={control.io}>
              <option value=""></option>
              {gpios.map(io=>(<option key={io} value={io}>GPIO {io}</option>))}
              </select>
              </div>
              <div className="col-sm-2">
                Amps: <input type="number" value={fullOnAmps} onChange={(e)=>this.updateFullOnAmps(e.target.value)} style={{width: "3em"}}/>
              </div>
              <div className="col-sm-2">
                Max Duty: <input type="number" min="1" max="100" value={maxDuty} onChange={(e)=>this.updateMaxDuty(e.target.value)} style={{width: "4em"}}/>
              </div>
              <div className="col-sm-2">
                Has Duty: <input type="checkbox" checked={hasDuty} onChange={(e)=>this.updateHasDuty(e.target.checked)} style={{width: "3em"}}/>
              </div>
              <div className="col-sm-1">
                <button className="btn btn-default" onClick={()=>this.remove()}>Remove Element</button>
              </div>
              </div>
          </div>
          </div>)
  }
}
