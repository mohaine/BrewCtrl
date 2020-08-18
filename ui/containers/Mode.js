import { connect } from 'react-redux'
import { changeMode } from '../actions/status.js'
import { requestConfiguration } from '../actions/configuration.js'
import { requestStatus } from '../actions/status.js'

import Component from '../components/Mode'

const mapStateToProps = (state) => {
  let brewery = state.server.brewery
  let requestStateStatus = state.server.requestStateStatus;

  return {
    brewery,requestStateStatus
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    changeMode: (mode) => {
      dispatch(changeMode(mode));
    },
    requestState: () => {
      dispatch(requestConfiguration())
      dispatch(requestStatus())
    }

  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
