

bobovis.TagCloud = function(container){
  this.containerElement = container;
  this.name=container.id;
  this.table=document.createElement("table");
  this.table.width="100%";
  this.containerElement.appendChild(this.table);
  this.topRow=this.table.insertRow(0);
  this.topCell=this.topRow.insertCell(0);
    
  this.topCell.setAttribute("class","ROWHEAD");
  this.topCell.setAttribute("className","ROWHEAD");
  this.topCell.appendChild(document.createTextNode(this.name+": "));
  this.tagList=document.createElement("span");
  this.topCell.appendChild(this.tagList);
    
  this.bottomRow=this.table.insertRow(1);
    
  this.bottomCell=this.bottomRow.insertCell(0);
  this.bottomCell.align="center";
  this.selected="";
  this.unselected="";
}

bobovis.TagCloud.TagLink = function(text,weight){		
	this.node=document.createElement("a");
	this.node.setAttribute('class','tag'+weight);
	this.node.setAttribute('className','tag'+weight);
	this.node.innerHTML=text;
	
	
	this.node.name=text;
	
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

bobovis.TagCloud.tagSort = function(tag1,tag2){
	s1=tag1.val.toLowerCase();
	s2=tag2.val.toLowerCase();
	val = bobovis.stringCompare(s1,s2);
	return val;
}

bobovis.TagCloud.prototype.handleRemoveTag = function(linkNode){
    var tagName=linkNode.name;
    this.unselected=tagName;
	google.visualization.events.trigger(this,'unselect', {});
}

bobovis.TagCloud.prototype.callback = function(linkNode){
    var tagName=linkNode.name;
    this.selected=tagName;
	google.visualization.events.trigger(this,'select', {});
}


bobovis.TagCloud.prototype.draw = function(data,options){
  // clear old data
  while (this.bottomCell.childNodes[0]) {
    this.bottomCell.removeChild(this.bottomCell.childNodes[0]);
  }
  // update selected tags
  while (this.tagList.childNodes[0]) {
    this.tagList.removeChild(this.tagList.childNodes[0]);
  }
  
  var numSelected = 0;
  var flag = false;
  var largest=0;
  var smallest=0;
  var src = this;
  var tagList=new Array();
  for (var row = 0; row < data.getNumberOfRows(); row++) {
	var val = bobovis.escapeHtml(data.getFormattedValue(row, 0));
	var hits = bobovis.escapeHtml(data.getFormattedValue(row, 1));
	var isSelected = data.getFormattedValue(row,2) === 'true';
	
	if (isSelected){
	  var link=new bobovis.Link(val,val);
	  link.node.onclick=function(){src.handleRemoveTag(this)};
	  if (numSelected!=0){
		this.tagList.appendChild(document.createTextNode(", "));
	  }
	  this.tagList.appendChild(link.node);
	  numSelected++;
	}
	else{
	  if (!flag){
	    largest=hits;
		smallest=hits;
		flag=true;
	  }
	  else{
	    if (hits>largest) largest=hits;
	    if (hits<smallest) smallest=hits;
	  }
	  var node=new Object();
	  node.val = val;
	  node.hits = hits;
	  tagList.push(node);
	}
  }
  
  if (tagList.length>0){
    var n = largest-smallest;
    var x;
    for (x in tagList){
	  var node = tagList[x];
	  var rank;
	  if (n>0){
	    rank=parseInt(9*node.hits/n+1);
	    if (rank>10) rank=10;
	    if (rank<1) rank=1;
	  }
	  else{
	    rank=10;
	  }
	  var link=new bobovis.TagCloud.TagLink(node.val,rank,src.callback);
	  link.node.onclick = function(){src.callback(this)};
	  this.bottomCell.appendChild(link.node);
	  this.bottomCell.appendChild(document.createTextNode(" "));
    }
  }
}


