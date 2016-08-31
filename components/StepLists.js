import React, { Component, PropTypes } from 'react'

import {createManualStep,findControlPointByName,findTempSensorByLocationName} from '../util/step'
import {parseTime} from '../util/date'

export default class StepsList extends Component {

  loadList(stepList){
    let {configuration,status,requestUpdateStepList} = this.props;
    let steps = stepList.steps.map(s=>{
      let step = createManualStep(configuration);
      step.name = s.name;
      step.stepTime = parseTime(s.time);
      let controlPoints =  step.controlPoints;
      s.controlPoints.forEach(cp=>{
        let controlPointDef = findControlPointByName(configuration, cp.controlName);
        if(controlPointDef){
          let controlPoint = controlPoints.find(cp=>cp.controlIo === controlPointDef.io)
          // console.log(cp, "=>",controlPoint)
          if(controlPoint){
            let sensor = findTempSensorByLocationName(configuration,status,cp.targetName);
            if(sensor){
              controlPoint.automaticControl = true;
              controlPoint.targetTemp = cp.targetTemp;
              controlPoint.tempSensorAddress = sensor.address;
            }
          }
        }
      });
      return step
    });
    requestUpdateStepList(steps)
  }

  render() {

    let { stepLists} = this.props
    return (<div>
        {stepLists && (stepLists.map(stepList=> (
          <div key={stepList.name}  >
            {stepList.name}
            &nbsp;
            <button type="button" className="btn btn-default" onClick={()=>this.loadList(stepList)} >Load</button>

          </div>
           )))}
       </div>)
  }
}
