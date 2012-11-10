
function svg_handler_any(name, src)
{
	this.name = name;
	this.src = src;
	this.state = 0;
	this.id = 0;
	
	this.icon = src;
	this.descr= name;
	this.type = "bg";
	
	this.create = function(x,y)
	{
		var svg = getRednerEngine().SVGEngine('svg');
		
		svg.load(this.src, {addTo: true, 
        changeSize: false, onLoad: function(svg)
		{
			//$("#info").text( svg.toSVG() );
			//alert( $('#new_obj', svg.root()).attr('id') );
			$('#new_obj', svg.root()).attr('id','obj'+obj_id);
		}}); 
			
		this.state = 1;
	}
	
	this.click = function(x, y)
	{
		if(this.state==0) {
			return;
		}
		
		this.state = 0;
		obj_id +=1;
	}
	
	this.break = function(x, y)
	{
		var svg = getRednerEngine().SVGEngine('svg');
		if(this.state!=0) $('#obj'+obj_id, svg.root()).remove();
		this.state=0;
	}
	
	this.move = function(x, y)
	{
		if(this.state==0) this.create(0,0);
		var svg = getRednerEngine().SVGEngine('svg');
		$('#obj'+obj_id, svg.root()).attr('transform',"translate("+x+","+y+")");
	}
}

add_handler( new svg_handler_any("Sofa","editor/sofa.svg") );
add_handler( new svg_handler_any("TV","editor/tv.svg") );
add_handler( new svg_handler_any("WC","editor/wc.svg") );
add_handler( new svg_handler_any("Tuer","editor/door.svg") );
add_handler( new svg_handler_any("Aquarium","editor/aquarium.svg") );