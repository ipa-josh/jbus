gl_editor_temp['light'] = {
	init: function($this) {
		this.prototype = gl_editor_temp['default'];
		this.prototype.init($this, {draggable: true, click: this.buildSheet} );
		
	},
	edit: function($this, vis) {
	},
	
	buildSheet: function() {
		gl_editor_temp['light'].prototype.buildSheet($(this), {
		'vis.img.on': 'image',
		'vis.img.off': 'image',
		});
		propsheet.propSheet('finish');
		return false;
	}
}

editor_intf.addMenuEntry(1, '<div id="me_light"><img src="img/licht_on.jpg" height="24px" />Light</div>');

$("#me_light").droppableMenuEntry({type: 'light'});