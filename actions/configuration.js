import axios from 'axios'
import { push } from 'react-router-redux'

import { buildUrl, userErrorMessage } from '../actions'

import RequestStatus from './RequestStatus'



export const updateConfiguration = (configuration) => {
    let status = new RequestStatus();
    let data = "configuration=" + encodeURI(JSON.stringify(configuration));

    return dispatch => {
        dispatch({
            type: 'REQUEST_CHANGE_CONFIGURATION',
            status: status.copy()
        });
        return axios( {
                method: 'POST',
                url: buildUrl('/cmd/configuration'),
                headers: {"Content-Type":"application/x-www-form-urlencoded; charset=UTF-8"},
                data: data
            })
            .then(json => {
              dispatch({
                  type: "SUCCESS_CHANGE_CONFIGURATION",
                  status: status.success()
              })
              dispatch(receiveConfig(status,json))
            }).catch(e => {
                dispatch({
                    type: "ERROR_CHANGE_CONFIGURATION",
                    status: status.error(userErrorMessage(e, "Configuration Load Failed"))
                })
            })
    }
}

function receiveConfig(status,json){
  return {
      type: "RECEIVE_CONFIG",
      status: status.success(),
      configuration: json.data
  }
}


export const requestConfiguration = (onComplete) => {
    let status = new RequestStatus();
    return dispatch => {
        dispatch({
            type: 'REQUEST_CONFIG',
            status: status.copy()
        });
        return axios.get(buildUrl('/cmd/configuration'))
            .then(json => {
                dispatch(receiveConfig(status,json))
                if (onComplete) {
                    onComplete();
                }
            })
            .catch(e => {
                dispatch({
                    type: "ERROR_CONFIG",
                    status: status.error(userErrorMessage(e, "Configuration Load Failed"))
                })
            })
    }
}
