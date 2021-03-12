import React, { Component, PropTypes } from 'react'

import { createManualStep, findControlByIo, findTargetByAddress } from '../util/step'
import { formatTemp, convertF2C, formatTempWhole } from '../util/tempature'
import QuickPickTemp from '../components/QuickPickTemp'
import QuickPickPercent from '../components/QuickPickPercent'
import { overlayControlPoint } from '../util/step'



export default class ControlPoint extends Component {
  constructor(props) {
    super(props);
    this.state = {
      editTargetTemp: false,
      editMaxDuty: false
    }
  }

  updateMaxDuty(temp) {
    let { step, controlPoint, requestUpdateStep } = this.props
    requestUpdateStep(overlayControlPoint(step.rawStep, Object.assign({}, controlPoint, { MaxDuty: temp })));
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
    if (!whatToControl) {
      whatToControl = {}
    }


    let hasDuty = controlPoint.hasDuty
    let maxDuty = controlPoint.MaxDuty

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
      {this.state.editTargetTemp && (<QuickPickTemp close={() => {this.setState({editTargetTemp:false})}} apply={(value) => { this.updateTargetTemp(value) }} value={targetTemp}/>)}
      {this.state.editMaxDuty && (<QuickPickPercent close={() => {this.setState({editMaxDuty:false})}} apply={(value) => { this.updateMaxDuty(value) }} value={maxDuty}/>)}

      <li className="list-group-item">
        <strong>{whatToControl.name}</strong>

        {automaticControl && (<span> keeps </span>)}
        {!automaticControl && (<span> is </span>)}
        <span> </span>
        <select onChange={(e) => this.updateControl(e.target.value)} value={controlPoint.tempSensorAddress}>
          <option value=""> Manual</option>
          {sensors.map(s => (<option key={s.address} value={s.address}>{s.name}</option>))}
        </select>

        {automaticControl && (<span>
          <span></span><span> at <span className="clickable" onClick={() => this.setState({ editTargetTemp: true })}> {formatTempWhole(targetTemp)} </span></span> <span></span>
        </span>)}

        { hasDuty && (<span>
          <span></span><span> Max Power: <span className="clickable" onClick={() => this.setState({ editMaxDuty: true })}> {maxDuty} </span> %</span> <span></span>
        </span>)}


      </li>
    </div>)
  }
}
