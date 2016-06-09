import axios from 'axios'
import { push } from 'react-router-redux'

import { buildUrl, userErrorMessage } from '../actions'

import RequestStatus from './RequestStatus'

let statusLoadInterval = undefined;

export const cancelStatusLoad = () => {
  if(statusLoadInterval){
    clearInterval(statusLoadInterval);
    statusLoadInterval = undefined;
  }
}
const rescheduleStatusLoad = (dispatch) => {
    cancelStatusLoad();
    startStatusLoad(dispatch);
}
const startStatusLoad = (dispatch) => {
  if(!statusLoadInterval){
    // statusLoadInterval = setInterval(()=>{
    //   dispatch(requestStatusNoSchedule());
    // }, 500);
  }
}




export const changeMode = (mode) => {
    let status = new RequestStatus();
    let data = "mode=" + mode;

    let form = new FormData();
    form.append("mode", mode);

    return dispatch => {
        dispatch({
            type: 'REQUEST_CHANGE_MODE',
            status: status.copy()
        });
        return axios( {
                method: 'POST',
                url: buildUrl('/cmd/status'),
                data: data
            })
            .then(json => {
                dispatch(statusMsg(status,json))
            }).catch(e => {
                dispatch({
                    type: "ERROR_CHANGE_MODE",
                    status: status.error(userErrorMessage(e, "Configuration Load Failed"))
                })
            })
    }
}

function statusMsg(status,json){
  return {
      type: "RECEIVE_STATUS",
      status: status.success(),
      data: json.data
  };
}



let requestStatusNoSchedule = (onComplete) => {
    let status = new RequestStatus();
    return dispatch => {
        rescheduleStatusLoad(dispatch);
        dispatch({
            type: 'REQUEST_STATUS',
            status: status.copy()
        });
        return axios.get(buildUrl('/cmd/status'))
            .then(json => {
                dispatch(statusMsg(status,json))
                if (onComplete) {
                    onComplete();
                }
            })
            .catch(e => {
                dispatch({
                    type: "ERROR_STATUS",
                    status: status.error(userErrorMessage(e, "Status Load Failed"))
                })
            })
    }
}


export const requestStatus = (onComplete) => {
    let status = new RequestStatus();
    return dispatch => {
        rescheduleStatusLoad(dispatch);

        requestStatusNoSchedule()(dispatch)
    }
}
