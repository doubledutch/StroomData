var ScriptInspect = React.createClass({
	onEditScript:function(e){
		var name=this.props.name
		// console.log(this.props.name)
		fetch('/script'+name, {
	  		method: 'GET',
			headers: {
				'Accept': 'application/json',
				'Content-Type': 'application/json'
			}
		}).then(function(response) {
			// console.log(response.text())
		    return response.text()
		}).then(function(data){
			// console.log(data)
			store.dispatch({type:'SCRIPT_EDIT',name:name,data:data})
		}).catch(function(ex) {
		    console.log('parsing failed', ex)
		})
	},
	onDeleteScript:function(){
		if(confirm("Are you sure you wan't to delete the script "+this.props.name+"? This can NOT be undone!!!")){
			console.log('delete')
			var name=this.props.name
			// console.log(this.props.name)
			fetch('/script'+name, {
		  		method: 'DELETE',
				headers: {
					'Accept': 'application/json',
					'Content-Type': 'application/json'
				}
			}).then(function(response) {
				// console.log(response.text())
			    return response.text()
			}).then(function(data){
				// console.log(data)
				// store.dispatch({type:'SCRIPT_EDIT',name:name,data:data})
				updateSources();
			}).catch(function(ex) {
			    console.log('parsing failed', ex)
			})
		}
	},
	render:function(){
		var fields=[]
		fields.push(h('input.form_button',{'type':'button','value':'Edit','onClick':this.onEditScript}))
		fields.push(h('div.form_spacer'))
		fields.push(h('input.form_button',{'type':'button','value':'Delete script','onClick':this.onDeleteScript}))
		return h('div.inspector',fields)
	}
})

function updateScriptRunner(){
	var state=store.getState()

	var source=state.script_editor.sample_source
	if(source==null){
		// TODO: such a hack - set all of this up when "new script" is clicked
		source=state.streams[0].topic
	}
	// console.log('source')
	// console.log(source)
	if(source!=null){
		(function(){
			try{
				var stroom={
					getStream:function(name){
						return {
							get:function(index,endIndex){
								return []
							},
							get:function(index){
								return ""
							},
							getLast:function(){
								return ""
							},
							getCount:function(){
								return 0
							},
							append:function(data){
								console.log(data)
								return 0
							},
							append:function(data,hint){
								console.log(data)
								return 0
							},
							truncate:function(index){

							}
						}
					}
				}
				eval(state.script_editor.data)
				store.dispatch({type:'SET',path:['script_editor','eval_success'],value:null})
				var count=getStream(state.streams,source).count
				if(count>state.script_editor.sample_count){
					var loc=Math.floor(Math.random()*(count-parseInt(state.script_editor.sample_count)))

					// console.log(loc)
					// Get count

					fetch('/stream/'+source+'/'+loc+'-'+(loc+parseInt(state.script_editor.sample_count-1)),{
				  		method: 'GET',
						headers: {
							'Accept': 'application/json',
							'Content-Type': 'application/json'
						}
					}).then(function(response) {
					    return response.json()
					}).then(function(json) {
						try{
							// console.log('should be running')
							var ftype=state.script_editor.sample_function
							var data=[]
							var result=null
							console.log('count: '+json.length)
							console.log('selected:'+state.script_editor.sample_count)
							for(var i=0;i<json.length;i++){
								var doc=JSON.parse(JSON.stringify(json[i]))
								if(ftype=='map'){
									result=map(json[i])
								}else if(ftype=='reduce'){
									result=reduce(result,json[i])
								}
								data.push({
									'doc':doc,'result':JSON.parse(JSON.stringify(result))
								})
							}
							store.dispatch({type:'SET',path:['script_editor','sample_data'],value:data})
						}catch(inner_ex){
							// console.log(inner_ex)
							store.dispatch({type:'SCRIPT_EVAL',success:false,error:inner_ex})
						}
					})
				}else{
					store.dispatch({type:'SCRIPT_EVAL',success:false,error:'The input stream needs to have at least '+state.script_editor.sample_count+' documents.'})
				}
			}catch(ex){
				// console.log(ex)
				store.dispatch({type:'SCRIPT_EVAL',success:false,error:ex})
				return
			}
		})();
	}
}

