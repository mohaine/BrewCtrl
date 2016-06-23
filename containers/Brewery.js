import { connect } from 'react-redux'
import { requestStatus,updateStep, updateStepList } from '../actions/status.js'
import { requestConfiguration, updateConfiguration } from '../actions/configuration.js'
import { selectStepById } from '../actions/ui.js'

import Component from '../components/Brewery'

const mapStateToProps = (state) => {
  let brewery = state.server.brewery
  let configuration = state.server.configuration
  let requestStateStatus = state.server.requestStateStatus;
  let selectedStepId = state.ui.selectedStepId;
  return {
    brewery,configuration,
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
    updateConfiguration: (cfg) => {
      dispatch(updateConfiguration(cfg));
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
