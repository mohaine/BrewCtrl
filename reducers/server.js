import {findControlPoint} from '../util/step'

function findSensor(status, address){
  if(status && status.sensors){
      return status.sensors.find(s=>s.address === address)
  }
}

function buildBreweryView(status,configuration){
  if(status && status.steps && configuration){

      let steps = status.steps.map(step=>{
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
            let id =  step.id + "h" + heater.io;
            heater = findControlPoint(step, heater.io);
          }

          return {id,name, heater, sensor};
        });
        let pumps = configuration.brewLayout.pumps.map(p=>{
          let pump = findControlPoint(step, p.io);

          // This shouldn't happen
          if(!pump){
            pump = {};
          }
          let name = p.name;
          let id = "pump" +  p.io;

          return  Object.assign({}, pump, {
            id, name
          });
        });

        let name = step.name;
        let id = step.id;
        let controlPoints = step.controlPoints;
        return {id,name, pumps, tanks,controlPoints, rawStep: step};
      });

      let mode = status.mode;

      let sensors = status.sensors.map(s=>{

        let cfgSesnsor = configuration.sensors.find(cs=>s.address === cs.address);
        let name = cfgSesnsor ? cfgSesnsor.name : "";
        let location = cfgSesnsor ? cfgSesnsor.location : "";

        return Object.assign({}, s, {
          location, name
        });
      });


      let brewery = {mode, steps, sensors};
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
