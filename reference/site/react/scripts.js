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

// TODO: write server side "compile" endpoint
//       what about unit tests?

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
		elements.push(h('div.browse_footer',[
				h('input.form_button',{'type':'button','value':'Save','onClick':this.onSave}),
				h('input.form_button',{'type':'button','value':'Eval','onClick':this.onEval}),
				h('input.form_button',{'type':'button','value':'Cancel','onClick':this.onCancel})
			]))
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