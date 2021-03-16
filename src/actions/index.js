
import { push } from 'connected-react-router'

import {config} from '../config'


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
    return config.baseUrl + relative
};

export const viewRoute = (path) => {
  return dispatch => {
        dispatch(push('/brewctrl/' + path))
    }
}
