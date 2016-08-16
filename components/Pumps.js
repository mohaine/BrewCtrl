import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import ContentEditable from '../components/ContentEditable'
import PumpEdit from '../components/PumpEdit'
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
        <h2>Pumps <button className="btn btn-default" onClick={()=>this.addPump()}>Add</button></h2>
        {pumps &&  pumps.map(pump=>(<PumpEdit key={pump.id} control={pump} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/>))}

       </div>)
  }
}
