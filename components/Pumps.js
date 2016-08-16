import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import ContentEditable from '../components/ContentEditable'
import ControlEdit from '../components/ControlEdit'
import {emptyGpios} from '../util/gpio'


export default class Pumps extends Component {
  addPump(){
    let {configuration,requestUpdateConfiguration} = this.props
    let io = emptyGpios(configuration)[0];
    let brewLayout = configuration.brewLayout;
    let pumps = brewLayout.pumps.slice();
    pumps.push({name:"New Pump",io, hasDuty:false,invertIo:false})
    brewLayout = Object.assign({}, brewLayout, {pumps});
    configuration = Object.assign({}, configuration, {brewLayout});
    requestUpdateConfiguration(configuration)
  }
  render() {
    let { pumps, configuration,requestUpdateConfiguration} = this.props
  return (<div>
        Pumps
        {pumps &&  pumps.map(pump=>(<ControlEdit key={pump.id} control={pump} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/>))}
        <button className="btn btn-default" onClick={()=>this.addPump()}>Add Pump</button>
       </div>)
  }
}
