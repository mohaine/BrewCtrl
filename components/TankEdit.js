
import React, { Component, PropTypes } from 'react'

import ContentEditable from '../components/ContentEditable'
import HeaterElementEdit from '../components/HeaterElementEdit'
import {emptyGpios} from '../util/gpio'

export default class Tank extends Component {

  overlayUpdate(overlay,remove,addElement){
    if(this.overlay){
      this.overlay = Object.assign({}, this.overlay, overlay);
    }
    let { configuration, requestUpdateConfiguration } = this.props
    let brewLayout = configuration.brewLayout
    let tanks = brewLayout.tanks.slice();
    if(overlay){
      tanks = tanks.map(tank => {
        if(tank.id === this.props.tank.id){
          return Object.assign({}, tank, overlay);
        }
        return tank;
      });
    } else if(remove){
      tanks = tanks.filter(tank=>tank.id != this.props.tank.id)
    } else if(addElement){
      tanks = tanks.filter(tank=>tank.id != this.props.tank.id)
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
  addElement(){
    let {tank} = this.props;

    let {configuration,requestUpdateConfiguration} = this.props
    let io = emptyGpios(configuration)[0];
    this.overlayUpdate({heater:{name:tank.name + " Heater",io, hasDuty:false,invertIo:false, fullOnAmps:0}});
  }

  render(){
      let {tank,configuration,requestUpdateConfiguration} = this.props;
      return (<div>
        <ContentEditable onChange={(e)=>this.updateName(e.target.value)} html={tank.name} />
        {tank.heater && <HeaterElementEdit control={tank.heater} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/>}
        {!tank.heater &&   <button className="btn btn-default" onClick={()=>this.addElement()}>Add Element</button>}
        <button className="btn btn-default" onClick={()=>this.remove()}>Remove Tank</button>
        </div>)

  }
}
