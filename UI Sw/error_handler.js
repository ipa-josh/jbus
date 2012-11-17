
error_handler = {
	content: {},
	nums: [0,0,0],

	ERROR: 0,
	WARNING: 1,
	INFO: 2,

	divid: "#status",
	popupid: "#error_dialog",
	listid: "ul",

	msg: function(status, src_id, descr) {
		if(src_id in this.content) {
			this.nums[this.content[src_id].status]-=1;
			if(descr.length==0 && status==this.INFO)
				delete this.content[src_id];
		}
		if(!(descr.length==0 && status==this.INFO)) { 
			this.nums[status]+=1;
			this.content[src_id] = {status: status, descr: descr};
		}
		this.update();
	},
	error: function(src_id, descr) {this.msg(this.ERROR, src_id, descr);},
	warn: function(src_id, descr) {this.msg(this.WARNING, src_id, descr);},
	info: function(src_id, descr) {this.msg(this.INFO, src_id, descr);},
	clear: function(src_id) {this.msg(this.INFO, src_id, "");},

	handle: function(id,data) {
		var s=data.split(",",2);
		var st=this.ERROR;
		if(s[0]=="OK") st=this.INFO;
		else if(s[0]=="WARNING") st=this.WARNING;
		this.msg(st,id,s[1]);
	},

	update: function() {
		if($(this.divid).html().length<90) $(this.divid).html('<img src="img/error.gif" class="img_err"/><img src="img/warning.png" class="img_warn"/>');
		if(this.nums[this.ERROR]>0) {
			$(this.divid).find(".img_err").show();
			$(this.divid).find(".img_warn").hide();}
		else if(this.nums[this.WARNING]>0) {
			$(this.divid).find(".img_err").hide();
			$(this.divid).find(".img_warn").show();}
		else {
			$(this.divid).find(".img_err").hide();
			$(this.divid).find(".img_warn").hide();}
	},

	init: function() {
		$( this.popupid ).bind({
		   popupbeforeposition: this.show
		});
	},

	show: function() {
		for(var i=0; i<3; i++) {
			var el = $(this).find("ul").eq(i);
			el.html(error_handler.generate(i));
			el.listview('refresh');
		}
	},
	generate: function(st) {
		var s="";
		for(var c in this.content) {
			if(this.content[c].status!=st) continue;
			s+='<li><h3>'+c+'</h3><p>'+this.content[c].descr+'</p></li>';
		}
		return s;
	}
};
