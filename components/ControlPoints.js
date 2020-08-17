import React, { Component, PropTypes } from 'react'

import { createManualStep, findControlByIo, findTargetByAddress } from '../util/step'
import ControlPoint from '../components/ControlPoint'


export default class ControlPoints extends Component {
  addStep() {
    let { configuration, steps, requestUpdateStepList } = this.props
    let step = createManualStep(configuration);
    let stepsRaw = steps.map(s => s.rawStep);
    stepsRaw.push(step);
    requestUpdateStepList(stepsRaw);
  }

  edit() {
  }


  render() {

    let { configuration, steps, status, selectedStepId, selectStepById, requestUpdateStep } = this.props

    let selectedStep = steps.find((s) => s.id == selectedStepId)
    let controlPoints = []

    if (selectedStep && selectedStep.rawStep) {
      controlPoints = selectedStep.rawStep.controlPoints
    }
    controlPoints = controlPoints.filter(c => c.controlIo > 0)

    return (
      <div className="container-fluid">
        <ul className="list-group" style={{width: "22em"}}>
          {controlPoints && (controlPoints.map(cp => (
            <ControlPoint configuration={configuration} controlPoint={cp} step={selectedStep} status={status} requestUpdateStep={requestUpdateStep} />
          )))}
        </ul>
      </div>
    )
  }
}
