gl_vis_temp['error'] = {
	init: function() {
		
	},
	vis: function($this, vis) {
		var ch=$this.JHA('getAllChildren');
		for(c in ch) {
			error_handler.handle(ch[c].data('path'), ch[c].data('data'));
		}
	}
}