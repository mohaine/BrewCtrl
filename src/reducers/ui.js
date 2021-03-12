let dispatch = undefined;

function selectStepById(steps, selectedStepId) {
  let selectedStepValid = steps.find(s => s.id === selectedStepId)
  if (!selectedStepValid && steps.length > 0) {
    selectedStepId = steps[0].id;
  }
  return selectedStepId;
}

export default (state = {}, action) => {
  switch (action.type) {
    case 'DISPATCH':
      dispatch = action.dispatch;
      return state;
    case 'SELECT_STEP':
      {
        let selectedStepId = action.id;
        return Object.assign({}, state, {
          selectedStepId
        })
      }
    case 'RECEIVE_STATUS':
      {
        let status = action.data;
        let selectedStepId = selectStepById(status.steps, state.selectedStepId);
        return Object.assign({}, state, {
          selectedStepId
        })
      }
    default:
      return state
  }
}
