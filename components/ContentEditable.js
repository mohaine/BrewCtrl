import React, { Component, PropTypes } from 'react'

export default class ContentEditable extends Component {
  constructor(props) {
      super(props);
      this.state = {
        html: props.html
      }
  }


  render(){
      return <div style={{display: "inline-block"}}
          onInput={(e) => this.emitChange(e)}
          onBlur={(e) => this.emitChange(e)}
          contentEditable
          dangerouslySetInnerHTML={{__html: this.state.html}}></div>;
  }

  emitChange(e){
      var html =  e.target.innerText;
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
