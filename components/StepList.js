import React, { Component, PropTypes } from 'react'

import {createManualStep,findControlByIo,findTargetByAddress} from '../util/step'


export default class StepList extends Component {
  addStep(){
    let {configuration, steps, requestUpdateStepList } = this.props
    let step = createManualStep(configuration);
    let stepsRaw = steps.map(s=>s.rawStep);
    stepsRaw.push(step);
    requestUpdateStepList(stepsRaw);
  }
  saveAsList(){
    let {configuration, steps, requestUpdateStepList,requestUpdateConfiguration } = this.props

    let listSteps = steps.map(s=>{
      let name = s.name
      let time = ""+s.time
      let controlPoints = s.controlPoints.filter(cp=>{
        return cp.automaticControl || cp.duty > 0
      }).map(cp=> {
        let duty = cp.duty
        let control = findControlByIo(configuration,cp.controlIo)
        let controlName = control.name
        let listCp = {duty,controlName}
        if(cp.automaticControl){
          let target = findTargetByAddress(configuration,cp.tempSensorAddress)
          listCp.targetName = target.name
          listCp.targetTemp = cp.targetTemp
        }
        return listCp;
      })
      return {name,time,controlPoints}
    })
    let list = {name:"New List", steps:listSteps}
    let stepLists = configuration.stepLists.map(s=>s)
    stepLists.push(list)
    let cfgNew = Object.assign({}, configuration, {stepLists})
    requestUpdateConfiguration(cfgNew)
  }

  render() {

    let { steps, selectedStepId, selectStepById, requestRemoveStep} = this.props

    let selectedStep = steps.find((s)=> s.id == selectedStepId)

    return (<div>
            <div className="btn-group">
          {steps && (steps.map(step=> (
          <button type="button" className={"btn " + (selectedStepId === step.id ?"down":"btn-default")} key={step.id} onClick={()=>selectStepById(step.id)} >
            {step.name}
          </button>
        )))}</div><br/><br/>
           <div className="btn-group">
             <button type="button" className="btn btn-default" onClick={()=>this.addStep()}>Add Step</button>
             {selectedStep && requestRemoveStep && <button type="button" className="btn btn-default" onClick={()=>requestRemoveStep(selectedStep.rawStep)}>Remove Step {selectedStep.name}</button> }
             <button type="button" className="btn btn-default" onClick={()=>this.saveAsList()}>Save As List</button>
           </div>
       </div>)
  }
}
