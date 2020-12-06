import React, { Component, PropTypes } from 'react'

import Step from '../components/Step'
import Sensors from '../components/Sensors'
import StepList from '../components/StepList'
import ControlPoints from '../components/ControlPoints'


export default class Brewery extends Component {
  componentDidMount() {
    let { brewery, requestState, requestStateStatus } = this.props
    if (!brewery) {
      requestState();
    }
  }
  render() {
    let { brewery, status, requestState, requestStateStatus, requestUpdateStep, configuration, requestUpdateConfiguration, requestUpdateStepList, selectedStepId, selectStepById } = this.props

    let requestRemoveStep = (step) => {
      let rawSteps = brewery ? brewery.steps.map(s => s.rawStep) : [];
      rawSteps = rawSteps.filter(s => s !== step)
      requestUpdateStepList(rawSteps);
    }
    let step = brewery ? brewery.steps.find(s => s.id == selectedStepId) : undefined;

    return (<div>

      {!brewery && requestStateStatus && requestStateStatus.active && (<div>Loading state</div>)}
      {brewery && (
        <div>
          <div style={{paddingTop: "3px"}}></div>
          <StepList steps={brewery.steps} selectedStepId={selectedStepId} selectStepById={selectStepById} requestUpdateStep={requestUpdateStep} requestUpdateStepList={requestUpdateStepList} configuration={configuration} requestRemoveStep={requestRemoveStep} requestUpdateConfiguration={requestUpdateConfiguration} />

          <div class="tab-content" id="nav-tabContent">
            <div class="tab-pane fade show active" id="nav-home" role="tabpanel" aria-labelledby="nav-home-tab">
              {step && (
                <div className="row">
                  <div key={step.id} className="col-sm-12 col-md-12 col-lg-12"><Step step={step} requestUpdateStep={requestUpdateStep} /></div>
                </div>
              )}

            </div>
          </div>


          <ControlPoints steps={brewery.steps} status={status} selectedStepId={selectedStepId} configuration={configuration} requestUpdateStep={requestUpdateStep} />
        </div>



      )}

    </div>)
  }
}