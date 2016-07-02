
import React, { Component, PropTypes } from 'react'


import {createManualStep} from '../util/step'

import config from 'config'


export default class AddStep extends Component {
  addStep(){

    let {configuration, steps, requestUpdateStepList } = this.props

    let step = createManualStep(configuration);

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
