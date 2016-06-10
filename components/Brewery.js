import React, { Component, PropTypes } from 'react'

import Step from '../components/Step'
import Mode from '../containers/Mode'


export default class Brewery extends Component {
  componentDidMount() {
    let { brewery,requestState, requestStateStatus } = this.props
    if(!brewery){
        requestState();
    }
  }
  render() {
    let { brewery ,requestState, requestStateStatus, requestUpdateStep } = this.props
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
      </div>


    ) }
       </div>)
  }
}
