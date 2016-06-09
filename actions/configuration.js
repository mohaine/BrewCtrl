import axios from 'axios'
import { push } from 'react-router-redux'

import { buildUrl, userErrorMessage } from '../actions'

import RequestStatus from './RequestStatus'



export const requestConfiguration = (onComplete) => {
    let status = new RequestStatus();
    return dispatch => {
        dispatch({
            type: 'REQUEST_CONFIG',
            status: status.copy()
        });
        return axios.get(buildUrl('/cmd/configuration'))
            .then(json => {
                dispatch({
                    type: "RECEIVE_CONFIG",
                    status: status.success(),
                    configuration: json.data
                })
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
