gl_vis_temp['light'] = {
	init: function($this) {
		$this.append('<img id="on" style="display:inline;" src="'+$this.data("vis").img.on+'" />')
		$this.append('<img id="off" style="display:inline;" src="'+$this.data("vis").img.off+'" />')
		
		$this.find('img#on').hide();
		$this.find('img#off').hide();
		
		$this.click(
				function() {
					var v=$this.JHA('getChild',{name:"status"}).data('data')
					if(v==false||v=="false")
						v=true;
					else
						v=false;
					$this.JHA('set', 'status/'+v)
				});
				
		if(history) history.add($this.data('path')+"/status");
	},
	vis: function($this, vis) {
		var v=$this.JHA('getChild',{name:"status"}).data('data')
		if(v==true||v=="true") {
			$this.find('img#on').show();
			$this.find('img#off').hide();
		}
		else {
			$this.find('img#on').hide();
			$this.find('img#off').show();
		}
	}
}