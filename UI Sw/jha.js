(function( $ ) {
	var id=0;
	var map={};

	var methods = {
			init : function(data) {
				return this.each(function(){
					var $this = $(this)
					$this.data('path', data.path)
					var async=true;
					if("sync" in data)
						async = !data.sync

						$.ajax({
							url: 'get'+data.path,
							dataType: "json",
							async: async,
							error: function(a,b,c) {
								alert(a+b+c);
							},
							success: function(data) {

								if(data.base=="HAObject"||data.base=="HARoot") { //container
									for(s in data.subs) {
										$this.JHA('add',{path:$this.data("path")+"/"+data.subs[s]});
									}
								}
								else if(data.base=="data") {
									$this.data("data",data.data)
									return
								}

								$this.JHA('visualize',{vis:data.vis});

							}
						});
				});
			},
			destroy : function( ) {

				return this.each(function(){
					var $this = $(this)
				});
			},
			add : function(data) {
				return this.each(function(){
					id=id+1
					$(this).append("<div id='"+id+"' style='position:absolute'/>")
					map[data.path] = id
					$("#"+id).JHA(data)
				});
			},

			visualize:  function(data) {
				return this.each(function(){
					$(this).JHAVis(data)
				});
			},

			getChild:  function(data) {
				return $("#"+map[$(this).data('path')+"/"+data.name]);
			},

			set:  function(data) {
				return this.each(function(){
					var $this = $(this)
					$.ajax({
						url: 'set'+data.path,
						dataType: "json",
						error: function(a,b,c) {
							alert(a+b+c);
						},
						success: function(res) {

							if(res) {
								for(v in data.reload)
									$("#"+map[data.reload[v]]).JHA({
										path: $("#"+map[data.reload[v]]).data("path"),
										sync: true
									})
									$this.JHAVis('update')
							}

						}
					});
				});

			}
	};

	$.fn.JHA = function( method ) {

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
