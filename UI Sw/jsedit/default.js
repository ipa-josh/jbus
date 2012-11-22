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
				  alert( (ui.position.left-$(this).position().left)+" "+(ui.position.top-$(this).position().top));
				  return true;
				}
			});
			
		if(config.draggable)
			$this.draggable({ delay: 200, revert: true, opacity: 0.7 });
	},
	edit: function($this, vis) {
	}
}