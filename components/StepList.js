import React, { Component, PropTypes } from 'react'


export default class StepList extends Component {
  render() {

    let { steps, selectedStepId, selectStepById} = this.props
    return (<div>
        {steps && (steps.map(step=> (
          <button type="button" className={"btn " + (selectedStepId === step.id ?"down":"btn-default")} key={step.id} onClick={()=>selectStepById(step.id)} >
            {step.name}
          </button>
           )))}
       </div>)
  }
}
