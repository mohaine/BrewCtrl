
import React, { Component, PropTypes } from 'react'

import config from 'config'
import {formatDate} from '../util/date'


export default class About extends Component {
  render() {
      return (
        <div className="primary">
          <div className="container">
            <div className="row">
              <div className="col-md-12">
                <div className="content-header">
                  <h1>About</h1>
                </div>
              </div>
            </div>
            <div className="row">
              <div className="col-md-2">
                Name
              </div>
              <div className="col-md-4">
                {config.name}
              </div>
            </div>
            <div className="row">
              <div className="col-md-2">
                Version Hash
              </div>
              <div className="col-md-4">
                {config.versionHash}
              </div>
            </div>
            <div className="row">
              <div className="col-md-2">
                Build Time
              </div>
              <div className="col-md-4">
                {formatDate(config.buildTime)}
              </div>
            </div>
          </div>
    </div>)
  }
}
