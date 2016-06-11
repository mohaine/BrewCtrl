import React, { Component, PropTypes } from 'react'

import Tank from '../components/Tank'
import Pump from '../components/Pump'
import Mode from '../containers/Mode'


export default class Step extends Component {
    render() {
    let { step, requestUpdateStep } = this.props
    return (<div>
      { step && (
      <div>
        Step: {step.name}
        <div className="container-fluid">
          <div className="row">
            {step.tanks.map(tank=>(<div key={tank.id} className="col-sm-3 col-md-3 col-lg-2"><Tank step={step} tank={tank}  /></div>))}
          </div>
        </div>


        <div className="container-fluid">
          <div className="row">
              {step.pumps.map(pump=>(<div key={pump.id} className="col-sm-2 col-md-2 col-lg-1"><Pump step={step} pump={pump} requestUpdateStep={requestUpdateStep}/></div>))}
          </div>
        </div>
      </div>
    ) }
       </div>)
  }
}
