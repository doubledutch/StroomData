
var ServiceEditor =React.createClass({
	onSave:function(e){
		store.dispatch({type:'SERVICE_SAVE'})
		// console.log(this.props.service_editor)
	},
	onCancel:function(e){
		store.dispatch({type:'SET',path:['service_editor','show'],value:false})
	},
	setServiceType:function(e){
		store.dispatch({type:'SET',path:['service_editor','service_type'],value:e.target.value})
	},
	setName:function(e){
		store.dispatch({type:'SET',path:['service_editor','id'],value:e.target.value})
	},
	setIn:function(e){
		store.dispatch({type:'SET',path:['service_editor','in'],value:e.target.value})
	},
	setOut:function(e){
		store.dispatch({type:'SET',path:['service_editor','out'],value:e.target.value})
	},
	setPartitionKey:function(e){
		store.dispatch({type:'SET',path:['service_editor','partition_key'],value:e.target.value})
	},
	setFunctionType:function(e){
		store.dispatch({type:'SET',path:['service_editor','function_type'],value:e.target.value})
	},
	setScript:function(e){
		store.dispatch({type:'SET',path:['service_editor','script'],value:e.target.value})
	},
	render:function(){
		var elements=[]
		elements.push(h('div.browse_header','Edit Service'))
		
		var form=[]
		form.push(h('label','Service Name'))
		form.push(h('input',{name:'name',onChange:this.setName,defaultValue:this.props.service_editor.name}))


		form.push(h('div.form_separator',''))

		form.push(h('label','Service Type'))
		var options=[]
		options.push(h('option',{value:'filter'},'Filter'))
		options.push(h('option',{value:'aggregate'},'Aggregate'))
		options.push(h('option',{value:'partitioned_aggegrate'},'Partitioned Aggregate'))
		options.push(h('option',{value:'kvstore'},'Key Value Store'))

		form.push(h('select',{name:'type',onChange:this.setServiceType},options))

		if(this.props.service_editor.service_type=='partitioned_aggegrate'){
			form.push(h('label','Partition Key'))
			form.push(h('input',{name:'partition_key',onChange:this.setPartitionKey,defaultValue:''}))
		}

		form.push(h('div.form_separator',''))

		form.push(h('label','Input Stream'))
		options=[]
		for(var i=0;i<this.props.streams.length;i++){
			options.push(h('option',{value:this.props.streams[i].topic},this.props.streams[i].topic))
		}
		form.push(h('select',{name:'in_stream_select',onChange:this.setIn},options))

		form.push(h('div.form_separator',''))

		form.push(h('label','Output Stream'))
		options=[]
		// for(var i=0;i<this.props.streams.length;i++){
		//	options.push(h('option',{value:this.props.streams[i].topic},this.props.streams[i].topic))
		// }
		// form.push(h('select',{name:'out_stream_select'},options))
		form.push(h('input',{name:'out_stream',onChange:this.setOut,defaultValue:''}))

		form.push(h('div.form_separator',''))

		form.push(h('label','Function Type'))
		options=[]
		options.push(h('option',{value:'javascript'},'Javascript'))
		options.push(h('option',{value:'sql'},'SQL'))
		options.push(h('option',{value:'http'},'External HTTP endpoint'))
		form.push(h('select',{name:'function',onChange:this.setFunctionType,value:this.props.service_editor.function_type},options))

		if(this.props.service_editor.function_type=='javascript'){

			form.push(h('label','Script'))
			options=[]
			for(var i=0;i<this.props.scripts.length;i++){
				if(this.props.scripts[i].name.endsWith(".js") || this.props.scripts[i].name.endsWith(".javascript")){
					options.push(h('option',{value:this.props.scripts[i].name},this.props.scripts[i].name))
				}
			}
			form.push(h('select',{name:'script',onChange:this.setScript},options))
		}else if(this.props.service_editor.function_type=='sql'){

			form.push(h('label','Script'))
			options=[]
			for(var i=0;i<this.props.scripts.length;i++){
				if(this.props.scripts[i].name.endsWith(".sql")){
					options.push(h('option',{value:this.props.scripts[i].name},this.props.scripts[i].name))
				}
			}
			form.push(h('select',{name:'script',onChange:this.setScript},options))
		}else if(this.props.service_editor.function_type=='http'){

			form.push(h('label','URL'))

			form.push(h('input',{name:'url',onChange:this.setUrl,defaultValue:this.props.service_editor.url}))
		}
		elements.push(h('div.browse_page',form))
		/*elements.push(h('div.browse_page',[
			h('input',{'defaultValue':this.props.script_editor.name,'name':'name',onChange:this.saveFilename}),
			h('textarea.editor',{'defaultValue':this.props.script_editor.data,'name':'script_content',onChange:this.saveScript,onKeyDown:this.onKeyDown})
			]))
		if(this.props.script_editor.eval_success!=null){
			if(this.props.script_editor.eval_success){
				elements.push(h('div.eval_success','The script was evaluated without error!'))
			}else{
				console.log()
				elements.push(h('div.eval_error',[h('div',this.props.script_editor.eval_error.toString())]))
			}
		}*/
		elements.push(h('div.browse_footer',[
				h('input.form_button',{'type':'button','value':'Save','onClick':this.onSave}),
				// h('input.form_button',{'type':'button','value':'Eval','onClick':this.onEval}),
				h('input.form_button',{'type':'button','value':'Cancel','onClick':this.onCancel})
			]))
		return h('div',elements)
	}
})

