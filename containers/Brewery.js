import { connect } from 'react-redux'
import { requestStatus,updateStep, updateStepList } from '../actions/status.js'
import { requestConfiguration, updateConfiguration } from '../actions/configuration.js'

import Component from '../components/Brewery'

const mapStateToProps = (state) => {
  let brewery = state.server.brewery
  let configuration = state.server.configuration
  let requestStateStatus = state.server.requestStateStatus;
  return {
    brewery,configuration,
    requestStateStatus
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    requestState: () => {
      dispatch(requestConfiguration())
      dispatch(requestStatus())
    },
    requestUpdateStep: (step) => {
      dispatch(updateStep(step));
    },
    requestUpdateStepList: (step) => {
      dispatch(updateStepList(step));
    },
    updateConfiguration: (cfg) => {
      dispatch(updateConfiguration(cfg));
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
