
import React, { Component, PropTypes } from 'react'


import {generateAlpahId} from '../util/id'

import config from 'config'


export default class AddStep extends Component {
  addStep(){

    function initCp(cp){
      cp.duty = 0;
      cp.tempSensorAddress = "";
      cp.targetTemp = 0;
      cp.automaticControl = false;
      if(cp.fullOnAmps === undefined) cp.fullOnAmps = 0;
      return cp;
    }

    let {configuration, steps, requestUpdateStepList } = this.props
    let controlPoints = [];
    let brewLayout = configuration.brewLayout;
    brewLayout.tanks.forEach(t=>{
        let heater = t.heater;
        if(heater){
          let controlPoint = initCp({controlIo: heater.io, hasDuty: heater.hasDuty,fullOnAmps: heater.fullOnAmps});
          controlPoints.push(controlPoint)
        }
    });

    brewLayout.pumps.forEach(p=>{
      let controlPoint = initCp({controlIo: p.io, hasDuty: p.hasDuty,fullOnAmps: p.fullOnAmps});
      controlPoints.push(controlPoint)
    });

    let step = {stepTime: 0, name: "New Step", id: generateAlpahId(8),controlPoints}
    let stepsRaw = steps.map(s=>s.rawStep);
    stepsRaw.push(step);
    requestUpdateStepList(stepsRaw);

  }


  render() {
      return (
        <div >
      <button type="button" onClick={()=>this.addStep()} >Add Step</button>
    </div>)
  }
}
