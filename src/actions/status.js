import axios from 'axios'

import { buildUrl, userErrorMessage } from '../actions'
import { config } from '../config'
import RequestStatus from './RequestStatus'



let statusLoadInterval = undefined;

let wsClient = undefined
let webSocketActive = false

export const cancelStatusLoad = () => {
    if (statusLoadInterval) {
        clearInterval(statusLoadInterval);
        statusLoadInterval = undefined;
    }
}
const rescheduleStatusLoad = (dispatch) => {
    cancelStatusLoad();
    startStatusLoad(dispatch);
}

const startStatusLoad = (dispatch) => {
    if (wsClient === undefined) {
        wsClient = new WebSocket(config.wsUrl);
        wsClient.onopen = () => {
            webSocketActive = true
            dispatch({ type: "WEB_SOCKET", active: true })
        };
        wsClient.onmessage = (message) => {
            let status = new RequestStatus();
            let json = JSON.parse(message.data)
            dispatch(statusMsg(status, { "data": json }))
        };
        wsClient.onerror = (event) => {
            console.error("WebSocket error observed:", event);
        };
        wsClient.onclose = (event) => {
            console.log('WebSocket Client Closed');
            dispatch({ type: "WEB_SOCKET", active: false})
            webSocketActive = false
            wsClient = undefined
            rescheduleStatusLoad(dispatch);
        };
    }

    if (!statusLoadInterval) {
        statusLoadInterval = setInterval(() => {
            if(webSocketActive){
                cancelStatusLoad()
            } else {
                dispatch(requestStatusNoSchedule());
            }
        }, 500);
    }
}

export const updateStepList = (steps) => {
    let status = new RequestStatus();
    let data = "steps=" + encodeURI(JSON.stringify(steps));
    return dispatch => {
        dispatch({
            type: 'REQUEST_CHANGE_STEPLIST',
            status: status.copy()
        });
        return axios({
            method: 'POST',
            url: buildUrl('/cmd/status'),
            headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
            data: data
        })
            .then(json => {
                dispatch({
                    type: "SUCCESS_CHANGE_STEPLIST",
                    status: status.success()
                })
                dispatch(statusMsg(status, json))
            }).catch(e => {
                dispatch({
                    type: "ERROR_CHANGE_STEPLIST",
                    status: status.error(userErrorMessage(e, "Configuration Load Failed"))
                })
            })
    }
}


export const updateStep = (step) => {
    let status = new RequestStatus();
    let data = "modifySteps=" + encodeURI(JSON.stringify([step]));
    return dispatch => {
        dispatch({
            type: 'REQUEST_CHANGE_STEP',
            status: status.copy()
        });
        return axios({
            method: 'POST',
            url: buildUrl('/cmd/status'),
            headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
            data: data
        })
            .then(json => {
                dispatch({
                    type: "SUCCESS_CHANGE_STEP",
                    status: status.success()
                })
                dispatch(statusMsg(status, json))
            }).catch(e => {
                dispatch({
                    type: "ERROR_CHANGE_STEP",
                    status: status.error(userErrorMessage(e, "Configuration Load Failed"))
                })
            })
    }
}



export const changeMode = (mode) => {
    let status = new RequestStatus();
    let data = "mode=" + mode;

    return dispatch => {
        dispatch({
            type: 'REQUEST_CHANGE_MODE',
            status: status.copy()
        });
        return axios({
            method: 'POST',
            url: buildUrl('/cmd/status'),
            headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
            data: data
        })
            .then(json => {
                dispatch({
                    type: "SUCCESS_CHANGE_MODE",
                    status: status.success()
                })
                dispatch(statusMsg(status, json))

            }).catch(e => {
                dispatch({
                    type: "ERROR_CHANGE_MODE",
                    status: status.error(userErrorMessage(e, "Configuration Load Failed"))
                })
            })
    }
}

function statusMsg(status, json) {
    return {
        type: "RECEIVE_STATUS",
        status: status.success(),
        data: json.data
    };
}


let currentlyRequestingStatus = false
let requestStatusNoSchedule = (onComplete) => {
    let status = new RequestStatus();
    return dispatch => {

        if (currentlyRequestingStatus) {
            return { type: 'SKIP' }
        }
        currentlyRequestingStatus = true

        rescheduleStatusLoad(dispatch);
        dispatch({
            type: 'REQUEST_STATUS',
            status: status.copy()
        });
        return axios.get(buildUrl('/cmd/status'))
            .then(json => {
                currentlyRequestingStatus = false
                dispatch(statusMsg(status, json))
                if (onComplete) {
                    onComplete();
                }
            })
            .catch(e => {
                currentlyRequestingStatus = false
                dispatch({
                    type: "ERROR_STATUS",
                    status: status.error(userErrorMessage(e, "Status Load Failed"))
                })
            })
    }
}


export const requestStatus = (onComplete) => {
    return dispatch => {
        rescheduleStatusLoad(dispatch);
        requestStatusNoSchedule()(dispatch)
    }
}
