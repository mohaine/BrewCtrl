import React, { Component, PropTypes } from 'react'

export default class ContentEditable extends Component {
  constructor(props) {
      super(props);
      this.state = {
        html: props.html
      }
  }


  render(){
      let text = this.state.html
      let placeholder = this.props.placeholder
      if(placeholder == undefined){
        placeholder = "Click to enter"
      }

      return <input style={{display: "inline-block"}}
          onInput={(e) => this.emitChange(e)}
          onBlur={(e) => this.emitChange(e)}
          onChange={(e) => {}}
          value={text} placeholder={placeholder} style={{"border":"none"}}
          />;
  }

  emitChange(e){
      var html =  e.target.value;
      if (this.props.onChange && html !== this.state.html) {
          this.props.onChange({
              target: {
                  value: html
              }
          });
      }
      this.setState({html})
  }
}
