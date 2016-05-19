
var MenuBar = React.createClass({
	render: function(){
		var menuItems=[]
		for(var i=0;i<this.props.pages.length;i++){
			var data=Object.assign({
				'selected':this.props.current_page==this.props.pages[i].id
			},this.props.pages[i])
			menuItems.push(h(MenuItem,data))
		}
		return h('div.menu',menuItems)
	}
})

var MenuItem = React.createClass({
	render: function(){
		var id=this.props.id
		return h('div.menu_item'+(this.props.selected?'.selected':''),{
			onClick:function(){
				store.dispatch({ type: 'MENU_SELECT', id:id})
			}
		},this.props.title)
	}
})