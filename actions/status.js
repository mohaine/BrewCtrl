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
    statusLoadInterval = setInterval(()=>{
      dispatch(requestStatusNoSchedule());
    }, 500);
  }
}


let requestStatusNoSchedule = (onComplete) => {
    let status = new RequestStatus();
    return dispatch => {
        rescheduleStatusLoad(dispatch);
        dispatch({
            type: 'REQUEST_STATUS',
            status: status.copy()
        });
        return axios.post(buildUrl('/cmd/status'), {})
            .then(json => {
                dispatch({
                    type: "RECEIVE_STATUS",
                    status: status.success(),
                    data: json.data
                })
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
