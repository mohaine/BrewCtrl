import { connect } from 'react-redux'
import { requestConfiguration,requestUpdateConfiguration } from '../actions/configuration.js'

import Component from '../components/Configuration'

const mapStateToProps = (state) => {
  let configuration = state.server.configuration;
  let requestConfigurationStatus = state.server.requestConfigurationStatus;
  let requestUpdateConfigurationStatus = state.server.requestUpdateConfigurationStatus;
  return {
    configuration,
    requestConfigurationStatus,
    requestUpdateConfigurationStatus
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    requestConfiguration: () => {
      dispatch(requestConfiguration())
    },
    requestUpdateConfiguration: (cfg,onComplete) => {
      dispatch(requestUpdateConfiguration(cfg,onComplete))
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
