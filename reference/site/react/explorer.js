

var QueryEditor =React.createClass({
	onExecute:function(e){
		if(this.props.query_editor.id!=null){
			fetch('/query/'+this.props.query_editor.id, {
		  		method: 'DELETE',
				headers: {
					'Accept': 'application/json',
					'Content-Type': 'application/json'
				}
			})
		}
		fetch('/query/', {
		  		method: 'POST',
				headers: {
					'Accept': 'application/json',
					'Content-Type': 'application/json'
				},
				body:JSON.stringify({
					'type':'sql',
					'query':this.props.query_editor.data
				})
			}).then(function(response) {
			    return response.json()
			}).then(function(json) {
	            // console.log(json)
				// store.dispatch({type:'SERVER_STREAMS',data:json})
				store.dispatch({type:'SET',path:['query_editor','id'],value:json.id})
				store.dispatch({type:'SET',path:['current_query'],value:json})
				// updateSources();
			}).catch(function(ex) {
			    console.log('parsing failed', ex)
			})
	},
	onCancel:function(e){
		fetch('/query/'+this.props.query_editor.id, {
		  		method: 'DELETE',
				headers: {
					'Accept': 'application/json',
					'Content-Type': 'application/json'
				}
			}).then(function(response) {
			    return response.json()
			}).then(function(json) {
	            // console.log(json)
				// store.dispatch({type:'SERVER_STREAMS',data:json})
				store.dispatch({type:'SET',path:['query_editor','id'],value:null})
				// updateSources();
			}).catch(function(ex) {
			    console.log('parsing failed', ex)
			})
	},
	saveQuery:function(e){
		store.dispatch({type:'SET',path:['query_editor','data'],value:e.target.value})
		// console.log(e.target.value)
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
		elements.push(h('div.browse_header','SQL Query'))
		elements.push(h('div.browse_page',[
			h('textarea.sql_editor',{'defaultValue':this.props.query_editor.data,'name':'query_content',onChange:this.saveQuery,onKeyDown:this.onKeyDown})
			]))
		if(this.props.current_query!=null){
			if(this.props.current_query.state=='ERROR'){
				elements.push(h('div.eval_error',[h('div',this.props.current_query.error)]))
			}
		}
		var footer=[
			
		]
		if(this.props.query_editor.id==null || (this.props.current_query!=null && (this.props.current_query.state=='COMPLETED' || this.props.current_query.state=='ERROR'))){
			footer.push(h('input.form_button',{'type':'button','value':'Execute','onClick':this.onExecute}))
		}else{
			footer.push(h('input.form_button',{'type':'button','value':'Cancel query','onClick':this.onCancel}))
		}
		elements.push(h('div.browse_footer',footer))
		// elements.push(h(SampleResults,this.props.script_editor))
		// elements.push(h('div.browse_footer',))
		return h('div',elements)
	}
})

var QueryStatus = React.createClass({
	render:function(){
		var elements=[]
		if(this.props.state=='WAITING'){
			elements.push(h('div','Waiting for server side execution slot'))
		}else if(this.props.state=='RUNNING'){

			var text='Running '
			if(this.props.scan_total>0){
				text+='[ '+Math.floor(((this.props.scan_current)/(this.props.scan_total))*100)+'% ] '
			}
			text+=(this.props.time/1000)+' s'

			elements.push(h('div',text))
		}else if(this.props.state=='COMPLETED'){
			var text='Query executed in '+(this.props.time/1000)+' s. '
			text+='Scanning documents at a rate of '+Math.floor(this.props.scan_total/(this.props.time/1000))+' docs/s'
			elements.push(h('div',text))
		}
		return h('div.query_state',elements)
	}
})

var ExplorerPage = React.createClass({
	render: function(){
		var elements=[]
		elements.push(h(QueryEditor,this.props))
		if(this.props.query_editor.id!=null){
			elements.push(h(QueryStatus,this.props.current_query))
		}
		// elements.push(h('div',[h('input.form_button',{'type':'button','value':'Run','onClick':this.onRun})]))

		return h('div',elements)
	}
})