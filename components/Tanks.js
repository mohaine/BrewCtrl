import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import ContentEditable from '../components/ContentEditable'
import TankEdit from '../components/TankEdit'
import {emptyGpios} from '../util/gpio'


export default class Tanks extends Component {
  addTank(){
    let {configuration,requestUpdateConfiguration} = this.props
    let io = emptyGpios(configuration)[0];
    let brewLayout = configuration.brewLayout;
    let tanks = brewLayout.tanks.slice();
    tanks.push({name:"New Tank",io, hasDuty:true,invertIo:false})
    brewLayout = Object.assign({}, brewLayout, {tanks});
    configuration = Object.assign({}, configuration, {brewLayout});
    requestUpdateConfiguration(configuration)
  }
  render() {
    let { tanks, configuration,requestUpdateConfiguration} = this.props
  return (
    <div className="container-fluid">
      <div className="panel">
        <div className="panel-heading">
          <h2>Tanks <button className="btn btn-default" onClick={()=>this.addTank()}>Add</button></h2>
        </div>
        <div className="panel-body">
          {tanks &&  tanks.map(tank=>(<div key={tank.id}><TankEdit tank={tank} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/></div>))}
        </div>
      </div>
    </div>
)
  }
}
