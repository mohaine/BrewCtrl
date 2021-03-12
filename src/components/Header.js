import React, { Component, PropTypes } from 'react'

import Mode from '../containers/Mode'
import RouteLink from '../containers/RouteLink'

export default class Header extends Component {
  constructor(props, context) {
    super(props, context)
    this.state = {commLost: false}
  }
  componentDidMount(){
    this.statusCheckInterval = setInterval(()=>{
      let last = this.props.lastStatusDate;
      if(this.props.lastStatusDate){
        let now = new Date()
        let commLost = now.getTime() - last.getTime()  > 5000
        if(commLost!= this.state.commLost){
          this.setState({commLost})
        }
      }
    }, 1000)
  }
  componentWillUnmount(){
    if(this.statusCheckInterval){
      clearInterval(this.statusCheckInterval)
    }
  }
  render() {
        return (<div className="container-fluid" style={{paddingTop: "15px"}}>
          <div style={{float: "right"}}>
            <RouteLink route="brew">Brew</RouteLink> &nbsp;
            <RouteLink route="configuration">Configuration</RouteLink> &nbsp;
            <RouteLink route="steplists">Step Lists</RouteLink>
          </div>
          <Mode />

          {this.state.commLost && (<div>
          <div style={{zIndex: 20, position: "absolute", top: 0, left:0, height:"100%", width:"100%", background: "#fff",opacity: 0.75}}>
          </div>
          <div style={{zIndex: 21, position: "absolute", top: 20, left:20,background: "#fff",opacity: 1}}>
            <span className="alert alert-danger"> <strong>Server communiciation lost. Please check network or server status. Retrying...</strong> </span>
          </div>
          </div>)}

        </div>)
  }
}
