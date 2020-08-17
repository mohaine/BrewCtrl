import React, { Component, PropTypes } from 'react'

import Sensors from '../components/Sensors'
import Pumps from '../components/Pumps'
import Tanks from '../components/Tanks'
import ConfigTopEdit from '../components/ConfigTopEdit'

export default class Configuration extends Component {
  constructor(props) {
      super(props);
      this.state = {
      }
  }


  componentDidMount() {
    let { configuration,requestConfiguration, requestConfigurationStatus } = this.props
    if(!configuration){
        requestConfiguration();
    }
  }

  onChangeFile(fileInput){
    this.files = fileInput.files;
    this.fileInput = fileInput;
  }

  upload(form){
    let {requestUpdateConfiguration} = this.props;
    let files = this.files;
    let fileInput = this.fileInput;

    this.setState({info:undefined, alert:undefined});

    if (!files) {
    	this.setState({alert:"Please select a file."});
    	return;
    }

    if (files.length > 1) {
      this.setState({alert:"Please select a single file."});
    	return;
    }

    var reader = new FileReader();
		reader.onloadend = (evt) => {
			if (evt.target.readyState == FileReader.DONE) {
        requestUpdateConfiguration(JSON.parse(evt.target.result),()=>{
          this.setState({info:"Import Complete"});
          this.files = undefined;
          fileInput.form.reset();
        })
			}
		};
    reader.readAsBinaryString(files[0]);
  }

  render() {
    let { brewery ,configuration,requestConfiguration, requestConfigurationStatus ,requestUpdateConfiguration } = this.props

    return (<div>

      {this.state.alert && (<div className="alert alert-danger" role="alert">{this.state.alert}</div>)}
      {this.state.info && (<div className="alert alert-info" role="alert">{this.state.info}</div>)}

      {!configuration && requestConfigurationStatus && requestConfigurationStatus.active && (<div>Loading configuration</div>) }
      { configuration && (<ConfigTopEdit configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/>) }
      { configuration && brewery && <Sensors sensors={brewery.sensors} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/> }
      { configuration && <Pumps pumps={configuration.brewLayout.pumps} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/> }
      { configuration && <Tanks tanks={configuration.brewLayout.tanks} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/> }


      <hr />
      <div className="row">
        <div className="col-6 container">
        <div className="panel panel-default config-upload">
          <div className="panel-title">
          <label forHtml="configurationToUpload">Select a Configuration File to Upload</label><br />
          </div>
          <div className="panel-body">
              <form id="config-upload-form" method="post" action="/cmd/configuration">
                <input type="file" name="configuration" id="configurationToUpload" onChange={(e)=>this.onChangeFile(e.target) }/>
                <button  className="btn btn-default " type="button" onClick={(e)=>this.upload(e.target.form)} >Upload</button>
              </form>
          </div>
        </div>
        </div>
        <div className="col-6">
            <a href="/cmd/configuration" download="BrewControllerConfig.json">Download Current Configuration</a>
        </div>
      </div>


       </div>)
  }
}
