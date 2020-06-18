

import {generateAlpahId} from './id'


export const overlayControlPoint = function (step, cpNew){
  let controlPoints = step.controlPoints;
  if(controlPoints){
      controlPoints = controlPoints.map(cp=>cp.controlIo === cpNew.controlIo ? cpNew : cp)
  }
  return Object.assign({}, step, { controlPoints: controlPoints })
}

export const findControlPoint = function (step, io){
  if(step && step.controlPoints){
      return step.controlPoints.find(cp=>cp.controlIo === io)
  }
}

export const findTempSensorByLocationName = function(configuration,status,name){
  if(name){
    let sensors = configuration.sensors.filter(s=>s.location === name);
    let activeSensors = sensors.filter(s=> status.sensors.find(rs => rs.address === s.address));
    return activeSensors.length>0 ? activeSensors[0] : undefined;
  }
}

export const findControlPointByName = function(configuration,name){
  let brewLayout = configuration.brewLayout;
  let controlPoint = undefined;
  brewLayout.tanks.forEach(t=>{
      let heater = t.heater;
      if(heater){
        if(t.name === name || heater.name === name){
          controlPoint = heater;
        }
      }
  });
  brewLayout.pumps.forEach(p=>{
    if(p.name === name){
      controlPoint = p;
    }
  });
  return controlPoint;
}

export const findTargetByAddress = function(configuration,address){
  let brewLayout = configuration.brewLayout;
  let target = undefined;
  brewLayout.tanks.forEach(t=>{
      let sensor = t.sensor;
      if(sensor){
        if(sensor.address == address){
          target = t
        }
      }
  });

  return target
}

export const findControlByIo = function(configuration,io){
  let brewLayout = configuration.brewLayout;
  let target = undefined;
  brewLayout.tanks.forEach(t=>{
      let heater = t.heater;
      if(heater){
        if(heater.io == io){
          target = heater
        }
      }
  });
  brewLayout.pumps.forEach(p=>{
    if(p.io == io){
      target =  p
    }
  });
  return target
}


export const createManualStep = function(configuration){
  function initCp(cp){
    cp.duty = 0;
    cp.tempSensorAddress = "";
    cp.targetTemp = 0;
    cp.automaticControl = false;
    if(cp.fullOnAmps === undefined) cp.fullOnAmps = 0;
    if(cp.maxDuty === undefined) cp.maxDuty = 100;
    return cp;
  }

  let controlPoints = [];
  let brewLayout = configuration.brewLayout;
  brewLayout.tanks.forEach(t=>{
      let heater = t.heater;
      if(heater){
        let controlPoint = initCp({controlIo: heater.io, hasDuty: heater.hasDuty,fullOnAmps: heater.fullOnAmps, maxDuty: heater.maxDuty});
        controlPoints.push(controlPoint)
      }
  });
  brewLayout.pumps.forEach(p=>{
    let controlPoint = initCp({controlIo: p.io, hasDuty: p.hasDuty,fullOnAmps: p.fullOnAmps});
    controlPoints.push(controlPoint)
  });

  let step = {stepTime: 0, name: "New Step", id: generateAlpahId(8),controlPoints}
  return step;

}
