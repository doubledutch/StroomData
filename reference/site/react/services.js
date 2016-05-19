
var ServicePage = React.createClass({
	render: function(){
		var elements=[]
		// Add header
		elements.push(h('p','So many services...'))
		// Add services
		for(var i=0;i<this.props.services.length;i++){
			var service=this.props.services[i]
			elements.push(h('div',service.id))
		}
		// Return enclosing div with elements
		return h('div',elements)
	}
})