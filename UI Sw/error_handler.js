
error_handler = {
	content: {},
	nums: [0,0,0],

	ERROR: 0,
	WARNING: 1,
	INFO: 2,

	divid: "#status",

	msg: function(status, src_id, descr) {
		if(src_id in content)
			this.nums[content[src_id].status]-=1;
		this.nums[status]+=1;
		content[src_id] = {status: status, descr: descr};
		this.update();
	},
	error: function(src_id, descr) {this.msg(this.ERROR, src_id, descr);},
	warn: function(src_id, descr) {this.msg(this.WARNING, src_id, descr);},

	update: function() {
		if(this.nums[this.ERROR]>0)
			$(this.divid).html('<img src="img/error.gif" />');
		else if(this.nums[this.WARNING]>0)
			$(this.divid).html('<img src="img/warning.png" />');
		else
			$(this.divid).html('');
	},

	init: function() {
		$(this.divid).click(function() {
		});
	}
};
