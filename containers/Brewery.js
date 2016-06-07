import { connect } from 'react-redux'
import { requestStatus } from '../actions/status.js'
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
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
