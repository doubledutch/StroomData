var TableView = React.createClass({
	renderHeader:function(){
		var fields=[]
		for(var i=0;i<this.props.columns.length;i++){
			var column=this.props.columns[i]
			var width=column.width
			var title=column.key
			fields.push(h('div.data_cell.'+width,title))
		}
		return h('div.data_header',fields)
	},
	renderRows:function(){
		var rows=[]
		for(var n=0;n<this.props.data.length;n++){
			var data=this.props.data[n]
			var fields=[]
			for(var i=0;i<this.props.columns.length;i++){
				var column=this.props.columns[i]
				var width=column.width
				var key=column.key
				fields.push(h('div.data_cell.'+width,data[key]))
			}
			rows.push(h('div.data_row',fields))
		}
		return h('div.data_table',rows)
	},
	render:function(){
		var elements=[]

		elements.push(this.renderHeader())
		elements.push(this.renderRows())
		return h('div',elements)
	}
})

