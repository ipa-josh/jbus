gl_vis_temp['cmd'] = {
	init: function($this) {
		$("#header").append('<a id="settings" style="position:absolute;right:50px;top:5px;padding:5px;width:23px;height:23px;"><img src="img/settings.png" style="top:-10px;position:relative;left:-16px;" /></a>').trigger('create');
		
		var id = "settings_dialog";
		$("body").append('<div id="'+id+'" title="Settings" data-role="dialog" style="color:white"/>').trigger('create');
	
		$("#"+id).append('<a data-role="button" class="settingsreboot">Reboot</a>').trigger('create');
		$("#"+id).append('<a data-role="button" class="settingsclose">Close</a>').trigger('create');
		
		$('.settingsclose').click( function() {
			$.mobile.changePage('#main');
		});
		
		$('.settingsreboot').data('obj',$this).click( function() {
			$this = $(this).data('obj');
			$this.JHA('set', 'reboot/true');
		});
		
		$("#settings").click(function() {
			$.mobile.changePage('#settings_dialog');
		});
	},
	vis: function($this, vis) {
	}
}