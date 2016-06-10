



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
