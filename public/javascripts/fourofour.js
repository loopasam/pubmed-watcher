// The amount of circles we want to make:
var count = 100;

var content = 'Not found :-( ';
content += $('#error-message').html();

var text = new PointText({
    point: view.center,
    justification: 'center',
    content: content
});

text.characterStyle = {
        fontSize: 60,
        font: 'Roboto',
        fillColor: 'black'
};


// Create a symbol, which we will use to place instances of later:
var circle = new Path.Circle({
    center: [0, 0],
    radius: 20,
    fillColor: '#2980b9',
    opacity: 0.5
});

var symbol = new Symbol(circle);

//Place the instances of the symbol:
for (var i = 0; i < count; i++) {
 // The center position is a random point in the view:
 var center = Point.random() * view.size;
 var placedSymbol = symbol.place(center);
 placedSymbol.scale(i / count);
}


function onFrame(event) {
    // Run through the active layer's children list and change
    // the position of the placed symbols:
    for (var i = 0; i < count; i++) {
        var item = project.activeLayer.children[i];
        // Move the item 1/20th of its width to the right. This way
        // larger circles move faster than smaller circles:
        if(item.bounds.width > 20){
        	item.position.x += item.bounds.width / 200;
        }else{
           item.position.x += item.bounds.width / 20;
        }
        
        // If the item has left the view on the right, move it back
        // to the left:
        if (item.bounds.left > view.size.width) {
            item.position.x = -item.bounds.width;
        }
    }
    // Each frame, rotate the path by 3 degrees:
	 circle.fillColor.hue += 1;
}