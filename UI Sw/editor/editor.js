
var editor_intf = {

	add: function(path, xml) {
	},

	remove: function(path) {
		//TODO:
		$("#content").JHA('getChild',{name: path}).remove();
	},
			
	addMenuEntry: function(cat, entry) {
		var list=["background-elements","user-interfaces","hardware"];
		
		$("#"+list[cat]).append('<li>'+entry+'</li>').listview('refresh');
	}
	
};
var gl_editor_temp={};

(function( $ ) {
	function loadEditorData(name) {
		if(name!="default") loadEditorData("default");
		
		if(name in gl_editor_temp)
			return gl_editor_temp[name]

		$.ajax({
			url: "jsedit/"+name+".js",
			dataType: "script",
			cache: false,
			async: false,
			success: function(data) {
			},
			error: function(a,b,c) {
				alert("e1: "+a+b+c);
			}
		});

		if(name in gl_editor_temp)
			return gl_editor_temp[name]

		alert("editor "+name+" not available")
			return {}
	}
	
	on_new_element = function($this, vis) {
		try {
			loadEditorData(vis).init($this);
		} catch(e) {
			alert(e);
		}
	};

	var methods = {
			init : function(data) {
				return this.each(function(){
					$(this).css("z-index", 100);
					$(this).draggable({ helper: "clone", opacity: 0.9
					});
				});
			},
			destroy : function( ) {
				return this.each(function(){
					var $this = $(this)
				});
			}
	};

	$.fn.droppableMenuEntry = function( method ) {

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
