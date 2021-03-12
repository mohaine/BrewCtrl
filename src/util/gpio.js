

export const emptyGpios =  function(configuration){
  let gpios = [2, 3, 4, 14, 15, 17, 18, 27, 22, 23, 24, 10, 9, 25, 11, 8, 7].filter(io=>{
    let brewLayout = configuration.brewLayout
    if(brewLayout){
      let pumps = brewLayout.pumps;
      if(pumps.find(p=>p.io === io)){
        return false;
      }
      let tanks = brewLayout.tanks;
      if(tanks.find(t=>t.heater && t.heater.io === io)){
        return false;
      }
    }
    return true;
  }).sort((a,b)=> a-b)
  return gpios;
}
