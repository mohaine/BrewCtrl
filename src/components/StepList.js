import React, { Component } from 'react'

import { createManualStep, findControlByIo, findTargetByAddress } from '../util/step'
import ContentEditable from '../components/ContentEditable'


export default class StepList extends Component {
  addStep() {
    let { configuration, steps, requestUpdateStepList } = this.props
    let step = createManualStep(configuration);
    let stepsRaw = steps.map(s => s.rawStep);
    stepsRaw.push(step);
    requestUpdateStepList(stepsRaw);
  }
  saveAsList() {
    let { configuration, steps, requestUpdateConfiguration } = this.props

    let listSteps = steps.map(s => {
      let name = s.name
      let time = "" + s.time
      let controlPoints = s.controlPoints.filter(cp => {
        return cp.automaticControl || cp.duty > 0
      }).map(cp => {
        let duty = cp.duty
        let control = findControlByIo(configuration, cp.controlIo)
        let controlName = control.name
        let listCp = { duty, controlName }
        if (cp.automaticControl) {
          let target = findTargetByAddress(configuration, cp.tempSensorAddress)
          listCp.targetName = target.name
          listCp.targetTemp = cp.targetTemp
        }
        return listCp;
      })
      return { name, time, controlPoints }
    })
    let list = { name: "New List", steps: listSteps }
    let stepLists = configuration.stepLists.map(s => s)
    stepLists.push(list)
    let cfgNew = Object.assign({}, configuration, { stepLists })
    requestUpdateConfiguration(cfgNew)
  }

  updateName(step, name) {
    let { requestUpdateStep } = this.props
    if (this.updateNameTimer) {
      clearTimeout(this.updateNameTimer);
      this.updateNameTimer = undefined;
    }
    this.updateNameTimer = setTimeout(() => { requestUpdateStep(Object.assign({}, step.rawStep, { name: name })); }, 500);
  }


  render() {
    let { steps, selectedStepId, selectStepById, requestRemoveStep } = this.props

    return (<div>

      <ul className="nav nav-tabs">
        {steps && (steps.map(step => {
          let activeStep = selectedStepId === step.id;
          if(activeStep){
            console.log("activeStep: ",step)
          }

          let stepName = step.name

          return (
            <li key={step.id} className="nav-item  clickable" onClick={() => { selectStepById(step.id) }} >

              <span className={"nav-link" + (activeStep ? " active" : "")}>
                <span>
                  {activeStep && (<ContentEditable onChange={(e) => this.updateName(step, e.target.value)} html={stepName} />)}
                  {!activeStep && (<span>{stepName}</span>)}
                </span>
                {requestRemoveStep && (<strong className="hoverable" onClick={() => requestRemoveStep(step.rawStep)} style={{ float: "right", padding: "0px 4px 0px 4px", marginLeft: "20px" }}> &#215; </strong>)}
              </span>
            </li>
          )
        }))}

        <li className="nav-item clickable">
          <span className="nav-link" onClick={() => this.addStep()}><strong>  &#43; </strong></span>
        </li>
        <li className="nav-item clickable" style={{ float: "right", position: "relative" }}>
          <button type="button" className="btn btn-default" onClick={() => this.saveAsList()}>Save As List</button>
        </li>
      </ul>
    </div>)
  }
}
