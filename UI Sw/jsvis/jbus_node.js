gl_vis_temp['jbus_node'] = {
	init: function($this) {
		$this.append('<img style="display:inline;" src="img/jbus_node.png" width="50%"/>');
		
		var num = $this.parent().data('node_num')+1;
		var vis = $this.data('vis');
		vis.pos.x = ( (num-1)%2)*128;
		vis.pos.y = Math.floor(num/2)*32+64;
		$this.data('vis', vis);
		$this.parent().data('node_num', num);
		
		$this.click(
				function() {
				});
	},
	vis: function($this, vis) {
	},
	edit: function($this, vis) {
	}
}