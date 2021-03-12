import React, { Component } from 'react'

import { createManualStep, findControlPointByName, findTempSensorByLocationName } from '../util/step'
import { parseTime } from '../util/date'
import ContentEditable from '../components/ContentEditable'

export default class StepsList extends Component {
  deleteList(stepList) {
    let { configuration, requestUpdateConfiguration } = this.props;
    let stepLists = configuration.stepLists.filter(s => s.id !== stepList.id)
    let cfgNew = Object.assign({}, configuration, { stepLists })
    requestUpdateConfiguration(cfgNew)
  }

  updateName(stepList, name) {
    let { configuration, requestUpdateConfiguration } = this.props;
    let stepLists = configuration.stepLists.map(s => {
      if (s.id === stepList.id) {
        return Object.assign({}, s, { name })
      }
      return s
    })
    let cfgNew = Object.assign({}, configuration, { stepLists })

    if (this.updateNameTimer) {
      clearTimeout(this.updateNameTimer);
      this.updateNameTimer = undefined;
    }
    this.updateNameTimer = setTimeout(() => { requestUpdateConfiguration(cfgNew) }, 500);

  }

  loadList(stepList) {
    let { configuration, status, requestUpdateStepList } = this.props;
    let steps = stepList.steps.map(s => {
      let step = createManualStep(configuration);
      step.name = s.name;
      step.stepTime = parseTime(s.time);
      let controlPoints = step.controlPoints;
      if (s.controlPoints) {
        s.controlPoints.forEach(cp => {
          let controlPointDef = findControlPointByName(configuration, cp.controlName);
          if (controlPointDef) {
            let controlPoint = controlPoints.find(cp => cp.controlIo === controlPointDef.io)
            if (controlPoint) {
              if (cp.targetName) {
                let sensor = findTempSensorByLocationName(configuration, status, cp.targetName);
                if (sensor) {
                  controlPoint.automaticControl = true;
                  controlPoint.targetTemp = cp.targetTemp;
                  controlPoint.tempSensorAddress = sensor.address;
                }
              } else {
                controlPoint.duty = cp.duty
              }
            }
          }
        });
      }
      return step
    });
    requestUpdateStepList(steps)
  }

  render() {

    let { stepLists } = this.props
    return (<div className="container-fluid">

      <h2>Step Lists</h2>

      {stepLists && (stepLists.map(stepList => (
        <div className="row" key={stepList.id}  >
          <div className="col-sm-6">
            <ContentEditable onChange={(e) => this.updateName(stepList, e.target.value)} html={stepList.name} />
          </div>
          <div className="col-sm-6">
            <div className="btn-grouip">
              <button type="button" className="btn btn-default" onClick={() => this.loadList(stepList)}>Load</button>
              <button type="button" className="btn btn-default" onClick={() => this.deleteList(stepList)}>Delete</button>
            </div>
          </div>
        </div>
      )))}
    </div>)
  }
}
