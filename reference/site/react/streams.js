
var StreamPage = React.createClass({
	render: function(){
		var elements=[]
		// Add header
		elements.push(h('p','So many streams...'))
		// Add services
		for(var i=0;i<this.props.streams.length;i++){
			var stream=this.props.streams[i]
			elements.push(h('div',stream.count+' '+stream.topic))
		}
		// Return enclosing div with elements
		return h('div',elements)
	}
})