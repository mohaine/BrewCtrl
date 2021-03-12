
import React, { Component, PropTypes } from 'react'


export default class QuickPick extends Component {
  constructor(props) {
      super(props);
      this.state = {
        value: props.value
        // ,msg:""
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
    // this.setState({msg: this.state.msg + "D"})
    let { increment } = this.props
		let count = 0;
		let timeoutFunction = () => {
			this.cancelMouseDown();
      this.setValue(increment(this.state.value, up));
			count++;
			var delay = 300;
      if (count > 14) {
				delay = 25;
      } else if (count > 7) {
				delay = 100;
      } else if (count > 3) {
				delay = 200;
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
    // this.setState({msg: this.state.msg + "U"})
    this.cancelMouseDown();
	}
  touchEnd() {
    // this.setState({msg: this.state.msg + "E"})
    this.cancelMouseDown();
  }
  touchStart() {
    // this.setState({msg: this.state.msg + "S"})
  }
  render() {
    let { quickPickValues, formatValue, children } = this.props
      return (
        <div style={{position: "relative", display: "inline"}}>
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
      <div  style={{backgroundColor: "#fff",position: "absolute", top: "0px",left: "0px", zIndex: 11, border: "1px solid black"}} >
        <div style={{ width: '210px',position: 'relative',height: '190px',display: 'inline-block'}}>
          <div style={{ marginRight: '75px', padding: '7px'}}>

          <button style={{
            border:"none",
            padding:"2px",
            inset:"none",
            backgroundColor:"#fff",
            margin: "0px"
          }}
          onMouseDown={()=>this.mouseDown(true)} onMouseUp={()=>this.mouseUp()} onTouchStart={(e)=>{this.touchStart(e)}} onTouchEnd={(e)=>{this.touchEnd(e)}}>
          <svg
          width="120"
          height="60"
          id="svg2"
          version="1.1" >
          <path
          style={{  fill: '#000000',
            fillOpacity: '1',
            stroke: '#000000',
            strokeWidth: '1.34837282px',
            strokeLinecap: 'butt',
            strokeLinejoin: 'miter',
            strokeOpacity: '1',
            cursor: "pointer"}}
          d="M 60,0 0,60 l 120,0 z"
          id="upOne"  />
          </svg>
          </button>
          <div style={{ fontSize: '30px',
                        fontWeight: 'bold',
                        width: "120px",
                        textAlign: "center",
                        marginLeft: "3px"
                       }}
          >{formatValue(this.state.value)}</div>
          <button style={{
            border:"none",
            padding:"0px",
            inset:"none",
            backgroundColor:"#fff",
            margin: "0px"
          }}
          onMouseDown={()=>this.mouseDown(false)} onMouseUp={()=>this.mouseUp()} onTouchStart={(e)=>{this.touchStart(e)}} onTouchEnd={(e)=>{this.touchEnd(e)}}>
          <svg
          width="120"
          height="60"
          id="svg2"
          version="1.1" >
          <path
          style={{  fill: '#000000',
            fillOpacity: '1',
            stroke: '#000000',
            strokeWidth: '1.34837282px',
            strokeLinecap: 'butt',
            strokeLinejoin: 'miter',
            strokeOpacity: '1',
            cursor: "pointer"}}
            d="M 0,0 120,0 l -60,60 z"
          id="upDown"  />
          </svg>
          </button>
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
        <div>
          {children}
        </div>
        </div>
        </div>


      )
  }
}
