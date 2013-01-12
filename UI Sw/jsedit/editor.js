gl_editor_temp['editor'] = {
	init: function($this) {
		if(editor_intf) {
			editor_intf.obj = $this;
			editor_intf.active();
		}
	},
	vis: function() {	
	},
	edit: function($this, vis) {
	}
}