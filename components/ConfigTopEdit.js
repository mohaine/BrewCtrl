
import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import {emptyGpios} from '../util/gpio'
import ContentEditable from '../components/ContentEditable'


export default class ConfigTopEdit extends Component {

  constructor(props) {
      super(props);
      this.state = {
        maxAmps: props.configuration.brewLayout.maxAmps
      }
  }


  overlayUpdate(overlay,remove){
    if(this.overlay){
      this.overlay = Object.assign({}, this.overlay, overlay);
    }
    let { configuration, requestUpdateConfiguration } = this.props
    let brewLayout = configuration.brewLayout


    // Create new object tree
    brewLayout = Object.assign({}, brewLayout, overlay);
    configuration = Object.assign({}, configuration, {brewLayout});

    if(this.updateCfgTimer){
      clearTimeout(this.updateCfgTimer);
      this.updateCfgTimer = undefined;
    }

    this.updateCfgTimer = setTimeout(()=> {
      requestUpdateConfiguration(configuration)
      this.updateCfgTimer = undefined;
    },500);
  }

  updateMaxAmps(maxAmps){
    maxAmps = parseInt(maxAmps,10)
    this.setState({maxAmps})
    this.overlayUpdate({maxAmps});
  }

  render() {
  // let { control, configuration } = this.props
  let {maxAmps} = this.state

  return (
    <div className="container-fluid">
      <div className="panel">
        <div className="panel-heading">
          <h2>Configuration</h2>
        </div>
        <div className="panel-body">
        <div className="row">
          <div className="col-sm-2">
            Max System Amps:
          </div>
          <div className="col-sm-1">
            <input type="number" value={maxAmps} onChange={(e)=>this.updateMaxAmps(e.target.value)} style={{width: "3em"}}/>
          </div>
          </div>
        </div>
      </div>
    </div>
          )
  }
}
