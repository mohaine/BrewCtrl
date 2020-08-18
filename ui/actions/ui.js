import axios from 'axios'
import { push } from 'react-router-redux'


export const selectStepById = (id) => {
    return dispatch => {
      dispatch({
          type: "SELECT_STEP",
          id: id
      })
    }
}
