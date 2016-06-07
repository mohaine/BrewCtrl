import axios from 'axios'
import { push } from 'react-router-redux'

import { buildUrl, userErrorMessage } from '../actions'

import RequestStatus from './RequestStatus'

// let statusLoadInterval = undefined;
//
// export const cancelStatusLoad = () => {
//   if(statusLoadInterval){
//     clearInterval(statusLoadInterval);
//     statusLoadInterval = undefined;
//   }
// }
// const rescheduleStatusLoad = (dispatch) => {
//   if(statusLoadInterval){
//     cancelStatusLoad();
//     startStatusLoad();
//   }
// }
// const startStatusLoad = (dispatch) => {
//   if(!statusLoadInterval){
//     statusLoadInterval = setInterval(()=>{
//       requestCurrentStatus();
//     }, 1000 * 3);
//   }
// }


export const requestStatus = (onComplete) => {
    let status = new RequestStatus();
    return dispatch => {
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