function getStream(streams,topic){
	for(var i=0;i<streams.length;i++){
		var stream=streams[i]
		if(stream.topic==topic)return stream
	}
	return null
}

var SampleResults=React.createClass({
	render:function(){
		if(this.props.sample_data.length==0){
			return h('div','')
		}else{
			var rendered=[]
			rendered.push(h('div.split_view',[h('div.split_header','Input'),h('div.split_header','Output')]))
			for(var i=0;i<this.props.sample_data.length;i++){
				var data=this.props.sample_data[i]
				rendered.push(h('div.split_view',[h('div.split',[h(JSONViewer,{'data':data.doc})]),h('div.split',[h(JSONViewer,{'data':data.result})])]))
			}
			rendered.push(h('div.split_view',[h('div'),h('div')]))
			return h('div',rendered)
		}
	}
})/*

var SampleRunner = React.createClass({
	runTest:function(e){
		console.log('click click')
		// store.dispatch({type:'SET',path:['script_editor','sample_run'],value:true})
		// updateScriptRunner()
	},
	setIn:function(e){
		store.dispatch({type:'SET',path:['script_editor','sample_source'],value:e.target.value})
	},
	render:function(){
		var elements=[]

		// elements.push(h('div.browse_header','Edit Script'))
		

		var options=[]
		for(var i=0;i<this.props.streams.length;i++){
			options.push(h('option',{value:this.props.streams[i].topic},this.props.streams[i].topic))
		}

		var footer=[]
		footer.push(h('label.form_label','Input Stream'))
		footer.push(h('select.form_select',{name:'in_stream_select',onChange:this.setIn},options))
		footer.push(h('label.form_label','Function'))
		options=[]
		options.push(h('option',{value:'map'},'Map'))
		options.push(h('option',{value:'reduce'},'Reduce'))
		footer.push(h('select.form_select',{name:'function_select',onChange:this.setFunction},options))
		footer.push(h('input.form_button',{'type':'button','value':'Run','onClick':this.runTest}))
		
		elements.push(h('div.browse_footer',footer))

		elements.push(h(SampleResults,this.props.script_editor))
		return h('div',elements)
	}
})*/

