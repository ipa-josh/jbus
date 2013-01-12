var propsheet;

(function( $ ) {
	function generateImgList(def) {
		var r='<select>';
		
		var imgs = editor_intf.listImages();
		for(var i=0; i<imgs.length; i++)
			r += '<option value="'+imgs[i]+'">'+imgs[i]+'</option>';
		return r+'</select>';
	}
	var methods = {
			init : function(data) {
				return this.each(function(){
					propsheet = $(this);
				});
			},
			destroy : function( ) {
				return this.each(function(){
					var $this = $(this)
				});
			},
			clear: function() {
				return this.each(function(){
					$(this).html("");
				});
			},
			finish: function() {
				return this.each(function(){
					$(this).append('<tr><td></td><td>Save</td></tr>');
					$(this).trigger("create");
				});
			},
			add: function(name, data, hint) {
				return this.each(function(){
					if(!data)
						$(this).append('<tr><td colspan="2"><b>'+name+'</b></td></tr>');
					else if(typeof data=='object')
						for(var o in data)
								$(this).propSheet('add',name+"."+o, data[o], hint);
					else if(hint!=undefined && name in hint) {
						if(hint[name]=='const')
							$(this).append('<tr><td>'+name+'</td><td>'+data+'</td></tr>');
						else if(hint[name]=='int')
							$(this).append('<tr><td>'+name+'</td><td><input type="number" value="'+data+'" data-mini="true" /></td></tr>');
						else if(hint[name]=='text')
							$(this).append('<tr><td>'+name+'</td><td><input type="text" value="'+data+'" data-mini="true" /></td></tr>');
						else if(hint[name]=='image')
							$(this).append('<tr><td>'+name+'</td><td>'+generateImgList(data)+'</td></tr>');
					}
					else
						$(this).append('<tr><td>'+name+'</td><td>'+data+'</td></tr>');
				});
			}
	};

	$.fn.propSheet = function( method ) {

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
