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

    let { close , apply, value, extraItems } = this.props

    let dutyQuickPickItems = [
        {value:0,text:"0%"},
        {value:25,text:"25%"},
        {value:50,text:"50%"},
        {value:75,text:"75%"},
        {value:100,text:"100%"},
      ];
    
    if(extraItems){

        dutyQuickPickItems = [
            {value:0,text:"0%"},
            {value:33,text:"33%"},
            {value:66,text:"66%"},
            {value:100,text:"100%"},
          ];

        extraItems.forEach(item => dutyQuickPickItems.push(item))
    }

    return (<div>
   <QuickPick close={close}
        apply={apply}
        quickPickValues={dutyQuickPickItems}
        increment={(value,up)=>{
          if(! (parseInt(value, 10) > -1)){
            return up? 100: 0;
          }
          let newValue = value + (up?1:-1)
          if(newValue<0) newValue = 0;
          if(newValue>100) newValue = 100;
          return newValue;
        }}
        value={value}
        formatValue={(duty)=>{
          if(duty == 'AUTO'){
            return 'Auto'
          }
          return duty + "%"}}
        />
    </div>)
  }
}
