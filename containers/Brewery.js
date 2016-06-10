import { connect } from 'react-redux'
import { requestStatus,updateStep } from '../actions/status.js'
import { requestConfiguration } from '../actions/configuration.js'

import Component from '../components/Brewery'

const mapStateToProps = (state) => {
  let brewery = state.server.brewery
  let requestStateStatus = state.server.requestStateStatus;
  return {
    brewery,
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
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
