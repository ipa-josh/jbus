//load style.css
if (document.createStyleSheet){
    document.createStyleSheet('redner/dialog.css');
}
else {
    $("head").append($("<link rel='stylesheet' href='redner/dialog.css' type='text/css' media='screen' />"));
}

(function( $ ){

  var methods = {
     init : function( options ) {

       return this.each(function(){
         var $this = $(this);
		 
		 var settings = $.extend( {
		  'width'  : '400px',
		  'height' : '300px',
		  'title'  : ''
		 }, options);
		 
		 $("body").append('<div id="dialog" title="'+settings['title']+'"><div id="dialog-tabs"><ul></ul></div></div>');
		 
		 $("#dialog").dialog({
			bgiframe: true,
			autoOpen: false,
			height: settings['height'],
			width : settings['width'],
			modal: true,
			buttons: {
				OK: function() {
						  $("#dialog > form").submit();
				  $(this).dialog('close');
				},
				Abbrechen: function() {
					$(this).dialog('close');
				}
			}
		});

       });
     },
     destroy : function( ) {

       return this.each(function(){
		$("#dialog").remove();
       })

     },
     addTab : function(options) {
		 var settings = $.extend( {
		  'title'  : ''
		 }, options);
		 
		var pos = $("#dialog #dialog-tabs ul li").length+1;
		$("#dialog #dialog-tabs ul").append('<li><a href="#tabs-'+pos+'">'+settings['title']+'</a></li>');
		$("#dialog #dialog-tabs").append('<div id="tabs-'+pos+'"><p>abc</p></div>');
	 },
     open : function( ) {
		$("#dialog #dialog-tabs").tabs();
		$("#dialog").dialog('open');
	 }
  };

  $.fn.Dialog = function( method ) {
    
    if ( methods[method] ) {
      return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.tooltip' );
    }    
  
  };

})( jQuery );