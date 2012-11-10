//load style.css
if (document.createStyleSheet){
    document.createStyleSheet('redner/style.css');
}
else {
    $("head").append($("<link rel='stylesheet' href='redner/style.css' type='text/css' media='screen' />"));
}

var handler_list = new Array();

function add_handler(h) {
	handler_list.push(h);
}

var active_redner_engine;
function getRednerEngine() {
	return active_redner_engine;
}
function setRednerEngine(eng) {
	active_redner_engine = eng;
}



(function( $ ){

  var methods = {
     init : function( options ) {

       return this.each(function(){
         var $this = $(this);
		 
		 var settings = $.extend( {
		  'width'  : '400px',
		  'height' : '300px'
		 }, options);
		 
		 $this.css('width', settings['width']);
		 $this.css('height',settings['height']);

		 $this.svg({onLoad: methods["drawInitial"]});

       });
     },
     destroy : function( ) {

       return this.each(function(){
       })

     },
     exportSVG : function( ) {
		return $(this).svg('get').toSVG();
	 },
     svg : function( ) {
		return $(this).svg('get');
	 },
     clear : function( ) {
		$(this).svg('get').clear();
		methods["drawInitial"].drawInitial($(this).svg('get'));
	 },
     drawInitial : function( svg ) {
		svg.load('<svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%"> <defs> <pattern id="gridPattern" width="20" height="20" patternUnits="userSpaceOnUse"> <rect x="0" y="0" width="20" height="20" fill="#666666" style="stroke:#999999;stroke-width:2;"/> </pattern> </defs> <g transform="translate(0,0)"> <rect fill="url(#gridPattern)" x="0" y="0" width="100%" height="100%"/> </g> </svg> ',
			{changeSize: false});
	 }
  };

  $.fn.SVGEngine = function( method ) {
    
    if ( methods[method] ) {
      return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.tooltip' );
    }    
  
  };

})( jQuery );