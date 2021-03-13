import { connect } from 'react-redux'

import { viewRoute} from '../actions/'

import App from '../components/App'

const mapStateToProps = (state,ownProps) => {
   return { location: state.router.location };
}

const mapDispatchToProps = (dispatch,ownProps) => {
  return {
    route: (route) => {
      dispatch(viewRoute(route));
    }
  }
}

export default connect(mapStateToProps,mapDispatchToProps)(App)
