



var StreamPage = React.createClass({
	render: function(){
		var elements=[]
		// Add header
		// elements.push(h('p','So many streams...'))

		elements.push(h(TableView,{data:this.props.streams,columns:[
			{key:'topic',width:'flex'},
			{key:'count',width:'medium'},
			{key:'size',width:'medium'}
		]}))

		// Return enclosing div with elements
		return h('div',elements)
	}
})