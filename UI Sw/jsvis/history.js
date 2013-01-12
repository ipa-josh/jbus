gl_vis_temp['history'] = {
	load_css: function(css) {
		if (document.createStyleSheet){
                document.createStyleSheet(css);
            }
            else {
                $("head").append($("<link rel='stylesheet' href='"+css+"' type='text/css' media='screen' />"));
            }
	},
	load_js: function(js) {
		$.ajax({
			url: js,
			dataType: "script",
			cache: true,
			async: true,
			success: function(data) {
			},
			error: function(a,b,c) {
				alert("could not load "+js);
			}
		});
	},
	list:[],
	add: function(path, method) {
		if(typeof method==undefined) method="";
		this.list[this.list.length] = [path, method];
	},
	
	init: function($this) {
		history = this;
		$("#header").append('<a id="settings" style="position:absolute;right:20px;top:5px;padding:4px;width:25px;height:25px;"><img src="img/history.png" style="top:-10px;position:relative;left:-12px;top:-9px;" /></a>').trigger('create');
		
		this.load_css('css/jquery.jqChart.css');
		this.load_css('css/jquery.jqRangeSlider.css');
		
		this.load_js('dep/jquery.jqChart.min.js');
		this.load_js('dep/jquery.jqRangeSlider.min.js');
		
		var id = "history_dialog";
		$("body").append('<div id="'+id+'" title="Settings" data-role="dialog" style="color:white"/>').trigger('create');
		$("body").append('<div id="'+id+'_loading" title="Loading" data-role="dialog" style="color:white"><center>Loading...</center></div>').trigger('create');
	
		$("#"+id).append('<div id="jqChart" style="width: 700px; height: 500px;"></div>');
		
		function Julian2Date(v) {
			var U = (v-2440587.5)*86400.0;
			return new Date(U*1000);
		 }

		$("#settings").data('this',$this).click(function() {
			//setTimeout(function() {
			//$.mobile.changePage('#history_dialog_loading');}, 1);
			
			var $this = $(this).data('this');
			var series = [];
			for(var aa in history.list) {
				var a=history.list[aa];
				var data=[];
				var axis='y2';
				if(a[1]=='avg') {
					var data_ = $this.JHA('history',{method:a[1],from:-3600*24*32,to:0,path:a[0],step:7200}).res; //~1 month
					var old;
					for(var i=0; i<data_.length; i+=2) {
						if(data_[i]!='null') {
							data[data.length] = old = [Julian2Date(parseFloat(data_[i+1])),parseFloat(data_[i])];
							old[2]=i;
							old[3]=parseFloat(data_[i+1]);
						}
						else if (old)
							data[data.length] = [Julian2Date((old[3]+(i-old[2])/24.)),old[1]];
					}
					axis='y1';
				}
				else {
					var data_ = $this.JHA('history',{method:a[1],from:-3600*24*32,to:0,path:a[0],step:0}).res; //~1 month
					for(var i=0; i<data_.length; i+=2) {
						if(i!=0)
							data[data.length] = [Julian2Date((data_[i+1]-0.0001)),data_[i-2]=='true'?1:0];
						data[data.length] = [Julian2Date(parseFloat(data_[i+1])),data_[i]=='true'?1:0];
					}
				}
				var title = a[0].split("/");
				title=title[title.length-2];
				if(data.length>0)
					series[series.length] = {
										title: title,
										type: 'line',
										data: data,
										nullHandling: 'break',
										markers: null,
										axisY: axis
					};
			}
			
			var background = {
                type: 'linearGradient',
                x0: 0,
                y0: 0,
                x1: 0,
                y1: 1,
                colorStops: [{ offset: 0, color: '#e2e0d9' },
                             { offset: 1, color: 'white'}]
            };
			
			$('#jqChart').jqChart({
                title: 'History',
                legend: { title: 'Device' },
                border: { strokeStyle: '#6ba851' },
                background: background,
                animation: { duration: 2 },
                tooltips: { type: 'shared' },
                crosshairs: {
                    enabled: true,
                    hLine: false,
                    vLine: { strokeStyle: '#cc0a0c' }
                },
                axes: [
                        {
                            type: 'dateTime',
                            location: 'bottom',
                            zoomEnabled: true
                        },
						{
							name: 'y1',
							location: 'left'
						},
						{
							name: 'y2',
							location: 'right'
						}
                      ],
                series: series
            });
			
			$.mobile.changePage('#history_dialog');
		});
	},
	vis: function($this, vis) {
	}
}