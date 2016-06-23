import React, { Component, PropTypes } from 'react'

import {formatTime,formatTimeMinutes} from '../util/date'

import Tank from '../components/Tank'
import Pump from '../components/Pump'
import Mode from '../containers/Mode'
import QuickPick from '../components/QuickPick'


export default class Step extends Component {

  constructor(props) {
    super(props);
    this.state = {
      editTime: false
    }
  }
  updateTime(time){
    let { step, requestUpdateStep } = this.props
    requestUpdateStep(Object.assign({}, step.rawStep, { stepTime: time }));

  }
  render() {

    function createEntry(seconds){
      return {value:seconds*60,text:formatTimeMinutes(seconds*60)};
    }

    let { step, requestUpdateStep } = this.props
    return (<div>


      { step && (
      <div>

      { this.state.editTime && (<QuickPick close={()=>{this.setState({editTime: false })}}
      apply={(value)=>{this.updateTime(value)}}
      quickPickValues={ [
          createEntry(15),
          createEntry(30),
          createEntry(60),
          createEntry(90),
          createEntry(0),
        ]}
      increment={(value,up)=>value + (up?60:-60)}
      value={step.time}
      formatValue={(t)=>formatTimeMinutes(t)}
      />)}

        Step: {step.name}


        Time: <span onClick={()=>this.setState({editTime: true })} >{formatTime(step.time)}</span>

        <div className="container-fluid">
          <div className="row">
            {step.tanks.map(tank=>(<div key={tank.id} className="col-sm-3 col-md-3 col-lg-2"><Tank step={step} tank={tank} requestUpdateStep={requestUpdateStep}/></div>))}
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
