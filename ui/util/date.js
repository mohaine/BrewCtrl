import moment from 'moment'

export const formatDate = function(date) {
  if (!date) {
    return "";
  }
  return moment(date).format('YYYY/MM/DD h:mm:ss a')
};

export const formatDateBreak = (date) => {
  let dateStr = moment(date).format('YYYY/MM/DD h:mm:ss a')
  dateStr = dateStr.split(' ');
  dateStr = dateStr[0] + '<br/>' + dateStr[1] + ' ' + dateStr[2];
  return {
    __html: dateStr
  };
}

export const parseTime = function(time) {
  var seconds = 0;
  var split = time.split(":");
  split = split.reverse();
  for (var i = 0; i < split.length; i++) {
    var msLevel = parseInt(split[i], 10);
    msLevel *= Math.pow(60, i);
    seconds += msLevel;
  }
  return seconds;

}

export const formatTime = (time) => {
  if (time == 0) {
    return "\u221E";
  }

  let minutes = parseInt(time / 60);
  let seconds = parseInt(time - (minutes * 60));

  let formated = minutes + ":";

  if (seconds < 10) {
    formated = formated + '0';
  }
  return formated + seconds;
}


export const formatTimeMinutes = (time) => {
  if (time == 0) {
    return "\u221E";
  }

  let minutes = parseInt(time / 60);
  return minutes;
}
