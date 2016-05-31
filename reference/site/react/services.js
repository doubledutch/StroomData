
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
		options.push(h('option',{value:'paggegrate'},'Partitioned Aggregate'))

		form.push(h('select',{name:'type',onChange:this.setServiceType},options))

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


var ServicePage = React.createClass({
	onCreateService:function(){
		store.dispatch({type:'SERVICE_CREATE'})
	},
	render: function(){
		var elements=[]
		// Add header
		// elements.push(h('p','So many streams...'))
		if(this.props.service_editor.show){
			elements.push(h(ServiceEditor,this.props))
		}else{
			elements.push(h('div',[h('input.form_button',{'type':'button','value':'New service','onClick':this.onCreateService})]))
			elements.push(h(TableView,{data:this.props.services,columns:[
				{key:'id',width:'flex'},
				{key:'service',width:'small'},
				{key:'type',width:'small'},
				{key:'state',width:'medium'}
			]}))
		}
		// Return enclosing div with elements
		return h('div',elements)
	}
})