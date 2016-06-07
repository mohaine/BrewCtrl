import { connect } from 'react-redux'
import { requestConfiguration } from '../actions/configuration.js'

import Component from '../components/Configuration'

const mapStateToProps = (state) => {
  let configuration = state.configuration.current;
  let requestConfigurationStatus = state.configuration.requestConfigurationStatus;
  return {
    configuration,
    requestConfigurationStatus
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    requestConfiguration: () => {
      dispatch(requestConfiguration())
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
