gl_vis_temp['thermometer'] = {
	init: function($this) {
		$this.append('<div class="progressbar"><span class="progressbar-value"><em class="progressbar-cover"></em></span></div><div class="text"></div>');
		if(history) history.add($this.data('path')+"/out","avg");
	},
	vis: function($this, vis) {
		var v=parseFloat($this.JHA('getChild',{name:"out"}).data('data')); //v is degree
		$this.find(".text").html(v.toFixed(2)+"&deg;");
		bkcolor = (v < 10) ? '#00f' :'#ff0';
		if (v > 15 && v<25) { bkcolor = '#0f0'; }
		if (v > 30) { bkcolor = '#f00'; }
		v = (v+20)/0.7; //-20 to 50 degree
		$this.find('.progressbar-cover').css('bottom' , v + '%');  // the cover controls the bar height
		$this.find('.progressbar-value').css('backgroundColor' , bkcolor ); // value contains the bar color
	}
}