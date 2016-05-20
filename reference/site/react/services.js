
var ServicePage = React.createClass({
	render: function(){
		var elements=[]
		// Add header
		// elements.push(h('p','So many streams...'))

		elements.push(h(TableView,{data:this.props.services,columns:[
			{key:'id',width:'flex'},
			{key:'service',width:'small'},
			{key:'type',width:'small'},
			{key:'state',width:'medium'}
		]}))

		// Return enclosing div with elements
		return h('div',elements)
	}
})