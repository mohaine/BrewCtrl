
import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import ContentEditable from '../components/ContentEditable'


export default class ControlEdit extends Component {

  overlayUpdate(overlay){

    if(this.overlay){
      this.overlay = Object.assign({}, this.overlay, overlay);
    }

    let { configuration, requestUpdateConfiguration } = this.props

    let brewLayout = configuration.brewLayout

    let pumps = brewLayout.pumps;
    pumps = pumps.map(control => {
      if(control.id === this.props.control.id){
        return Object.assign({}, control, overlay);
      }
      return control;
    });

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

  updateName(name){
    this.overlayUpdate({name});
  }


  updateGpio(io){
    io = parseInt(io,10)
    this.overlayUpdate({io});
  }

  render() {
  let gpios = [2, 3, 4, 14, 15, 17, 18, 27, 22, 23, 24, 10, 9, 25, 11, 8, 7].sort((a,b)=> a-b)
  let { control, configuration } = this.props
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
              </div>
          </div>
          </div>)
  }
}
