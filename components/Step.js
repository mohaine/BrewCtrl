import React, { Component, PropTypes } from 'react'

import {formatTime,formatTimeMinutes} from '../util/date'

import Tank from '../components/Tank'
import Pump from '../components/Pump'
import Mode from '../containers/Mode'
import QuickPick from '../components/QuickPick'
import ContentEditable from '../components/ContentEditable'
import {generateAlpahId}  from '../util/id'

export default class Step extends Component {

  constructor(props) {
    super(props);
    let waitForTargetTemp  = props.step.waitForTargetTemp ? true : false;
    let waitForId = generateAlpahId()
    this.state = {
      editTime: false,
      waitForTargetTemp,waitForId
    }
  }
  updateTime(time){
    let { step, requestUpdateStep } = this.props
    requestUpdateStep(Object.assign({}, step.rawStep, { stepTime: time }));
  }
  updateWaitForTargetTemp(waitForTargetTemp){
    let { step, requestUpdateStep } = this.props
    this.setState({ waitForTargetTemp })
    requestUpdateStep(Object.assign({}, step.rawStep, { waitForTargetTemp }));
  }
  updateName(name){
    let { step, requestUpdateStep } = this.props

    if(this.updateNameTimer){
      clearTimeout(this.updateNameTimer);
      this.updateNameTimer = undefined;
    }
    this.updateNameTimer = setTimeout(()=>{requestUpdateStep(Object.assign({}, step.rawStep, { name: name }));}, 500);
  }

  render() {
    let { waitForTargetTemp, waitForId } = this.state

    function createEntry(seconds){
      return {value:seconds*60,text:formatTimeMinutes(seconds*60)};
    }

    let { step, requestUpdateStep,requestRemoveStep } = this.props
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
      increment={(value,up)=>{
        let v = value + (up? 60:-60);
        if(v < 0){
           v = 0;
         }
        return v;
      }}
      value={step.time}
      formatValue={(t)=>formatTimeMinutes(t)}
      ></QuickPick>)}

        <div>
          <label>Step:</label> <ContentEditable onChange={(e)=>this.updateName(e.target.value)} html={step.name} />
        </div>
        <div>
        <label>Time:</label> <span onClick={()=>this.setState({editTime: true })} >{formatTime(step.time)}</span>
        </div>
        <div>
        <input id={waitForId} type="checkbox" checked={waitForTargetTemp} onChange={(e)=>this.updateWaitForTargetTemp(e.target.checked)} />
        <label htmlFor={waitForId}> Wait for Target Temp </label>

        </div>

        <div className="container-fluid">
          <div className="row">
            {step.tanks.map(tank=>(<div key={tank.id} className="col-sm-4 col-md-4 col-lg-3"><Tank step={step} tank={tank} requestUpdateStep={requestUpdateStep}/></div>))}
          </div>
        </div>


        <div className="container-fluid">
          <div className="row">
              {step.pumps.map(pump=>(<div key={pump.id} className="col-sm-2 col-md-2 col-lg-1"><Pump step={step} pump={pump} requestUpdateStep={requestUpdateStep}/></div>))}
          </div>
        </div>
      </div>
    ) }

    <br/>
    <br/>
       </div>)
  }
}
