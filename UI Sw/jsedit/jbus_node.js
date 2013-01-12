gl_editor_temp['jbus_node'] = {
	init: function($this) {
		this.prototype = gl_editor_temp['default'];
		this.prototype.init($this, {draggable: true} );
		var id = 'dialog_jbus_'+$this.attr('id');
		$("body").append('<div id="'+id+'" title="JBus Node Configuration" data-role="dialog" style="color:white"/>').trigger('create');
		
		var hwid = $this.data("path").split("/");
		hwid = hwid[hwid.length-1].replace("node","");
		
		if(hwid==0) {
			var childs = $this.parent().JHA('getAllChildren');
			for(var j=0; j<childs.length; j++) {
				var hid = childs[j].data("path").split("/");
				hid = hid[hid.length-1];
				if(hid.search("node")!=0) continue;
				hwid = Math.max(hwid, hid.replace("node",""));
			}
			hwid+=1;
		}
		
		$("#"+id).append('<label for="hwid">Node ID: </label><input type="number" name="name" id="hwid" value="'+hwid+'" data-mini="true" />').trigger('create');
		
		var map = [ [0,'In'], [1,'In (pullup)'], [4, 'Analog'], [2, 'Out (0)'], [6, 'Out (1)'] ];
		
		var childs = $this.JHA('getAllChildren');
		var names = [];
		for(var j=0; j<childs.length; j++) {
			var pin = childs[j].data("path").split("/");
			pin = pin[pin.length-1];
			if(isNaN (pin-0)) continue;
			
			$("#"+id).append('<table><tr><td>Pin '+pin+': </td><td class="pinconfig'+pin+'"></td></tr></table>').trigger('create');
			var pc = $("#"+id).find(".pinconfig"+pin);
			
			pc.append('<fieldset data-role="controlgroup" data-type="horizontal" data-mini="true"></fieldset>').trigger('create');
			
			var fs = pc.find("fieldset");
			
			names[names.length] = pin;
			for(var i=0; i<map.length; i++)
				fs.append('<input type="radio" name="pin_type'+pin+'" id="'+pin+'r'+map[i][0]+'" value="'+map[i][0]+'" /><label for="'+pin+'r'+map[i][0]+'">'+map[i][1]+'</label>');
			
			fs.controlgroup('refresh')
		}

		$("#"+id).append('<a data-role="button" class="jbussave">Save</a>').trigger('create');
		$("#"+id).append('<a data-role="button" class="jbusclose">Close</a>').trigger('create');
		
		$this.click( function() {
			//load data from config
			$("#"+id).find("input:checked").removeAttr("checked")
			var config = jQuery.parseJSON($(this).JHA('getChild',{name:"config"}).data('data'));
			for(var pin in config) {
				if(isNaN (pin-0)) continue;
				$("#"+id).find("#"+pin+"r"+config[pin]).attr('checked',true).checkboxradio("refresh");
			}
			$.mobile.changePage('#dialog_jbus_'+$this.attr('id'));
		});
		
		$('.jbusclose').click( function() {
			$.mobile.changePage('#main');
		});
		
		$('.jbussave').data('names',names).data('form',$("#"+id)).data('obj',$this).click( function() {
			var form = $(this).data('form');
			var names = $(this).data('names');
			var hwid = form.find("#hwid").val();
			
			var c='"{\\"id\\":'+hwid;
			
			for(var i=0; i<names.length; i++) {
				var v = form.find('input[name=pin_type'+names[i]+']:radio:checked').val();
				if(v==undefined) v=0;
				c+=',\\"'+names[i]+'\\":' + v;
			}
			
			c+='}"';
			
			$(this).data('obj').JHA('set', 'config/'+c);
			$.mobile.changePage('#main');
		});
		
	},
	edit: function($this, vis) {
	}
}

editor_intf.addMenuEntry(2, '<div id="me_light"><img src="img/jbus_node.png" height="24px" /> JBus Node</div>');

$("#me_jbus_node").droppableMenuEntry({type: 'jbus_node'});