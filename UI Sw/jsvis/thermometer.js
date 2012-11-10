gl_vis_temp['thermometer'] = {
	init: function() {
		$this.prepend('<div class="progressbar"><span class="progressbar-value"><em class="progressbar-cover"></em></span></div>')
	},
	vis: function($this, vis) {
		var v=parseFloat($this.JHA('getChild',{name:"status"}).data('data'))
		bkcolor = (v < 25) ? '#0f0' :'#ff0';
		if (v > 75) { bkcolor = '#f00'; }
		$this.find('.progressbar-cover').css('bottom' , v + '%');  // the cover controls the bar height
		$this.find('.progressbar-value').css('backgroundColor' , bkcolor ); // value contains the bar color
	}
}