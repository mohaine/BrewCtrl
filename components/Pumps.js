import React, { Component, PropTypes } from 'react'

import {formatTemp} from '../util/tempature'
import ContentEditable from '../components/ContentEditable'
import ControlEdit from '../components/ControlEdit'


export default class Sensors extends Component {
  render() {
    let { pumps, configuration,requestUpdateConfiguration} = this.props
  return (<div>
        Pumps
        {pumps &&  pumps.map(pump=>(<ControlEdit key={pump.id} control={pump} configuration={configuration} requestUpdateConfiguration={requestUpdateConfiguration}/>))}
       </div>)
  }
}
