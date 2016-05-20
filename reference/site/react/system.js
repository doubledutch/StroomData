
var SystemBar = React.createClass({
	render: function(){
		var items=[]
		items.push(h('div.title','Stroom'))
		var date=this.props.system.build_date
		date=date.substr(0, date.indexOf('T')); 
		items.push(h('div.version','DD reference implementation v'+this.props.system.build_version+' b'+this.props.system.build_number+' (built on '+date+')'))
		items.push(h(SystemStat,{value:this.props.system.service_threads,title:'Threads'}))
		items.push(h(SystemStat,{value:this.props.system.cpu_cores,title:'CPU Cores'}))
		items.push(h(SystemMeter,{value:this.props.system.disk_used,max:this.props.system.disk_total,title:'Disk Space'}))
		items.push(h(SystemMeter,{value:this.props.system.memory_used,max:this.props.system.memory_max,title:'Memory'}))
		return h('div.system',items)
	}
})

var SystemMeter=React.createClass({
	render:function(){
		var value=this.props.value
		var max=this.props.max
		// console.log('v '+value+'  m '+max)
		var ratio=value/max
		var size=Math.floor(ratio*146)
		return h('div.meter',[h('div.meter_title',this.props.title),h('div.meter_container',[h('div.meter_value',{'style':{'width':size+'px'}})])])
	}
})

var SystemStat=React.createClass({
	render:function(){
		var value=this.props.value
		return h('div.meter',[h('div.meter_title',this.props.title),h('div.meter_numeric',this.props.value)])
	}
})