var ScriptEditor =React.createClass({
	onSave:function(e){
		var filename=this.props.script_editor.name
		if(!filename.startsWith('/')){
			filename='/'+filename;
		}
		fetch('/script'+filename, {
		  		method: 'POST',
				headers: {
					'Accept': 'application/json',
					'Content-Type': 'application/json'
				},
				body:this.props.script_editor.data
			}).then(function(response) {
			    return response.json()
			}).then(function(json) {
	            console.log(json)
				// store.dispatch({type:'SERVER_STREAMS',data:json})
				// store.dispatch({type:'SET',path:['currentStream'],value:null})
				// updateSources();
			}).catch(function(ex) {
			    console.log('parsing failed', ex)
			})
	},
	onCancel:function(e){
		store.dispatch({type:'SET',path:['script_editor','show'],value:false})
	},
	saveFilename:function(e){
		store.dispatch({type:'SET',path:['script_editor','name'],value:e.target.value})
	},
	saveScript:function(e){
		store.dispatch({type:'SET',path:['script_editor','data'],value:e.target.value})
		// console.log(e.target.value)
	},
	onEval:function(e){
		try{
			(eval(this.props.script_editor.data))
			store.dispatch({type:'SCRIPT_EVAL',success:true,error:null})
		}catch(ex){
			console.log(ex)
			store.dispatch({type:'SCRIPT_EVAL',success:false,error:ex})
		}
	},
	onKeyDown:function(e){
		if(e.keyCode === 9){
	        var i1 = e.target.selectionStart
	        var i2 = e.target.selectionEnd
	        var target = e.target
	        var value = target.value
	        target.value = value.substring(0, i1) + "\t"+ value.substring(i2)
	        e.target.selectionStart = e.target.selectionEnd = i1 + 1
	        e.preventDefault()
	    }
	},
	runTest:function(e){
		store.dispatch({type:'SET',path:['script_editor','sample_run'],value:true})
		updateScriptRunner()
	},
	setIn:function(e){
		store.dispatch({type:'SET',path:['script_editor','sample_source'],value:e.target.value})
	},
	setFType:function(e){
		store.dispatch({type:'SET',path:['script_editor','sample_function'],value:e.target.value})
	},
	setSampleCount:function(e){
		store.dispatch({type:'SET',path:['script_editor','sample_count'],value:e.target.value})
	},
	render:function(){
		var elements=[]
		elements.push(h('div.browse_header','Edit Script'))
		elements.push(h('div.browse_page',[
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
		}
		var footer=[
				h('input.form_button',{'type':'button','value':'Save','onClick':this.onSave}),
				h('input.form_button',{'type':'button','value':'Cancel','onClick':this.onCancel}),
				h('div.form_spacer',''),
				h('input.form_button',{'type':'button','value':'Eval Script','onClick':this.onEval}),
				h('div.form_spacer','')
			]
		footer.push(h('label.form_label','In'))
		var options=[]
		for(var i=0;i<this.props.streams.length;i++){
			options.push(h('option',{value:this.props.streams[i].topic},this.props.streams[i].topic))
		}
		footer.push(h('select.form_select',{name:'in_stream_select',onChange:this.setIn},options))
		footer.push(h('label.form_label','Samples'))
		options=[]
		options.push(h('option',{value:'5'},'5'))
		options.push(h('option',{value:'20'},'20'))
		options.push(h('option',{value:'100'},'100'))
		footer.push(h('select.form_select',{name:'sample_select',onChange:this.setSampleCount},options))
		footer.push(h('label.form_label','Function'))
		options=[]
		options.push(h('option',{value:'map'},'Map'))
		options.push(h('option',{value:'reduce'},'Reduce'))
		footer.push(h('select.form_select',{name:'function_select',onChange:this.setFType},options))
		footer.push(h('input.form_button',{'type':'button','value':'Run Test','onClick':this.runTest}))
		
		elements.push(h('div.browse_footer',footer))
		elements.push(h(SampleResults,this.props.script_editor))
		// elements.push(h('div.browse_footer',))
		return h('div',elements)
	}
})

var ScriptPage = React.createClass({
	onCreateScript:function(){
		store.dispatch({type:'SCRIPT_CREATE'})
	},
	render: function(){
		var currentScript=this.props.currentScript
		var elements=[]
		// Add header
		// elements.push(h('p','So many streams...'))
		// console.log('render '+this.props.currentStream)
		if(this.props.script_editor.show){
			elements.push(h(ScriptEditor,this.props))
			if(this.props.show_sample){
				elements.push(h(SampleRunner,this.props))
			}
		}else{
			elements.push(h('div',[h('input.form_button',{'type':'button','value':'New script','onClick':this.onCreateScript})]))
			elements.push(h(TableView,{id:'scripts',data:this.props.scripts,columns:[
				{key:'name',width:'flex'},
				{key:'size',width:'medium'}
			],'selected':this.props.currentScript
			,'inspectorClass':ScriptInspect
			,'selectRow':function(id){
				if(currentScript==id){
					store.dispatch({type:'SET',path:['currentScript'],value:null})
				}else{
					store.dispatch({type:'SET',path:['currentScript'],value:id})
				}
			}}))
		}
		// Return enclosing div with elements
		return h('div',elements)
	}
})