

import moment from 'moment'

export const formatDate =  function(date){
  if(!date){
      return "";
  }
  return moment(date).format('YYYY/MM/DD h:mm:ss a')
};

export const formatDateBreak = (date) => {
  let dateStr = moment(date).format('YYYY/MM/DD h:mm:ss a')
  dateStr = dateStr.split(' ');
  dateStr = dateStr[0] + '<br/>' + dateStr[1] + ' ' + dateStr[2];
  return {__html: dateStr};
}
