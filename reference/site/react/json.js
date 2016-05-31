var JSONViewer=React.createClass({
	render:function(){
		var obj=this.props.data
		var rows=[]
		rows.push(h('div',[h('span.json_block','{')]))
		rows=rows.concat(renderJSONObject(obj,'    '))
		rows.push(h('div',[h('span.json_block','}')]))
		// return h('div.browse_page',JSON.stringify(this.props.data))
		return h('div.browse_page',rows)
	}
})

function renderJSONString(str){
	return [h('span.json_symbol','"'),h('span.json_string',str),h('span.json_symbol','"')]
}

function renderJSONKey(str){
	return [h('span.json_symbol','"'),h('span.json_key',str),h('span.json_symbol','"')]
}

function isFlatArray(arr){
	for(var i=0;i<arr.length;i++){
		var val=arr[i]
		if(typeof val === 'object'){
			return false
		}
	}
	return true
}

function renderValue(value,indent){
	var cols=[]
	if(indent!=''){
		cols.push(h('span',indent))
	}
	if(typeof value==='number'){
		cols.push(h('span.json_number',value))
	}else if(typeof value==='string'){
		cols=cols.concat(renderJSONString(value))
	}else if(typeof value==='boolean'){
		cols.push(h('span.json_boolean',''+value))
	}else if(typeof value==='undefined'){
		cols.push(h('span.json_null','null'))
	}
	return cols
}

function renderJSONArray(arr,indent){
	var result=[]
	if(isFlatArray(arr)){
		result.push(h('span',indent))
		for(var n=0;n<arr.length;n++){
			if(n>0)result.push(h('span.json_symbol',', '))
			result=result.concat(renderValue(arr[n],''))
		}
	}else{
		for(var n=0;n<arr.length;n++){
			var value=arr[n]
			var cols=[]
			if(typeof value === 'object'){
				if(Array.isArray(value)){
					// Render Array
					cols.push(h('span',indent))
					cols.push(h('span.json_block','['))
					result.push(h('div',cols))

					result=result.concat(renderJSONArray(value,indent+'    '))

					cols=[]
					cols.push(h('span',indent))
					cols.push(h('span.json_block',']'))
				}else if(value==null){
					cols.push(h('span.json_null','null'))
				}else{
					// Render nested object
					cols.push(h('span',indent))
					cols.push(h('span.json_block','{'))
					result.push(h('div',cols))
					result=result.concat(renderJSONObject(value,indent+'    '))
					cols=[]
					cols.push(h('span',indent))
					cols.push(h('span.json_block','}'))
				}
			}else{
				cols=renderValue(arr[n],indent)
			}
			if(n<arr.length-1)cols.push(h('span.json_symbol',','))
			result.push(h('div',cols))
		}
	}
	return result
}

function renderJSONObject(obj,indent){
	var result=[]
	var keys=Object.keys(obj)
	for(var i=0;i<keys.length;i++){
		var key=keys[i]

		var cols=[]
		cols.push(h('span',indent))
		cols=cols.concat(renderJSONKey(key))
		cols.push(h('span.json_symbol',' : '))
		var value=obj[key]
		if(typeof value==='object'){
			if(Array.isArray(value)){
				// Render Array
				cols.push(h('span.json_block','['))
				result.push(h('div',cols))

				result=result.concat(renderJSONArray(value,indent+'    '))

				cols=[]
				cols.push(h('span',indent))
				cols.push(h('span.json_block',']'))
			}else if(value==null){
				cols.push(h('span.json_null','null'))
			}else{
				// Render nested object
				cols.push(h('span.json_block','{'))
				result.push(h('div',cols))
				result=result.concat(renderJSONObject(value,indent+'    '))
				cols=[]
				cols.push(h('span',indent))
				cols.push(h('span.json_block','}'))
			}
		}else{
			cols=cols.concat(renderValue(value))
		}
		if(i<keys.length-1){
			cols.push(h('span.json_symbol',','))
		}
		if(cols.length>0){
			result.push(h('div',cols))
		}
	}
	return result
}