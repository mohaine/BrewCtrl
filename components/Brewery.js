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

        <div>version {brewery.version}
        {brewery.tanks.map(tank=> (<Tank tank={tank} />))}
        {brewery.pumps.map(pump=> (<Pump pump={pump} />))}


        </div>) }
       </div>)
  }
}
