
var editor_intf = {

	add: function(path, xml) {
	},

	remove: function(path) {
		//TODO:
		$("#content").JHA('getChild',{name: path}).remove();
	}
	
};

(function( $ ) {
	
	on_new_element = function($this) {
		$this.mouseenter(function(){$(this).effect("highlight", {}, 3000);});
		$this.click(function() {alert(1);return false;});
		if($this.data('subs'))
			$this.droppable({
				drop: function( event, ui ) {
				  alert(2);
				}
			});
			
		try {
			$this.draggable({ delay: 200 });
			$this.draggable( "option", "revert", true );
		}
		catch(e) {
		}
	};
	
          /*$( "#droppable" ).droppable({
            drop: function( event, ui ) {
              $( this )
                .addClass( "ui-state-highlight" )
                .find( "p" )
                  .html( "Dropped!" );
            }*/

	/*var methods = {
			init : function(data) {
				return this.each(function(){
				});
			},
			destroy : function( ) {
				return this.each(function(){
					var $this = $(this)
				});
			}
	};

	$.fn.Editor = function( method ) {

		// Method calling logic
		if ( methods[method] ) {
			return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
		} else if ( typeof method === 'object' || ! method ) {
			return methods.init.apply( this, arguments );
		} else {
			$.error( 'Method ' +  method + ' does not exist on jQuery.tooltip' );
		}    

	};*/

})( jQuery );
