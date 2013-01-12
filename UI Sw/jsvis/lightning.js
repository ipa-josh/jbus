gl_vis_temp['lightning'] = {
	init: function($this) {
		$this.append('<div class="dark-light" style="border-radius: 50%;width: 50px;height: 50px;background-color:black;" />');
	},
	vis: function($this, vis) {
		var v=$this.JHA('getChild',{name:"status"}).data('data')
		
		var n = Math.round(Math.abs(v)*255);
		var col=n.toString(16)+n.toString(16)+"00";
		$this.find(".dark-light").style('background-color',"#"+col);
	},
	edit: function($this, vis) {
	}
}