
var obj_id=0;
var mouse_handler = 0;


(function( $ ){

  var methods = {
     init : function( options ) {

       return this.each(function(){
         var $this = $(this);
		 
		$this.mousemove(methods['editor_mousemove']);
		$this.click(methods['editor_mouseclick']);
		$this.bind("contextmenu",function(e){
			e.which=2;
			methods['editor_mouseclick'](e);
			return false;
		}); 

       });
     },
     destroy : function( ) {

       return this.each(function(){

         var $this = $(this);

         // Namespacing FTW
         $(window).unbind('contextmenu');

       })

     },
     menu : function(menu_el) {

       return this.each(function(){

		for(var i=0; i<handler_list.length; i++) {
			var h = handler_list[i];
			
		$("#applications").append('<li data-id="tools_'+h.name+'" id="tools_'+h.name+'" data-type="'+h.type+'">'+
		'<img src="'+h.icon+'" height="64" alt="" />'+
		'<strong>'+h.descr+'</strong>  </li>');
		
		  $("#tools_"+h.name).data('handler',h).click( function() {
			if(mouse_handler) mouse_handler.break();
			mouse_handler = $(this).data('handler');
		  });
		 }
		 
		 
		  // bind radiobuttons in the form
		  var $filterType = $('#filter input[name="type"]');
		  var $filterSort = $('#filter input[name="sort"]');

		  // get the first collection
		  var $applications = menu_el;

		  // clone applications to get a second collection
		  var $data = $applications.clone();

		  // attempt to call Quicksand on every form change
		  $filterType.add($filterSort).change(function(e) {
			if ($($filterType+':checked').val() == 'all') {
			  var $filteredData = $data.find('li');
			} else {
			  var $filteredData = $data.find('li[data-type=' + $($filterType+":checked").val() + ']');
			}

			  // if sorted by name
			  var $sortedData = $filteredData.sorted({
				by: function(v) {
				  return $(v).find('strong').text().toLowerCase();
				}
			  });

			// finally, call quicksand
			$applications.quicksand($sortedData, {
			  duration: 800,
			  easing: 'easeInOutQuad'
			});

		  });

       })
     },
	toGrid: function(x)
	{
		return Math.round(x/20.)*20;
	},
	editor_mousemove: function(e)
	{
		var x = e.pageX - this.offsetLeft;
		var y = e.pageY - this.offsetTop;
		x = methods['toGrid'](x);
		y = methods['toGrid'](y);
		if(mouse_handler)
			mouse_handler.move(x,y);
	},
	editor_mouseclick: function(e)
	{
		var x = e.pageX - this.offsetLeft;
		var y = e.pageY - this.offsetTop;
		x = methods['toGrid'](x);
		y = methods['toGrid'](y);
		if(mouse_handler)
		{
			if(e.which==1)
				mouse_handler.click(x,y);
			else if(e.which==2)
			{
				mouse_handler.break(x,y);
				mouse_handler.move(x,y);
			}
		}
		return false;
	}
  };

  $.fn.Editor = function( method ) {
    
    if ( methods[method] ) {
      return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.tooltip' );
    }    
  
  };

})( jQuery );


// Custom sorting plugin
(function($) {
  $.fn.sorted = function(customOptions) {
    var options = {
      reversed: false,
      by: function(a) { return a.text(); }
    };
    $.extend(options, customOptions);
    $data = $(this);
    arr = $data.get();
    arr.sort(function(a, b) {
      var valA = options.by($(a));
      var valB = options.by($(b));
      if (options.reversed) {
        return (valA < valB) ? 1 : (valA > valB) ? -1 : 0;				
      } else {		
        return (valA < valB) ? -1 : (valA > valB) ? 1 : 0;	
      }
    });
    return $(arr);
  };
})(jQuery);
