
import { push } from 'react-router-redux'



export const userErrorMessage = (e,defaultMessage) => {
  console.error(e);
  if(e &&  e.message){
    return e.message;
  }
  if(e && e.data && e.data.message){
    return e.data.message + " Error ID: " + e.data.id;
  }
  return defaultMessage;
}

export const buildUrl = (relative) => {
    return document.location.protocol +"//"+ document.location.host + relative
};

export const viewDashboard = () => {
    return dispatch => {
        dispatch(push('/msp/dashboard'))
    }
}
export const viewRoute = (path) => {
    return dispatch => {
        dispatch(push('/brewctrl/' + path))
    }
}
