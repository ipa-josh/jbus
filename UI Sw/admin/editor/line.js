handler_list.push( 
new function()
{
	this.name = "line";
	this.state = 0;
	this.id = 0;
	
	this.icon = "editor/line.png";
	this.descr= "Grundriss";
	this.type = "bg";
	
	this.click = function(x, y)
	{
		if(this.state==1) {
			this.state = 0;
			this.click(x,y);
			return;
		}
		this.state = 1;
		
		var svg = getRednerEngine().SVGEngine('svg');
		svg.line(x, y, x+3, y,
			{stroke: 'black', 'stroke-width': 3, id: 'obj'+obj_id});
		this.id = obj_id;
		obj_id +=1;
	}
	
	this.break = function(x, y)
	{
		var svg = getRednerEngine().SVGEngine('svg');
		$('#obj'+this.id, svg.root()).remove();
		this.state = 0;
	}
	
	this.move = function(x, y)
	{
		if(this.state!=1) return;
		var svg = getRednerEngine().SVGEngine('svg');
		$('#obj'+this.id, svg.root()).attr('x2',x).attr('y2',y);
	}
});