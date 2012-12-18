gl_vis_temp['sunset'] = {
	init: function($this) {
	},
	vis: function($this, vis) {
		var v=$this.JHA('getChild',{name:"status"}).data('data')
		
		var col;
		var n = Math.round(Math.abs(v)*255);
		if(v>=0)
			col="FF"+n.toString(16)+"00";
		else
			col=(255-n).toString(16)+"0000";
		alert(col);
		$("#body").css('background-color','#'+col);
	}
}