gl_editor_temp['none'] = {
	init: function($this) {
		this.prototype = gl_editor_temp['default'];
		this.prototype.init($this, {droppable: true} );
	},
	vis: function() {	
	},
	edit: function($this, vis) {
	}
}