

function findSensor(status, address){
  if(status && status.sensors){
      return status.sensors.find(s=>s.address === address)
  }
}

function findControlPoint(step, io){
  if(step && step.controlPoints){
      return step.controlPoints.find(cp=>cp.controlIo === io)
  }
}

function buildBreweryView(status,configuration){
  if(status && status.steps && configuration){

      let step = status.steps[0];

      let tanks = configuration.brewLayout.tanks.map(t=>{
        let name = t.name;
        let id = "tank" + name;
        let heater = t.heater;
        let sensor = t.sensor;

        if(sensor && sensor.address){
          sensor = findSensor(status,sensor.address);
        } else {
          sensor = undefined;
        }


        if(heater){
          let id = "heater" + heater.io;
          let controlPoint = findControlPoint(step, heater.io);
          let duty = controlPoint ? controlPoint.duty : 0;
          let on = controlPoint ? controlPoint.on : false;
          let automaticControl = controlPoint? controlPoint.automaticControl : 0;
          let targetTemperatureC = controlPoint ? controlPoint.targetTemp : 0;
          heater = {id, duty, on, automaticControl, targetTemperatureC}
        }

        return {id,name, heater, sensor};
      });
      let pumps = configuration.brewLayout.pumps.map(p=>{
        let name = p.name;
        let id = "pump" + p.io;

        let controlPoint = findControlPoint(step, p.io);
        let duty = controlPoint ? controlPoint.duty : 0;
        let on = controlPoint ? controlPoint.on : false;
        let automaticControl = controlPoint? controlPoint.automaticControl : 0;


        return {id,name, duty, on, automaticControl};
      });


      let brewery = {tanks, pumps};
      return brewery;
  }
}


export default (state = {}, action) => {
  switch (action.type) {

    case 'ERROR_CONFIG':
    case 'REQUEST_CONFIG':
      return Object.assign({}, state, {
        requestConfigurationStatus: action.status
      })
    case 'RECEIVE_CONFIG':
      return Object.assign({}, state, {
        configuration: action.configuration,
        brewery: buildBreweryView(state.status,action.configuration),
        requestConfigurationStatus: null,
      })

    case 'ERROR_STATUS':
    case 'REQUEST_STATUS':
      return Object.assign({}, state, {
        requestStatusStatus: action.status
      })

    case 'RECEIVE_STATUS':
      return Object.assign({}, state, {
        status: action.data,
        brewery: buildBreweryView(action.data,state.configuration),
        requestStatusStatus: null,
      })


    default:
      return state
  }
}
