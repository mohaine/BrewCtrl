

export const selectStepById = (id) => {
    return dispatch => {
      dispatch({
          type: "SELECT_STEP",
          id: id
      })
    }
}
