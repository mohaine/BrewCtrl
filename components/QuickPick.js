
import React, { Component, PropTypes } from 'react'

import config from 'config'


export default class QuickPick extends Component {
  constructor(props) {
      super(props);
      this.state = {
        value: props.value
      }
  }

  close(){
    let { close } = this.props
    this.cancelMouseDown();
    close();
  }

  setValue(value){
    this.setState({value: value});
    this.cancelApply();
    if(value != this.appliedValue){
      this.applyValueTimer = setTimeout(()=>{
        this.apply(value);
      }, 500);
    }
  }

  cancelApply(){
    if(this.applyValueTimer){
      clearTimeout(this.applyValueTimer);
      this.applyValueTimer = undefined;
    }
  }

  apply(value){
    let { apply } = this.props
    this.cancelMouseDown();
    this.cancelApply();
    apply(value);
    this.setState({appliedValue: value});
  }

  mouseDown(up) {
    let { increment } = this.props
		let count = 0;
		let timeoutFunction = () => {
			this.cancelMouseDown();
      this.setValue(increment(this.state.value, up));
			count++;
			var delay = 300;
			if (count > 3) {
				delay = 200;
			} else if (count > 7) {
				delay = 100;
			}
			this.mouseDownTimeout = setTimeout(timeoutFunction, delay);
		};
		timeoutFunction();

	}
  cancelMouseDown(){
    if(this.mouseDownTimeout){
      clearTimeout(this.mouseDownTimeout);
      this.mouseDownTimeout = undefined;
    }
  }
	mouseUp() {
    this.cancelMouseDown();
	}
  render() {
    let { quickPickValues, formatValue } = this.props
      return (
        <div>
        <div  onClick={(e) => {e.preventDefault(); this.close();}} style={{
          position: 'fixed',
          width: '100%',
          height: '100%',
          margin: '0',
          padding: '0',
          top: '0',
          left: '0',
          opacity: '0.2',
          backgroundColor: '#000',
          zIndex: 1
        }}>
        </div>
      <div  style={{backgroundColor: "#fff",position: "absolute", top: "0px",right: "0px", zIndex: 11, border: "1px solid black"}} >
        <div style={{ width: '200px',position: 'relative',height: '190px',display: 'inline-block'}}>
          <div style={{ marginRight: '75px', padding: '7px'}}>
            <svg
            className="tank"
            width="120.29716"
            height="154.28125"
            id="svg2"
            version="1.1" >
            <g
            id="g2985">
            <text
            style={{  fontSize: '43.14793015px',
              fontStyle: 'normal',
              fontVariant: 'normal',
              fontWeight: 'normal',
              fontStretch: 'normal',
              textAlign: 'center',
              lineHeight: '125%',
              letterSpacing: '0px',
              wordSpacing: '0px',
              writingMode: 'lr-tb',
              textAnchor: 'middle',
              fill: '#000000',
              fillOpacity: '1',
              stroke: 'none'}}
            x="59.116882"
            y="89.545815"
            id="text3001"><tspan
            y="89.545815"
            x="59.116882"
            id="textValue"
            >{formatValue(this.state.value)}</tspan></text>
            <path
            style={{  fill: '#000000',
              fillOpacity: '1',
              stroke: '#000000',
              strokeWidth: '1.34837282px',
              strokeLinecap: 'butt',
              strokeLinejoin: 'miter',
              strokeOpacity: '1',
              cursor: "pointer"}}
            d="M 61.622007,2.8866864 9.0592677,45.924629 l 105.1254723,0 z"
            id="upOne" onMouseDown={()=>this.mouseDown(true)} onMouseUp={()=>this.mouseUp()}
            />
            <path
            style={{  fill: '#000000',
              fillOpacity: '1',
              stroke: '#000000',
              strokeWidth: '1.34837282px',
              strokeLinecap: 'butt',
              strokeLinejoin: 'miter',
              strokeOpacity: '1',
              cursor: "pointer"}}
            d="m 61.622007,151.40286 -52.5627393,-43.03795 105.1254623,0 z"
            id="downOne" onMouseDown={()=>this.mouseDown(false)} onMouseUp={()=>this.mouseUp()} />
            </g>
            </svg>
          </div>
          <div style={{position: 'absolute',top: '0px',right: '0px', width: '75px'}}>
            {quickPickValues && quickPickValues.map(quickPick => (<button type="button" onClick={()=>{this.setValue(quickPick.value)}} key={quickPick.value}
              style={{ width: '75px',
               fontSize: '16px',
               fontWeight: 'bold',
               padding: '7px',
               backgroundColor: "#fff",
               border: "none"}}
              >{quickPick.text}</button>))}
          </div>
        </div>
        </div>
        </div>


      )
  }
}
