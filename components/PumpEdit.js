
import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import {emptyGpios} from '../util/gpio'
import ContentEditable from '../components/ContentEditable'


export default class PumpEdit extends Component {

  overlayUpdate(overlay,remove){
    if(this.overlay){
      this.overlay = Object.assign({}, this.overlay, overlay);
    }
    let { configuration, requestUpdateConfiguration } = this.props
    let brewLayout = configuration.brewLayout
    let pumps = brewLayout.pumps;
    if(overlay){
      pumps = pumps.map(control => {
        if(control.id === this.props.control.id){
          return Object.assign({}, control, overlay);
        }
        return control;
      });
    } else if(remove){
      pumps = pumps.filter(control=>control.id != this.props.control.id)
    }

    // Create new object tree
    brewLayout = Object.assign({}, brewLayout, {pumps});
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

  render() {
  let { control, configuration } = this.props
  let gpios = emptyGpios(configuration)
  gpios.push(control.io)
  gpios.sort((a,b)=> a-b)

  return (<div >
          <div className="container-fluid">
            <div className="row">
              <div className="col-sm-2">
              <ContentEditable onChange={(e)=>this.updateName(e.target.value)} html={control.name} />
              </div>
              <div className="col-sm-1">
              <select onChange={(e)=>this.updateGpio(e.target.value)} value={control.io}>
              <option value=""></option>
              {gpios.map(io=>(<option key={io} value={io}>GPIO {io}</option>))}
              </select>
              </div>
              <div className="col-sm-1">
                <button className="btn btn-default" onClick={()=>this.remove()}>Remove</button>
              </div>
              </div>
          </div>
          </div>)
  }
}
