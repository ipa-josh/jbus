handler_list.push( 
new function()
{
	this.name = "lamp";
	this.state = 0;
	this.id = 0;
	
	this.icon = "editor/lamp.png";
	this.descr= "Licht";
	this.type = "ia";
	
	this.create = function(x,y)
	{
		var svg = getRednerEngine().SVGEngine('svg');
		
		var grp=svg.group('obj'+obj_id);
		
		svg.circle(grp, x,y, 10,
			{fill: 'none', stroke: 'yellow',
			'stroke-width': 2});
		svg.line(grp, x-10/Math.sqrt(2), y-10/Math.sqrt(2), x+10/Math.sqrt(2), y+10/Math.sqrt(2),
			{stroke: 'yellow', 'stroke-width': 2});
		svg.line(grp, x-10/Math.sqrt(2), y+10/Math.sqrt(2), x+10/Math.sqrt(2), y-10/Math.sqrt(2),
			{stroke: 'yellow', 'stroke-width': 2});
			
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
});