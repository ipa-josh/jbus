var gl_vis_temp={};


(function( $ ) {
	var debug=true;

	function loadVisData(name) {
		if(name in gl_vis_temp)
			return gl_vis_temp[name]

		$.ajax({
			url: "jsvis/"+name+".js",
			dataType: "script",
			cache: false,
			async: false,
			success: function(data) {
			},
			error: function(a,b,c) {
				alert("e1: "+a+b+c);
			}
		});

		if(name in gl_vis_temp)
			return gl_vis_temp[name]

		alert("visualization "+name+" not available")
			return {}
	}

	var methods = {
			init : function(data) {
				return this.each(function(){
					var $this = $(this)

					var options = {
						'pos': {'x':0,'y':0},
						'vis': data.vis
					}

					$.ajax({
						url: 'vis/'+data.vis,
						dataType: "json",
						error: function(a,b,c) {
							alert(a+b+c);
						},
						success: function(data) {
							options = $.extend(options, data);

							$.ajax({
								url: 'vis/'+$this.data("path"),
								dataType: "json",
								error: function(a,b,c) {
									alert(a+b+c);
								},
								success: function(data) {
									options = $.extend(options, data);

									$this.data("vis",options)
									loadVisData(options['vis']).init($this)
									$this.JHAVis('update')
								}
							});

						}
					});
				});
			},
			destroy : function( ) {

				return this.each(function(){
					var $this = $(this)
				});
			},

			update:  function(data) {
				return this.each(function(){
					var vis = $(this).data('vis')
					$(this).offset({ top: vis.pos.y, left: vis.pos.x})
					var $this = $(this)

					loadVisData(vis.vis).vis($this,vis)

				});
			}
	};

	$.fn.JHAVis = function( method ) {

		// Method calling logic
		if ( methods[method] ) {
			return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
		} else if ( typeof method === 'object' || ! method ) {
			return methods.init.apply( this, arguments );
		} else {
			$.error( 'Method ' +  method + ' does not exist on jQuery.tooltip' );
		}    

	};

})( jQuery );