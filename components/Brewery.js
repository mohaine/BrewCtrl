import React, { Component, PropTypes } from 'react'

import Step from '../components/Step'
import Sensors from '../components/Sensors'
import AddStep from '../components/AddStep'
import StepList from '../components/StepList'


export default class Brewery extends Component {
  componentDidMount() {
    let { brewery,requestState, requestStateStatus } = this.props
    if(!brewery){
        requestState();
    }
  }
  render() {
    let { brewery ,requestState, requestStateStatus, requestUpdateStep, configuration, requestUpdateConfiguration, requestUpdateStepList,selectedStepId, selectStepById } = this.props

    let requestRemoveStep = (step) => {
      let rawSteps = brewery ? brewery.steps.map(s=> s.rawStep): [];
      rawSteps = rawSteps.filter(s => s !== step)
      requestUpdateStepList(rawSteps);
    }
    let step = brewery ? brewery.steps.find(s=>s.id == selectedStepId) : undefined;

    return (<div>

      {!brewery && requestStateStatus && requestStateStatus.active && (<div>Loading state</div>) }
      { brewery && (
      <div>
        <div className="container-fluid">
          {step && (
          <div className="row">
            <div key={step.id} className="col-sm-12 col-md-12 col-lg-12"><Step step={step} requestUpdateStep={requestUpdateStep} requestRemoveStep={requestRemoveStep} /></div>
          </div>
          )}
        </div>

        <StepList steps={brewery.steps} selectedStepId={selectedStepId} selectStepById={selectStepById} />
        <AddStep steps={brewery.steps} requestUpdateStepList={requestUpdateStepList} configuration={configuration} />
        <Sensors sensors={brewery.sensors} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/>
      </div>

    ) }

       </div>)
  }
}
