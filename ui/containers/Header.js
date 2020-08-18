import { connect } from 'react-redux'

import Component from '../components/Header'

const mapStateToProps = (state) => {
  let lastStatusDate = state.server.lastStatusDate;
  return {lastStatusDate}
}

const mapDispatchToProps = (dispatch) => {
  return {
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Component)
