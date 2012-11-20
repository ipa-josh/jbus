if(!on_new_element) var on_new_element = function() {};

(function( $ ) {
	var id=0;
	var map={};
	var ts=0;
	
	function jha_update() {
		$.ajax({
			url: 'visupdate'+ts,
			dataType: "json",
			error: function(a,b,c) {
				error_handler.error("auto-update","error while updating");
				
				setTimeout(jha_update, 1000);
			},
			success: function(data) {
				error_handler.clear("auto-update");
				ts = data.ts;
				
				for(d in data.c) {
					if(d=="") {
						$('#content').JHA({path:""});
						jha_parse($('#content'), data.c[d]);
					}
					else if(d in map) {
						jha_parse($("#"+map[d]), data.c[d]);
						$("#"+map[d]).parent().JHAVis('update');
					}
					else alert("error "+d);
				}
				
				jha_update();
			}
		});
	}
	
	function jha_parse($this, data) {
		if(data.base=="HAObject"||data.base=="HARoot") { //container
			$this.data("subs",data.subs)
			for(s in data.subs) {
				$this.JHA('add',{path:$this.data("path")+"/"+data.subs[s]});
			}
		}
		else if(data.base=="data") {
			$this.data("data",data.data)
			return
		}

		//alert(data.vis);
		if(!$this.data('vis')) {
			$this.JHA('visualize',{vis:data.vis});
			on_new_element($this);
		}
	}

	var methods = {
			init : function(data) {
				return this.each(function(){
					var $this = $(this)
					$this.data('path', data.path)
					/*var async=true;
					if("sync" in data)
						async = !data.sync

						$.ajax({
							url: 'get'+data.path,
							dataType: "json",
							async: async,
							error: function(a,b,c) {
								error_handler.error("communication","could not get data");
							},
							success: function(data) {
								error_handler.clear("communication");
								jha_parse($this, data);
							}
						});*/
				});
			},
			destroy : function( ) {

				return this.each(function(){
					var $this = $(this)
				});
			},
			add : function(data) {
				return this.each(function(){
					id=id+1;
					$(this).append("<div id='"+id+"' style='position:absolute'/>");
					map[data.path] = id;
					//alert(data.path);
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

			getAllChildren:  function() {
				var r=[];
				var subs = $(this).data('subs');
				for(s in subs) {
					r[r.length] =  $("#"+map[$(this).data('path')+"/"+subs[s]]);
				}
				return r;
			},

			set:  function(data) {
				return this.each(function(){
					var $this = $(this)
					$.ajax({
						url: 'set'+data.path,
						dataType: "json",
						error: function(a,b,c) {
							error_handler.error("communication","could not set data");
						},
						success: function(res) {
							error_handler.clear("communication");

							if(res) {
								/*alert(data.path);
								for(v in data.reload)
									alert(data.reload[v]);*/
								/*for(v in data.reload) {
									alert(data.reload[v]);
									$("#"+map[data.reload[v]]).JHA({
										path: $("#"+map[data.reload[v]]).data("path"),
										sync: true
									});
								}
								$this.JHAVis('update')*/
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
	
	jha_update();

})( jQuery );
