import React, { Component, PropTypes } from 'react'

import {overlayControlPoint} from '../util/step'

export default class Pump extends Component {

  togglePump() {
    let { step, pump, requestUpdateStep } = this.props
    if(!pump.automaticControl){
      let newDuty = pump.duty > 0 ? 0 : 100;
      requestUpdateStep(overlayControlPoint(step.rawStep, Object.assign({}, pump, { duty: newDuty }) ));
  
    }
  }

  render() {
    let { step, pump, requestUpdateStep } = this.props
    let automaticControl = pump.automaticControl;
    let on = pump.duty > 0 ;
    let actuallyOn = pump.on && on;

    let onColor = automaticControl? "#cfc": "#0f0"
    let offColor = automaticControl? "#fcc":  "#f00"


    return (<div>
        { pump && (
          <svg onClick={()=>{this.togglePump()}}
             className="tank"
             width="97.796799"
             height="117.54825"
             version="1.1">
            <defs
               id="defs4" />
            <g
               className="pump"
               id="pump"
               transform="translate(226.70305,-193.125)">
              <g
                 id="g3120">
                <rect
                   y="193.59904"
                   x="-186.99043"
                   height="27.276783"
                   width="57.625454"
                   id="pumpBody"
                   style={{stroke:"#000000",strokeWidth:"0.93237054",strokeOpacity:1,fill: on?onColor:offColor }} />
                <path
                   transform="translate(90.890703,207.47211)"
                   d="m -234.94057,27.236113 c 0,22.685294 -18.39007,41.075359 -41.07536,41.075359 -22.68529,0 -41.07536,-18.390065 -41.07536,-41.075359 0,-22.685295 18.39007,-41.07536 41.07536,-41.07536 22.68529,0 41.07536,18.390065 41.07536,41.07536 z"
                   id="path3087"
                   style={{stroke:"#000000",strokeOpacity:1,fill: on?onColor:offColor}}  />
                <path
                   transform="translate(106.32668,219.2453)"
                   d="m -279.41709,15.462922 c 0,6.646647 -5.38817,12.034819 -12.03481,12.034819 -6.64665,0 -12.03482,-5.388172 -12.03482,-12.034819 0,-6.6466467 5.38817,-12.0348186 12.03482,-12.0348186 6.64664,0 12.03481,5.3881719 12.03481,12.0348186 z"
                   id="path3118"
                   style={{fill: (actuallyOn?"none":"#000")}}  />
              </g>
              <text
                 style={{
                   fontSize:"20px",
                   textAlign:"center",
                   lineHeight:"125%",
                   letterSpacing:"0px",
                   wordSpacing:"0px",
                   writingMode:"lr-tb",
                   textAnchor:"middle",
                   fill:"#000000",
                   fillOpacity:1,
                   stroke:"none",
                   fontFamily:"Sans"}}
                 x="-175.94751"
                 y="296.16818"
                 id="pumpName"
                ><tspan
                   id="pumpNameText"
                   x="-175.94751"
                   y="296.16818">{pump.name}</tspan></text>
            </g>
          </svg>



        ) }
       </div>)
  }
}
