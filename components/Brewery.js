import React, { Component, PropTypes } from 'react'

import Tank from '../components/Tank'
import Pump from '../components/Pump'


export default class Brewery extends Component {
  componentDidMount() {
    let { brewery,requestState, requestStateStatus } = this.props
    if(!brewery){
        requestState();
    }
  }
  render() {
    let { brewery ,requestState, requestStateStatus } = this.props
    return (<div> Brewery

      {!brewery && requestStateStatus && requestStateStatus.active && (<div>Loading state</div>) }
      { brewery && (
      <div>
        <div className="container-fluid">
          <div className="row">
            {brewery.tanks.map(tank=> (<div key={tank.id} className="col-sm-3 col-md-3 col-lg-2"><Tank tank={tank}  /></div>))}
          </div>
        </div>


        <div className="container-fluid">
          <div className="row">
              {brewery.pumps.map(pump=> (<div key={pump.id} className="col-sm-1 col-md-2 col-lg-1"><Pump pump={pump}/></div>))}
          </div>
        </div>
      </div>
    ) }
       </div>)
  }
}
