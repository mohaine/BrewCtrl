import React, { Component } from 'react'



export default class Mode extends Component {
  componentDidMount() {
    let { brewery, requestState } = this.props
    if (!brewery) {
      requestState();
    }
  }

  render() {
    let { brewery, changeMode } = this.props

    let mode = brewery ? brewery.mode : undefined;

    function button(text, current, mode) {
      let selected = current === mode;
      return (<button id="modeOn" style={{ marginRight: "3px" }} className={"btn btn-lg " + (selected ? "down" : "btn-default")} onClick={() => { if (!selected) changeMode(mode) }} >{text}</button>)
    }

    return (
      <div className="mode">
        {(button("On", mode, "ON"))}
        {(button("Hold", mode, "HOLD"))}
        {(button("Heat Off", mode, "HEAT_OFF"))}
        {(button("Off", mode, "OFF"))}
      </div>
    )
  }
}
