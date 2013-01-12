gl_editor_temp['jbus_interface'] = {
	init: function($this) {
		this.prototype = gl_editor_temp['default'];
		this.prototype.init($this, {draggable: true} );
		
	},
	edit: function($this, vis) {
	}
}

editor_intf.addMenuEntry(2, '<div id="me_light"><img src="img/jbus_intf.png" height="24px" />JBus Interface</div>');

$("#me_jbus_interface").droppableMenuEntry({type: 'jbus_interface'});