handler_list.push( 
new function()
{
	this.name = "switch";
	this.state = 0;
	this.id = 0;
	
	this.icon = "editor/switch.png";
	this.descr= "Taster";
	this.type = "ia";
	
	this.create = function(x,y)
	{
		var svg = getRednerEngine().SVGEngine('svg');
		
		var grp=svg.group('obj'+obj_id);
		
		svg.line(grp, x, y-8, x, y+5,
			{stroke: 'red', 'stroke-width': 2});
		svg.line(grp, x-7, y-8, x+7, y-8,
			{stroke: 'red', 'stroke-width': 2});
		svg.line(grp, x-7, y-8, x-7, y-5,
			{stroke: 'red', 'stroke-width': 2});
		svg.line(grp, x+7, y-8, x+7, y-5,
			{stroke: 'red', 'stroke-width': 2});
			
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