import { connect } from 'react-redux'
import { changeMode } from '../actions/status.js'
import { requestConfiguration } from '../actions/configuration.js'

import Component from '../components/Mode'

const mapStateToProps = (state) => {
  let brewery = state.server.brewery
  return {
    brewery,
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    changeMode: (mode) => {
      dispatch(changeMode(mode));
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
