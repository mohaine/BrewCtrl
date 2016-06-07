

function buildBreweryView(status,configuration){
  if(status && configuration){
      let brewery = Object.assign({}, configuration.brewLayout, { });
      delete brewery.maxAmps;
      return brewery;
  }
}


export default (state = {}, action) => {
  switch (action.type) {

    case 'ERROR_CONFIG':
    case 'REQUEST_CONFIG':
      return Object.assign({}, state, {
        requestConfigurationStatus: action.status
      })
    case 'RECEIVE_CONFIG':
      return Object.assign({}, state, {
        configuration: action.configuration,
        brewery: buildBreweryView(state.status,action.configuration),
        requestConfigurationStatus: null,
      })

    case 'ERROR_STATUS':
    case 'REQUEST_STATUS':
      return Object.assign({}, state, {
        requestStatusStatus: action.status
      })

    case 'RECEIVE_STATUS':
      return Object.assign({}, state, {
        status: action.data,
        brewery: buildBreweryView(action.status,state.configuration),
        requestStatusStatus: null,
      })


    default:
      return state
  }
}
