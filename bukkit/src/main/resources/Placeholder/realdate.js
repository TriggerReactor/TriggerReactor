function realdate(args){
  var date = java.util.Date;
  var simpleDateFormat = java.text.SimpleDateFormat;
  var df = new simpleDateFormat("[yyyy/MM/dd a hh:mm:ss] ");
  var dateobj = new date();
 
  return df.format(dateobj);
}