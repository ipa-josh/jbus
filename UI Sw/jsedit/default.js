gl_editor_temp['default'] = {
	init: function($this, set_config) {
		var config = {
			highlight: false,
			click: function() {},
			droppable: false,
			draggable: false,
		};
		$.extend(config, set_config);
		if(config.highlight) $this.mouseenter(function(){$(this).effect("highlight", {}, 3000);});
		$this.click(config.click);
		if(config.droppable) //if($this.data('subs'))
			$this.droppable({
				drop: function( event, ui ) {
				  //alert( (ui.position.left-$(this).position().left)+" "+(ui.position.top-$(this).position().top));
				  var vis = ui.draggable.data('vis');
				  vis.pos.x = ui.draggable.offset().left;
				  vis.pos.y = ui.draggable.offset().top;
				  ui.draggable.data('vis', vis);
				  ui.draggable.JHAVis('update');
				  return true;
				}
			});
			
		if(config.draggable)
			$this.draggable({ delay: 200, revert: false, opacity: 0.7 });
	},
	edit: function($this, vis) {
	},	
	buildSheet: function($this, set_hint) {
		var hint = {
			'vis.pos.x': 'int',
			'vis.pos.y': 'int',
			'vis.vis': 'const'
		};
		$.extend(hint, set_hint);
		
		propsheet.propSheet('clear');
		propsheet.propSheet('add','Object');
		propsheet.propSheet('add', 'vis', $this.data('vis'), hint);
	}
}