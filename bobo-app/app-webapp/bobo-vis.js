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

bobovis.Link = function(text,value){
	this.node=document.createElement("a");
	this.node.innerHTML=text;
	this.node.name=value;
		
	this.node.onmouseout=function(){
		this.style.color='black';
		this.style.fontWeight='normal';
	}
	
	this.node.onmouseover=function(){
		this.style.cursor='pointer';
		this.style.color='blue';
		this.style.fontWeight='bold';
	}
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

bobovis.stringCompare = function(s1,s2){
    var len=Math.min(s1.length,s2.length);
	for (var i=0;i<len;++i){
		if (s1.charAt(i) < s2.charAt(i)) return -1;
		if (s1.charAt(i) > s2.charAt(i)) return 1;
	}
	
	if (len<s1.length){
		return 1;
	}
	else if (len<s2.length){
		return -1;
	}
	else{
		return 0;
	}
}


bobovis.CheckList.prototype.draw = function(data,options){
	bobovis.removeChildren(this.containerElement);
    var src = this;
	for (var row = 0; row < data.getNumberOfRows(); row++) {
	  var val = bobovis.escapeHtml(data.getFormattedValue(row, 0));
	  var hits = bobovis.escapeHtml(data.getFormattedValue(row, 1));
	  var isSelected = data.getFormattedValue(row,2) === 'true';
	  var elem=document.createElement("input");
	  elem.type='checkbox';
	  elem.name=val;
	  elem.checked = isSelected;
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


bobovis.SelectList = function(container){
	this.containerElement = container;
	this.name = container.id;
	this.Selection = new bobovis.Selection("",false);
}


bobovis.SelectList.prototype.getSelection = function(){
	return this.Selection;
}

bobovis.SelectList.prototype.handleClick = function(elem){
	this.Selection.val=elem.value;
	this.Selection.selected=elem.selected;
	google.visualization.events.trigger(this,'select', {});
}

bobovis.SelectList.prototype.draw = function(data,options){
    bobovis.removeChildren(this.containerElement);
    var selectList=document.createElement("select");
    var src = this;
    var multi = false;
    if (options!=null){
      multi = options.multi;
    }
    if (multi){
      selectList.multiple=true;
    }
    this.containerElement.appendChild(selectList);
    for (var row = 0; row < data.getNumberOfRows(); row++) {
      var val = bobovis.escapeHtml(data.getFormattedValue(row, 0));
	  var hits = bobovis.escapeHtml(data.getFormattedValue(row, 1));
	  var isSelected = data.getFormattedValue(row,2) === 'true';
	  var optionNode = document.createElement("option");
	  optionNode.value=val;
	  if (isSelected){
	    optionNode.selected=true;
	  }
	  optionNode.onclick=function(){src.handleClick(this)};
	  selectList.appendChild(optionNode);
	  optionNode.appendChild(document.createTextNode(val+" ("+hits+" )"));
    }
}

