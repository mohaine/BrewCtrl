import { connect } from 'react-redux'
import { useLocation, useNavigate } from 'react-router-dom'

import App from '../components/App'

const mapDispatchToProps = (dispatch,ownProps) => {
  return {
    route: (route) => {
      ownProps.navigate('/brewctrl/' + route)
    }
  }
}

const ConnectedApp = connect(null,mapDispatchToProps)(App)

export default function AppContainer() {
  const location = useLocation()
  const navigate = useNavigate()
  return <ConnectedApp location={location} navigate={navigate} />
}
