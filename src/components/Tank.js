import React, { Component, PropTypes } from 'react'

import {formatTemp,convertF2C,formatTempWhole} from '../util/tempature'
import QuickPick from '../components/QuickPick'
import QuickPickTemp from '../components/QuickPickTemp'
import QuickPickPercent from '../components/QuickPickPercent'

import {overlayControlPoint} from '../util/step'

export default class Tank extends Component {
  constructor(props) {
      super(props);
      this.state = {
        editTargetTemp: false,
        editElementDuty: false
      }
  }
  updateTargetTemp(temp){
    let { step, tank, requestUpdateStep } = this.props
    let heater = tank ? tank.heater : undefined;
    requestUpdateStep(overlayControlPoint(step.rawStep, Object.assign({}, heater, { targetTemp: temp }) ));
  }
  updateElementDuty(duty){
    let { step, tank, requestUpdateStep } = this.props
    let sensor = tank ? tank.sensor : undefined;
    let heater = tank ? tank.heater : undefined;

    let automaticControl = duty === 'AUTO';

    let newControlPoint = Object.assign({}, heater, { automaticControl: automaticControl })

    if(automaticControl){
      newControlPoint.tempSensorAddress = sensor.address;
      newControlPoint.targetName = tank.name;
    } else {
      newControlPoint.duty = duty;
    }

    requestUpdateStep(overlayControlPoint(step.rawStep,newControlPoint));
  }

