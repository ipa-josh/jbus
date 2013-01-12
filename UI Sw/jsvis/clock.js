gl_vis_temp['clock'] = {
	init: function($this) {
		if (document.createStyleSheet){
                document.createStyleSheet('css/clock.css');
            }
            else {
                $("head").append($("<link rel='stylesheet' href='css/clock.css' type='text/css' media='screen' />"));
            }

		$this.append('<div class="clock"><div id="Date"></div><ul><li id="hours"></li><li id="point">:</li><li id="min"></li><li id="point">:</li><li id="sec"></li></ul></div>');

// Create two variable with the names of the months and days in an array
//var monthNames = [ "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" ]; 
//var dayNames= ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"]
var monthNames = [ "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" ]; 
var dayNames= ["Sonntag","Montag","Dienstag","Mittwoch","Donnerstag","Freitag","Samstag"]

// Create a newDate() object
var newDate = new Date();
// Extract the current date from Date object
newDate.setDate(newDate.getDate());
// Output the day, date, month and year   
$('#Date').html(dayNames[newDate.getDay()] + " " + newDate.getDate() + ' ' + monthNames[newDate.getMonth()] + ' ' + newDate.getFullYear());

setInterval( function() {
	// Create a newDate() object and extract the seconds of the current time on the visitor's
	var seconds = new Date().getSeconds();
	// Add a leading zero to seconds value
	$("#sec").html(( seconds < 10 ? "0" : "" ) + seconds);
	},1000);
	
setInterval( function() {
	// Create a newDate() object and extract the minutes of the current time on the visitor's
	var minutes = new Date().getMinutes();
	// Add a leading zero to the minutes value
	$("#min").html(( minutes < 10 ? "0" : "" ) + minutes);
    },1000);
	
setInterval( function() {
	// Create a newDate() object and extract the hours of the current time on the visitor's
	var hours = new Date().getHours();
	// Add a leading zero to the hours value
	$("#hours").html(( hours < 10 ? "0" : "" ) + hours);
    }, 1000);	

	},
	vis: function($this, vis) {
	},
	edit: function($this, vis) {
	}
}