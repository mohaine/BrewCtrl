import { connect } from 'react-redux'

import { viewRoute} from '../actions/'

import Link from '../components/Link'

const mapStateToProps = (state,ownProps) => {
   return {  };
}

const mapDispatchToProps = (dispatch,ownProps) => {
  return {
    onClick: (route) => {
      dispatch(viewRoute(ownProps.route));
    }
  }
}

export default connect(mapStateToProps,mapDispatchToProps)(Link)