var ServiceInspect = React.createClass({
	onReset:function(e){
		if(confirm("Are you sure you wan't to reset this service? this will delete all of its output data! This can NOT be undone!!!")){
			fetch('/service/'+this.props.id+'/reset', {
	  		method: 'POST',
			headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
			}
		}).then(function(response) {
		    return response.json()
		}).then(function(json) {
			updateSources();
		}).catch(function(ex) {
		    console.log('parsing failed', ex)
		})
		}
	},
	onStop:function(e){
		fetch('/service/'+this.props.id+'/stop', {
	  		method: 'POST',
			headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
			}
		}).then(function(response) {
		    return response.json()
		}).then(function(json) {
			updateSources();
		}).catch(function(ex) {
		    console.log('parsing failed', ex)
		})
	},
	onStart:function(e){
		fetch('/service/'+this.props.id+'/start', {
	  		method: 'POST',
			headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
			}
		}).then(function(response) {
		    return response.json()
		}).then(function(json) {
			updateSources();
		}).catch(function(ex) {
		    console.log('parsing failed', ex)
		})
	},
	onRestart:function(e){
		fetch('/service/'+this.props.id+'/restart', {
	  		method: 'POST',
			headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
			}
		}).then(function(response) {
		    return response.json()
		}).then(function(json) {
			updateSources();
		}).catch(function(ex) {
		    console.log('parsing failed', ex)
		})
	},
	onDisable:function(e){
		fetch('/service/'+this.props.id+'/disable', {
	  		method: 'POST',
			headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
			}
		}).then(function(response) {
		    return response.json()
		}).then(function(json) {
			updateSources();
		}).catch(function(ex) {
		    console.log('parsing failed', ex)
		})
	},
	onEnable:function(e){
		fetch('/service/'+this.props.id+'/enable', {
	  		method: 'POST',
			headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
			}
		}).then(function(response) {
		    return response.json()
		}).then(function(json) {
			updateSources();
		}).catch(function(ex) {
		    console.log('parsing failed', ex)
		})
	},
	render:function(){
		// console.log(this.props.metrics)
		var elements=[]

		var metrics=[]
		if(this.props.state=='RUNNING'){
			var max=0
			for(var key in this.props.metrics){
				var val=this.props.metrics[key].avg
				if(val>max){
					max=val
				}
			}
			for(var key in this.props.metrics){
				if(key!='batch.time'){
					metrics.push(h('div.metrics_entry',[
						h('div.metrics_title',key),
						h('div.metrics_bar_container',[h('div.metrics_bar',{style:{width:Math.floor((this.props.metrics[key].avg/max)*200)+'px'}})]),
						h('div.metrics_value',Math.floor(this.props.metrics[key].avg/1000)/1000+' ms')
					]))
				}
			}

			elements.push(h('div.metrics_table',metrics))
		}
		var fields=[]
		if(this.props.state=='RUNNING'){
			fields.push(h('input.form_button',{'type':'button','value':'Stop','onClick':this.onStop}))
			fields.push(h('input.form_button',{'type':'button','value':'Restart','onClick':this.onRestart}))
		}else if(this.props.state=='STOPPED'){
			fields.push(h('input.form_button',{'type':'button','value':'Start','onClick':this.onStart}))
		}
		fields.push(h('div.form_spacer'))
		if(this.props.state!='DISABLED'){
			fields.push(h('input.form_button',{'type':'button','value':'Disable','onClick':this.onDisable}))
		}else{
			fields.push(h('input.form_button',{'type':'button','value':'Re-enable','onClick':this.onEnable}))
		}
		fields.push(h('input.form_button',{'type':'button','value':'Reset','onClick':this.onReset}))
		elements.push(h('div.inspector',fields))
		return h('div',elements)
	}
})

var ServicePage = React.createClass({
	onCreateService:function(){
		store.dispatch({type:'SERVICE_CREATE'})
	},
	render: function(){
		var elements=[]
		var currentService=this.props.currentService
		// Add header
		// elements.push(h('p','So many streams...'))
		if(this.props.service_editor.show){
			elements.push(h(ServiceEditor,this.props))
		}else{
			elements.push(h('div',[h('input.form_button',{'type':'button','value':'New service','onClick':this.onCreateService})]))
			elements.push(h(TableView,{data:this.props.services,
				columns:[
				{key:'id',width:'flex'},
				{key:'rate',width:'medium',format:'float:2'},
				{key:'index',width:'small',format:'integer'},
				// {key:'partitions',width:'small',format:'integer'},
				{key:'service',width:'medium'},
				{key:'type',width:'small'},
				{key:'state',width:'small'}
			],'selected':this.props.currentService
			,'inspectorClass':ServiceInspect,
			'selectRow':function(id){
				// console.log('click on '+id)
				if(currentService==id){
					store.dispatch({type:'SET',path:['currentService'],value:null})
				}else{
					store.dispatch({type:'SET',path:['currentService'],value:id})
				}
			}}))
		}
		// Return enclosing div with elements
		return h('div',elements)
	}
})