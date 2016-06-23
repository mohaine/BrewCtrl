import React, { Component, PropTypes } from 'react'

export default class ContentEditable extends Component {
  render(){
      return <div style={{display: "inline-block"}}
          onInput={(e) => this.emitChange(e)}
          onBlur={(e) => this.emitChange(e)}
          contentEditable
          dangerouslySetInnerHTML={{__html: this.props.html}}></div>;
  }

  emitChange(e){
      var html =  e.target.innerText;
      if (this.props.onChange && html !== this.lastHtml) {
          this.props.onChange({
              target: {
                  value: html
              }
          });
      }
      this.lastHtml = html;
  }
}
