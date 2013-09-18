// The amount of circles we want to make:
var count = 100;

var content = '404 infected - ';
content += $('#error-message').html();

var text = new PointText({
    point: view.center,
    justification: 'center',
    content: content,
    fontSize: 80,
    font: 'Source Sans Pro',
    fillColor: '#2980b9',
    opacity: 0.7
});


var center = new Point(0,0);
var points = 8;
var radius1 = 15;
var radius2 = 25;
var shape = new Path.Star(center, points, radius1, radius2);
shape.fillColor = '#2980b9';
shape.opacity = 0.5;

var symbol = new Symbol(shape);

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
        //Moves everything but the text
        if(item.content == null){
           item.position.x += item.bounds.width / 40;
        }
        
        // If the item has left the view on the right, move it back
        // to the left:
        if (item.bounds.left > view.size.width) {
            item.position.x = -item.bounds.width;
        }
    }
    // Each frame, rotate the path by 3 degrees:
	 shape.fillColor.hue += 1;
	 text.fillColor.hue += 1;
	 shape.rotate(-1);
}