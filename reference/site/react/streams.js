function updateStreamBrowser(){
	var state=store.getState().stream_browser
	fetch('/stream/'+state.topic+'/',{
  		method: 'GET',
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		}
	}).then(function(response) {
	    return response.json()
	}).then(function(json) {
		// console.log(json)
		store.dispatch({type:'SET',path:['stream_browser','count'],value:json.count})
		var index=state.index
		// TODO: only do anything else if we are not already showing the desired document
		if(state.browse=='_'){
			index=json.count-1
			store.dispatch({type:'SET',path:['stream_browser','index'],value:json.count-1})
		}else{
			index=state.browse
		}

		fetch('/stream/'+state.topic+'/'+index,{
	  		method: 'GET',
			headers: {
				'Accept': 'application/json',
				'Content-Type': 'application/json'
			}
		}).then(function(response) {
		    return response.json()
		}).then(function(json) {
			
			store.dispatch({type:'SET',path:['stream_browser','data'],value:json})
		}).catch(function(ex) {
		    console.log('parsing failed', ex)
		})
	}).catch(function(ex) {
	    console.log('parsing failed', ex)
	})
}


var StreamInspect = React.createClass({
	onViewData:function(e){
		store.dispatch({type:'BROWSE_OPEN',value:this.props.topic})
		updateStreamBrowser()
	},
	onDeleteData:function(e){
		if(confirm("Are you sure you wan't to delete all data in "+this.props.topic+"? This can NOT be undone!!!")){
			fetch('/stream/'+this.props.topic+'/0', {
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
				store.dispatch({type:'SET',path:['currentStream'],value:null})
				updateSources();
			}).catch(function(ex) {
			    console.log('parsing failed', ex)
			})
		}
	},
	onDeleteStream:function(e){
		if(confirm("Are you sure you wan't to delete "+this.props.topic+" and all data in it? This can NOT be undone!!!")){
			fetch('/stream/'+this.props.topic+'/', {
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
				store.dispatch({type:'SET',path:['currentStream'],value:null})
				updateSources();
			}).catch(function(ex) {
			    console.log('parsing failed', ex)
			})
		}
	},
	render:function(){
		var fields=[]
		fields.push(h('input.form_button',{'type':'button','value':'Browse data','onClick':this.onViewData}))
		fields.push(h('div.form_spacer'))
		fields.push(h('input.form_button',{'type':'button','value':'Delete data','onClick':this.onDeleteData}))
		fields.push(h('input.form_button',{'type':'button','value':'Delete stream','onClick':this.onDeleteStream}))
		return h('div.inspector',fields)
	}
})

var StreamBrowserFooter=React.createClass({
	gotoPrev:function(){
		store.dispatch({type:'BROWSE_VIEW',value:this.props.index-1})
		// console.log('goto previous')
	},
	gotoNext:function(){
		store.dispatch({type:'BROWSE_VIEW',value:this.props.index+1})
		// console.log('goto next')
	},
	startTail:function(){
		// console.log('goto tail')
		store.dispatch({type:'SET',path:['stream_browser','browse'],value:'_'})
	},
	stopTail:function(){
		store.dispatch({type:'SET',path:['stream_browser','browse'],value:this.props.index})
		// console.log('stop tail')
	},
	onClose:function(){
		store.dispatch({type:'BROWSE_CLOSE'})
	},
	render:function(){
		var elements=[]
		if(this.props.browse=='_'){
			elements.push(h('input.form_button',{'type':'button','value':'Stop tail','onClick':this.stopTail}))
		}else{
			if(this.props.index>0){
				elements.push(h('input.small_button',{'type':'button','value':'<','onClick':this.gotoPrev}))
			}
			if(this.props.index<this.props.count-1){
				elements.push(h('input.small_button',{'type':'button','value':'>','onClick':this.gotoNext}))
			}
			elements.push(h('div.form_spacer'))
			elements.push(h('input.form_button',{'type':'button','value':'Start tail','onClick':this.startTail}))
			
		}
		// TODO: place close button in a more appealing location
		elements.push(h('div.form_spacer'))
		elements.push(h('input.form_button',{'type':'button','style':{'float':'right'},'value':'Close','onClick':this.onClose}))
		return h('div.browse_footer',elements)
	}
})





var StreamBrowser=React.createClass({
	render:function(){
		var elements=[]
		elements.push(h('div.browse_header',this.props.stream_browser.topic+' [ '+this.props.stream_browser.index+' ] '))
		if(this.props.stream_browser.data==null){
			elements.push(h('div.browse_page','...loading data...'))
		}else{
			elements.push(h(JSONViewer,{data:this.props.stream_browser.data}))
		}
		elements.push(h(StreamBrowserFooter,this.props.stream_browser));
		return h('div',elements)
	}
})

var StreamPage = React.createClass({
	render: function(){
		var currentStream=this.props.currentStream
		var elements=[]
		// Add header
		// elements.push(h('p','So many streams...'))
		// console.log('render '+this.props.currentStream)
		if(this.props.stream_browser.show){
			elements.push(h(StreamBrowser,this.props))
		}else{
			elements.push(h(TableView,{id:'streams',data:this.props.streams,columns:[
				{key:'topic',width:'flex'},
				{key:'count',width:'medium',format:'integer'},
				{key:'size',width:'medium',format:'data'}
			],'selected':this.props.currentStream
			,'inspectorClass':StreamInspect
			,'selectRow':function(id){
				if(currentStream==id){
					store.dispatch({type:'SET',path:['currentStream'],value:null})
				}else{
					store.dispatch({type:'SET',path:['currentStream'],value:id})
				}
			}}))
		}
		// Return enclosing div with elements
		return h('div',elements)
	}
})