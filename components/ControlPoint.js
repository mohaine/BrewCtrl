import React, { Component, PropTypes } from 'react'

import { createManualStep, findControlByIo, findTargetByAddress } from '../util/step'
import { formatTemp, convertF2C, formatTempWhole } from '../util/tempature'
import QuickPick from '../components/QuickPick'
import { overlayControlPoint } from '../util/step'



export default class ControlPoint extends Component {
  constructor(props) {
    super(props);
    this.state = {
      editTargetTemp: false
    }
  }
  updateTargetTemp(temp) {
    let { step, controlPoint, requestUpdateStep } = this.props
    requestUpdateStep(overlayControlPoint(step.rawStep, Object.assign({}, controlPoint, { targetTemp: temp })));
  }
  updateControl(tempSensorAddress) {
    let { step, controlPoint, requestUpdateStep } = this.props
    let automaticControl = !!tempSensorAddress
    requestUpdateStep(overlayControlPoint(step.rawStep, Object.assign({}, controlPoint, { automaticControl, tempSensorAddress })));
  }

  findSensorName(s) {
    let { configuration } = this.props
    let name = undefined
    let sensorTarget = findTargetByAddress(configuration, s.address)
    if (sensorTarget) {
      name = sensorTarget.name
    }
    if (!name) {
      name = s.name
    }
    if (!name) {
      name = s.address
    }
    return name
  }

  render() {

    let { configuration, controlPoint, status } = this.props

    let whatToControl = findControlByIo(configuration, controlPoint.controlIo)
    if(!whatToControl){
      whatToControl = {}
    }

    let sensorAddress = controlPoint.tempSensorAddress

    let automaticControl = !!sensorAddress

    let targetName = ""
    let targetTemp = ""
    if (automaticControl) {
      let target = findTargetByAddress(configuration, sensorAddress)
      if (target) {
        targetName = target.name
      } else {
        let targetSensor = status.sensors.find(s => s.address == sensorAddress)
        if (targetSensor) {
          targetName = this.findSensorName(targetSensor)
        } else {
          targetName = sensorAddress
        }
      }
      targetTemp = controlPoint.targetTemp
    }

    let sensors = []
    if (status && status.sensors) {
      status.sensors.forEach(s => {
        let name = this.findSensorName(s)
        sensors.push({ name: name, address: s.address })
      })
    }

    return (<div>
      {this.state.editTargetTemp && (<QuickPick close={() => { this.setState({ editTargetTemp: false }) }}
        apply={(value) => { this.updateTargetTemp(value) }}
        quickPickValues={[{ value: convertF2C(120), text: formatTempWhole(convertF2C(120)) },
        { value: convertF2C(140), text: formatTempWhole(convertF2C(140)) },
        { value: convertF2C(153), text: formatTempWhole(convertF2C(153)) },
        { value: convertF2C(165), text: formatTempWhole(convertF2C(165)) },
        { value: convertF2C(205), text: formatTempWhole(convertF2C(205)) }]}
        increment={(value, up) => value + (up ? convertF2C(33) : -convertF2C(33))}

        value={targetTemp}
        formatValue={(temp) => formatTempWhole(temp)}
      />)}

      <div className="card-deck">
        <div className="card">
          <h5 class="card-title">{whatToControl.name}</h5>
          <div className="card-body">
          
            {automaticControl && (<span>Keep</span>)}
            {!automaticControl && (<span>Control</span>)}
            <span> </span>              
            <select onChange={(e) => this.updateControl(e.target.value)} value={controlPoint.tempSensorAddress}>
              <option value=""> Manual</option>
              {sensors.map(s => (<option key={s.address} value={s.address}>{s.name}</option>))}
            </select>

              {automaticControl && (<span>
                <span></span><span> at <text onClick={() => this.setState({ editTargetTemp: true })}> {formatTempWhole(targetTemp)} </text></span> <span></span>
            </span>)}

          </div>
        </div>
      </div>
    </div>)
  }
}
