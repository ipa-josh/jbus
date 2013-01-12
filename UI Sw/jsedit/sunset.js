gl_editor_temp['sunset'] = {
	init: function($this) {
		this.prototype = gl_editor_temp['default'];
		this.prototype.init($this, {draggable: true} );
		
	},
	edit: function($this, vis) {
	}
}

editor_intf.addMenuEntry(1, '<div id="me_light"><img src="img/sunset.png" height="24px" />Sunset</div>');

$("#me_sunset").droppableMenuEntry({type: 'sunset'});