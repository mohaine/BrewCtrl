import React, { Component, PropTypes } from 'react'

import Step from '../components/Step'
import Mode from '../containers/Mode'
import Sensors from '../components/Sensors'
import AddStep from '../components/AddStep'


export default class Brewery extends Component {
  componentDidMount() {
    let { brewery,requestState, requestStateStatus } = this.props
    if(!brewery){
        requestState();
    }
  }
  render() {
    let { brewery ,requestState, requestStateStatus, requestUpdateStep, configuration, updateConfiguration, requestUpdateStepList } = this.props
    return (<div>

      <Mode />

      {!brewery && requestStateStatus && requestStateStatus.active && (<div>Loading state</div>) }
      { brewery && (
      <div>
        <div className="container-fluid">
          <div className="row">
            {brewery.steps.map(step=> (<div key={step.id} className="col-sm-12 col-md-12 col-lg-12"><Step step={step} requestUpdateStep={requestUpdateStep} /></div>))}
          </div>
        </div>

        <AddStep steps={brewery.steps} requestUpdateStepList={requestUpdateStepList} configuration={configuration} />

        <Sensors sensors={brewery.sensors} configuration={configuration} updateConfiguration={updateConfiguration}/>

      </div>


    ) }

       </div>)
  }
}
