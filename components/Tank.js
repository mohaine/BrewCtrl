
import React, { Component, PropTypes } from 'react'

import {formatTemp,convertF2C,formatTempWhole} from '../util/tempature'
import QuickPick from '../components/QuickPick'
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

    let dutyQuickPickItems = [
      {value:0,text:"0%"},
      {value:33,text:"33%"},
      {value:66,text:"66%"},
      {value:100,text:"100%"},
    ];
    if(heater  && sensor){
      dutyQuickPickItems.push({value:"AUTO",text:"Auto"});
    }

    return (<div>
        { this.state.editTargetTemp && (<QuickPick close={()=>{this.setState({editTargetTemp: false })}}
        apply={(value)=>{this.updateTargetTemp(value)}}
        quickPickValues={ [{value:convertF2C(120),text:formatTempWhole(convertF2C(120))},
          {value:convertF2C(140),text:formatTempWhole(convertF2C(140))},
          {value:convertF2C(153),text:formatTempWhole(convertF2C(153))},
          {value:convertF2C(165),text:formatTempWhole(convertF2C(165))},
          {value:convertF2C(205),text:formatTempWhole(convertF2C(205))}]}
        increment={(value,up)=>value + (up?convertF2C(33):-convertF2C(33))}

        value={heater.targetTemp}
        formatValue={(temp)=>formatTempWhole(temp)}
        />)}
        { this.state.editElementDuty && (<QuickPick close={()=>{this.setState({editElementDuty: false })}}
        apply={(value)=>{this.updateElementDuty(value)}}
        quickPickValues={ dutyQuickPickItems}
        increment={(value,up)=>{
          if(value == 'AUTO'){
            return up? 0: 100;
          }
          let newValue = value + (up?1:-1)
          if(newValue<0) newValue = 0;
          if(newValue>100) newValue = 100;
          return newValue;
        }}
        value={heater.automaticControl? "AUTO" : heater.duty}
        formatValue={(duty)=>{
          if(duty == 'AUTO'){
            return 'Auto'
          }
          return duty + "%"}}
        />)}


        { tank && (
          <svg
             className="tank"
             width="205.48909"
             height="234.64206"
             id="svg2"
             version="1.1"
             >
            <g
               id="layer1"
               transform="translate(-95.762569,-217.71119)">
              <g
                 id="g3048"
                 transform="matrix(0.47493002,0,0,0.75284965,50.471915,53.862368)">
                <path
                   id="path2985-0"
                   d="m 96.624878,260.89238 0,225.53144 0.06252,0 c -0.01961,0.23029 -0.06252,0.45575 -0.06252,0.68693 0,22.86125 96.292402,41.40312 215.074092,41.40312 118.78169,0 215.07408,-18.54187 215.07408,-41.40312 0,-0.66194 -0.0904,-1.31277 -0.25008,-1.96712 l 0,-224.25125 -429.898092,0 z"
                   style={{fill:"#fff",fillOpacity:1,fillRule:"evenodd",stroke:"#000000",strokeWidth:"1px",strokeLinecap:"butt",strokeLinejoin:"miter",strokeOpacity:1}} />
                <path
                   transform="matrix(1.0033295,0,0,0.9999941,-0.82767917,-2.4978663)"
                   d="m 525.71428,262.36218 c 0,22.88037 -95.93899,41.42857 -214.28572,41.42857 -118.34674,0 -214.285722,-18.5482 -214.285722,-41.42857 0,-22.88036 95.938982,-41.42857 214.285722,-41.42857 118.34673,0 214.28572,18.54821 214.28572,41.42857 z"
                   id="path2985"
                   style={{fill:"#aaa",fillOpacity:1,fillRule:"evenodd",stroke:"#000000",strokeWidth:"1px",strokeLinecap:"butt",strokeLinejoin:"miter",strokeOpacity:1}} />

              </g>
              {heater && (
              <g id="heatElement"  onClick={()=>  this.setState({editElementDuty: true })}>
                <text
                   style={{
                     fontSize: '40px',
                     fontStyle: 'normal',
                     fontVariant: 'normal',
                     fontWeight: 'normal',
                     fontStretch: 'normal',
                     textAlign: 'end',
                     lineHeight: '125%',
                     letterSpacing: '0px',
                     wordSpacing: '0px',
                     writingMode: 'lr-tb',
                     textAnchor: 'start',
                     fill: heater.automaticControl?"#666": "#000",
                     fillOpacity: '1',
                     stroke: 'none'
                   }}
                   x="173.19844"
                   y="396.44644"
                   id="text3809"
                  >
                   <tspan id="heatElementText" x="173.19844" y="396.44644" >{heater.duty + "%"}</tspan>
                </text>
                <path
                   className="element"
                   style={{stroke:"#000000",strokeWidth:"1px",strokeLinecap:"butt",strokeLinejoin:"miter",strokeOpacity:1,
                   fill: heater.on? heater.automaticControl?"#faa": "#f00": "#fff"}}
                   d="m 299.7437,398.38955 -8.63379,0 0,4.34469 -125.54539,0 0,4.4293 123.54945,0 0,4.42929 -123.54945,0 0,4.4293 125.54539,0 0,3.31794 8.63379,0 z"
                   id="path3809"
                    />
              </g>)}

                <text
                 style={{fontSize: '40px',fontStyle: 'normal',fontVariant: 'normal',             fontWeight: 'normal',                   fontStretch: 'normal',                   textAlign: 'center',                   lineHeight: '125%',
                   letterSpacing: '0px',
                   wordSpacing: '0px',
                   writingMode: 'lr-tb',
                   textAnchor: 'middle',
                   fill: '#000000',
                   fillOpacity: '1',
                   stroke: 'none'}}
                 x="197.47765"
                 y="262.11508"
                 id="tankName"
                 ><tspan
                   id="tankNameText"
                   x="197.47765"
                   y="262.11508">{tank.name}</tspan></text>

                   {sensor && (

                   <g id="temperatures">
                       <path
                 style={{
                   fill: '#fff',
                   stroke: '#none'
                 }}
                 d="m 3.3299614,58.894094 143.9283286,0 0,71.039176 -143.9283286,0 z"
                 id="path3002"

                 transform="translate(95.762569,225.71119)"
                  />
              <text
                 style={{
                   fontSize: '40px',
                   fontStyle: 'normal',
                   fontVariant: 'normal',
                   fontWeight: 'normal',
                   fontStretch: 'normal',
                   textAlign: 'start',
                   lineHeight: '125%',
                   letterSpacing: '0px',
                   wordSpacing: '0px',
                   writingMode: 'lr-tb',
                   textAnchor: 'start',
                   fill: sensor.reading? '#000': '#f00',
                   fillOpacity: '1',
                   stroke: 'none'
                 }}
                 x="98.446396"
                 y="315.89194"
                 id="text3809-4"

                 className="temp"><tspan
                   id="tempatureText"
                   x="98.446396"
                   y="315.89194">{formatTemp(sensor.temperatureC)}</tspan></text>

                   {heater && heater.automaticControl && (  <text onClick={()=> this.setState({editTargetTemp: true })}
                          style={{  fontSize: '28px',
                            fontStyle: 'normal',
                            fontVariant: 'normal',
                            fontWeight: 'normal',
                            fontStretch: 'normal',
                            textAlign: 'start',
                            lineHeight: '125%',
                            letterSpacing: '0px',
                            wordSpacing: '0px',
                            writingMode: 'lr-tb',
                            textAnchor: 'start',
                            fill: '#000000',
                            fillOpacity: '1',
                            stroke: 'none',
                            cursor: "pointer"}}
                          x="99.786003"
                          y="340.66696"
                          id="targetTemp"
                          ><tspan
                            id="targetTempText"
                            x="99.786003"
                            y="340.66696">{"("+formatTempWhole(heater.targetTemp)+")"}</tspan></text>)}
           </g>)}
            </g>
          </svg>


        ) }
       </div>)
  }
}
