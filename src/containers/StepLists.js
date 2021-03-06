import { connect } from 'react-redux'
import { requestUpdateConfiguration } from '../actions/configuration.js'
import { updateStepList } from '../actions/status.js'

import Component from '../components/StepLists'

const mapStateToProps = (state) => {
  let stepLists = state.server.configuration ? state.server.configuration.stepLists : undefined;
  return {
    stepLists,
    configuration: state.server.configuration,
    status: state.server.status
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    requestUpdateStepList: (steps) => {
      dispatch(updateStepList(steps));
    },
    requestUpdateConfiguration: (cfg) => {
      dispatch(requestUpdateConfiguration(cfg));
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
