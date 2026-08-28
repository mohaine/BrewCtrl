import { connect } from 'react-redux'
import { useNavigate } from 'react-router-dom'

import Link from '../components/Link'

const mapDispatchToProps = (dispatch,ownProps) => {
  return {
    onClick: (route) => {
      ownProps.navigate('/brewctrl/' + ownProps.route)
    }
  }
}

const ConnectedLink = connect(null,mapDispatchToProps)(Link)

export default function RouteLink(props) {
  const navigate = useNavigate()
  return <ConnectedLink {...props} navigate={navigate} />
}
