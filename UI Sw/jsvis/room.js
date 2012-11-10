gl_vis_temp['room'] = {
	init: function($this) {
		$this.prepend('<img style="display:inline;position:absolute" />')
	},
	vis: function($this, vis) {
		$this.find('img').attr("src",vis.img)
	}
}