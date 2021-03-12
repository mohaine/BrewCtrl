

let convertC2F = function(tempC) {
  return (9.0 / 5.0) * tempC + 32;
}


export const convertF2C = function(tempF) {
  return (5.0 / 9.0) * (tempF - 32);
}


export const scaleC2F = function(tempC) {
  return (9.0 / 5.0) * tempC;
}


export const scaleF2C = function(tempF) {
  return (5.0 / 9.0) * (tempF);
}

export const round =  function(value, places) {
  var factor = Math.pow(10, places);
  value = Math.round(value * factor);
  return value / factor;
}


export const formatTemp =  function(tempC){
  return round(convertC2F(tempC),1).toFixed(1)  + "°"
}

export const formatTempWhole =  function(tempC){
  return round(convertC2F(tempC),0)  + "°"
}
