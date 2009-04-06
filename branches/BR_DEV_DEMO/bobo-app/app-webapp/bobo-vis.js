var bobovis = {};

bobovis.Selection = function(val,selected)
{
	this.val=val;
	this.selected=selected;
}

bobovis.CheckList = function(container){
	this.containerElement = container;
	this.name = container.id;
	this.Selection = new bobovis.Selection("",false);
}

bobovis.CheckList.prototype.getSelection = function(){
	return this.Selection;
}

bobovis.CheckList.prototype.handleClick = function(elem){
	this.Selection.val=elem.name;
	this.Selection.selected=elem.checked;
	google.visualization.events.trigger(this,'select', {});
}

bobovis.removeChildren=function(cell)
{		    	
	if ( cell.hasChildNodes() )
	{
	    while ( cell.childNodes.length >= 1 )
	    {
	        cell.removeChild( cell.firstChild );       
	    } 
	}
}


bobovis.CheckList.prototype.draw = function(data,options){
	var html = [];
    bobovis.removeChildren(this.containerElement);
    var src = this;
	for (var row = 0; row < data.getNumberOfRows(); row++) {
	  var val = bobovis.escapeHtml(data.getFormattedValue(row, 0));
	  var hits = bobovis.escapeHtml(data.getFormattedValue(row, 1));
	  var elem=document.createElement("input");
	  var html=[];
	  elem.type='checkbox';
	  elem.name=val;
	  elem.onclick=function(){src.handleClick(this)};
	  this.containerElement.appendChild(elem);

	  this.containerElement.appendChild(document.createTextNode(val+" ("+hits+")"));
	}
}

bobovis.escapeHtml = function(text)
{
	if (text==null) return '';
	return text.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>').replace(/"/g, '"');
}

