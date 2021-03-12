import React, { Component, PropTypes } from 'react'

import { formatTemp, convertF2C, formatTempWhole } from '../util/tempature'
import QuickPick from '../components/QuickPick'



export default class QuickPickTemp extends Component {
  constructor(props) {
    super(props);
    this.state = {
    }
  }
 
  render() {

    let { close , apply, value } = this.props

    return (<div>
     <QuickPick close={ close }
        apply={apply}
        quickPickValues={[{ value: convertF2C(120), text: formatTempWhole(convertF2C(120)) },
        { value: convertF2C(140), text: formatTempWhole(convertF2C(140)) },
        { value: convertF2C(153), text: formatTempWhole(convertF2C(153)) },
        { value: convertF2C(165), text: formatTempWhole(convertF2C(165)) },
        { value: convertF2C(205), text: formatTempWhole(convertF2C(205)) }]}
        increment={(value, up) => value + (up ? convertF2C(33) : -convertF2C(33))}
        value={value}
        formatValue={(temp) => formatTempWhole(temp)}
      />
    </div>)
  }
}
