var TableView = React.createClass({
	onClick:function(e){
		var target=e.target
		while(target.id==''){
			target=target.parentNode
		}
		if("selectRow" in this.props){
			this.props.selectRow(target.id)
		}
	},
	renderHeader:function(){
		var fields=[]
		for(var i=0;i<this.props.columns.length;i++){
			var column=this.props.columns[i]
			var width=column.width
			var title=column.key
			var format=column.format
			var align=''
			if(format!=null){
				if(format=='data' || format=='integer' || format.startsWith('float')){
					align='.right'
				}
			}
			fields.push(h('div.data_cell.'+width+align,title))
		}
		return h('div.data_header',fields)
	},
	renderRows:function(){
		function pickValue( propertyName, object ) {
		  var parts = propertyName.split( "." ),
		    length = parts.length,
		    i,
		    property = object || this;

		  for ( i = 0; i < length; i++ ) {
		    property = property[parts[i]];
		  }

		  return property;
		}

		var rows=[]
		for(var n=0;n<this.props.data.length;n++){
			var data=this.props.data[n]
			var fields=[]
			var id=this.props.id+'-'+n
			if("id" in data){
				id=this.props.id+'-'+data.id
			}
			if(this.props.rowClass!=null){
				fields=h(this.props.rowClass,data)
			}else{
				for(var i=0;i<this.props.columns.length;i++){
					var column=this.props.columns[i]
					var width=column.width
					var key=column.key
					var value=pickValue(key,data) // data[key]
					var format=column.format
					var align=''
					var unit=''
					if(format!=null){
						
						if(format=='data'){
							unit=' B '
							if(value>10*1024){
								unit=' KB'
								value=value/1024
							}
							if(value>10*1024){
								unit=' MB'
								value=value/1024
							}
							if(value>10*1024){
								unit=' GB'
								value=value/1024
							}
							value=Math.floor(value)
							align='.right'
						}else if(format=='integer'){
							align='.right'
							value=value.toString().replace(/\B(?=(\d{3})+(?!\d))/g,",")
						}else if(format.startsWith('float')){
							align='.right'
							if(format.indexOf(':')>-1){
								var svalue=''+value
								var decimals=parseInt(format.substring(format.indexOf(':')+1))
								if(svalue.indexOf('.')>-1){
									var pre=svalue.substring(0,svalue.indexOf('.'))
									var post=svalue.substring(svalue.indexOf('.')+1)
									if(post.length>decimals){
										post=post.substring(0,decimals)
									}
									value=pre+'.'+post
								}
							}
						}

					}
					fields.push(h('div.data_cell.'+width+align,value+unit))
				}
			}
			var classname='div.data_row'
			classname+='#'+id
			if(this.props.selected==id && this.props.inspectorClass!=null){
				classname='div.data_row.open_row#'+id
				rows.push(h(classname,{'onClick':this.onClick},fields))
				rows.push(h(this.props.inspectorClass,data))
			}else{
				rows.push(h(classname,{'onClick':this.onClick},fields))
			}
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