  render() {
    let { tank } = this.props

    let sensor = tank ? tank.sensor : undefined;
    let heater = tank ? tank.heater : undefined;


    let extraItems = []
    if(heater  && sensor){
      extraItems.push({value:"AUTO",text:"Auto"});
    }

    return (<div>
        {this.state.editTargetTemp && (<QuickPickTemp close={() => {this.setState({editTargetTemp:false})}} apply={(value) => { this.updateTargetTemp(value) }} value={heater.targetTemp}/>)}

        { this.state.editElementDuty && (<QuickPickPercent close={()=>{this.setState({editElementDuty: false })}} apply={(value)=>{this.updateElementDuty(value)}}
            value={heater.automaticControl? "AUTO" : heater.duty} extraItems={extraItems}
          />)}


        { tank && (

          <svg
             className="tank"
             width="233.05785"
             height="266.55518"
             id="svg2"
             version="1.1">
            <g
               transform="matrix(0.54055162,0,0,0.85687171,-51.960455,-186.74186)"
               id="g3048">
              <path
                 style={{"fill":"#ffffff","fillOpacity":"1","fillRule":"evenodd","stroke":"#000000","strokeWidth":"1px","strokeLinecap":"butt","strokeLinejoin":"miter","strokeOpacity":"1"}}
                 d="m 96.624878,260.89238 0,225.53144 0.06252,0 c -0.01961,0.23029 -0.06252,0.45575 -0.06252,0.68693 0,22.86125 96.292402,41.40312 215.074092,41.40312 118.78169,0 215.07408,-18.54187 215.07408,-41.40312 0,-0.66194 -0.0904,-1.31277 -0.25008,-1.96712 l 0,-224.25125 -429.898092,0 z"
                 id="path2985-0" />
              <path
                 style={{"fill":"#aaaaaa","fillOpacity":"1","fillRule":"evenodd","stroke":"#000000","strokeWidth":"1px","strokeLinecap":"butt","strokeLinejoin":"miter","strokeOpacity":"1"}}
                 id="path2985"
                 d="m 525.71428,262.36218 c 0,22.88037 -95.93899,41.42857 -214.28572,41.42857 -118.34674,0 -214.285722,-18.5482 -214.285722,-41.42857 0,-22.88036 95.938982,-41.42857 214.285722,-41.42857 118.34673,0 214.28572,18.54821 214.28572,41.42857 z"
                 transform="matrix(1.0033295,0,0,0.9999941,-0.82767917,-2.4978663)" />
            </g>
             {heater && (
             <g id="heatElement"  onClick={()=>  this.setState({editElementDuty: true })}>
              <rect
                 y="160.48915"
                 x="67.414841"
                 height="75"
                 width="163"
                 id="rect3539"
                 style={{"fill":"#fff","fillRule":"evenodd","stroke":"none"}} />
              <text
                 id="text3809"
                 y="203.17743"
                 x="87.723328"
                 style={{"fontStyle":"normal","fontWeight":"normal","fontStretch":"normal","fontSize":"45.52684402px","lineHeight":"125%","textAlign":"end","letterSpacing":"0px","wordSpacing":"0px","writingMode":"lrTb","textAnchor":"start",fill: heater.automaticControl?"#666": "#000","fillOpacity":"1","stroke":"none"}}>
                <tspan
                   y="203.17743"
                   x="87.723328"
                   id="heatElementText">{heater.duty + "%"}</tspan>
              </text>
              <path
                 id="path3809"
                 d="m 231.75349,205.38902 -9.82673,0 0,4.945 -142.892138,0 0,5.0413 140.620418,0 0,5.04129 -140.620418,0 0,5.04131 142.892138,0 0,3.77638 9.82673,0 z"
                 style={{"stroke":"#000000","strokeWidth":"1.13817108px","strokeLinecap":"butt","strokeLinejoin":"miter","strokeOpacity":"1",
                 fill: heater.on? heater.automaticControl?"#faa": "#f00": "#fff" }}
                 className="element" />
            </g>)}
            <text
               id="tankName"
               y="50.285362"
               x="115.35722"
               style={{"fontStyle":"normal","fontVariant":"normal","fontWeight":"normal","fontStretch":"normal","fontSize":"45.52684402px","lineHeight":"125%","textAlign":"center","letterSpacing":"0px","wordSpacing":"0px","writingMode":"lrTb","textAnchor":"middle","fill":"#000000","fillOpacity":"1","stroke":"none"}}>
              <tspan
                 y="50.285362"
                 x="115.35722"
                 id="tankNameText">{tank.name}</tspan>
            </text>

            {sensor && (
            <g
               id="temperatures"
               transform="matrix(1.1381711,0,0,1.1381711,-109.40613,-248.04645)">
              <path
                 transform="translate(95.762569,225.71119)"
                 id="path3002"
                 d="m 3.3299614,58.894094 143.9283286,0 0,71.039176 -143.9283286,0 z"
                 style={{"fill":"#ffffff"}} />
              <text
                 className="temp"
                 id="text3809-4"
                 y="315.89194"
                 x="98.446396"
                 style={{"fontStyle":"normal","fontVariant":"normal","fontWeight":"normal","fontStretch":"normal","fontSize":"40px","lineHeight":"125%","textAlign":"start","letterSpacing":"0px","wordSpacing":"0px","writingMode":"lrTb","textAnchor":"start","fill":"#000000","fillOpacity":"1","stroke":"none"}}>
                <tspan
                   y="315.89194"
                   x="98.446396"
                   id="tempatureText">{formatTemp(sensor.temperatureC)}</tspan>
              </text>
              {heater && heater.automaticControl && (  <text onClick={()=> this.setState({editTargetTemp: true })}
                 id="targetTemp"
                 y="340.66696"
                 x="99.786003"
                 style={{"fontStyle":"normal","fontVariant":"normal","fontWeight":"normal","fontStretch":"normal","fontSize":"28px","lineHeight":"125%","textAlign":"start","letterSpacing":"0px","wordSpacing":"0px","writingMode":"lrTb","textAnchor":"start","fill":"#000000","fillOpacity":"1","stroke":"none"}}>
                <tspan
                   y="340.66696"
                   x="99.786003"
                   id="targetTempText">{"("+formatTempWhole(heater.targetTemp)+")"}</tspan>
              </text>)}
            </g>
          )}
          </svg>




        ) }
       </div>)
  }
}
