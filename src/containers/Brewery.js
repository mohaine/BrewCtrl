import { connect } from 'react-redux'
import { requestStatus,updateStep, updateStepList } from '../actions/status.js'
import { requestConfiguration, requestUpdateConfiguration } from '../actions/configuration.js'
import { selectStepById } from '../actions/ui.js'

import Component from '../components/Brewery'

const mapStateToProps = (state) => {
  let brewery = state.server.brewery
  let status = state.server.status
  let configuration = state.server.configuration
  let requestStateStatus = state.server.requestStateStatus;
  let selectedStepId = state.ui.selectedStepId;

  return {
    brewery,status,configuration,
    requestStateStatus,selectedStepId
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
    requestUpdateStepList: (steps) => {
      dispatch(updateStepList(steps));
    },
    requestUpdateConfiguration: (cfg) => {
      dispatch(requestUpdateConfiguration(cfg));
    },
    selectStepById: (id)=>{
      dispatch(selectStepById(id));
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
