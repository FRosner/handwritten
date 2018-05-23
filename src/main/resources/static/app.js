window.onload = function() {
    var canvasWidth = 280
    var canvasHeight = 280

    var canvasDiv = document.getElementById('canvasDiv');
    canvas = document.createElement('canvas');
    canvas.setAttribute('width', canvasWidth);
    canvas.setAttribute('height', canvasHeight);
    canvas.setAttribute('id', 'canvas');
    canvasDiv.appendChild(canvas);
    if(typeof G_vmlCanvasManager != 'undefined') {
        canvas = G_vmlCanvasManager.initElement(canvas);
    }
    context = canvas.getContext("2d");

    $('#canvas').mousedown(function(e) {
      var mouseX = e.pageX - this.offsetLeft;
      var mouseY = e.pageY - this.offsetTop;

      paint = true;
      addClick(e.pageX - this.offsetLeft, e.pageY - this.offsetTop);
      redraw();
    });

    $('#canvas').mousemove(function(e) {
      if(paint){
        addClick(e.pageX - this.offsetLeft, e.pageY - this.offsetTop, true);
        redraw();
      }
    });

    $('#canvas').mouseup(function(e) {
      paint = false;
    });

    $('#canvas').mouseleave(function(e) {
      paint = false;
    });

    var clickX = new Array();
    var clickY = new Array();
    var clickDrag = new Array();
    var paint;

    $('#clearCanvasButton').mousedown(function(e) {
      clickX = new Array();
      clickY = new Array();
      clickDrag = new Array();
      clearCanvas();
      $('#predictionResult').text("")
    });

    $('#predictButton').mousedown(function(e) {
      canvas.toBlob(function(d) {
      var fd = new FormData();
      fd.append('image', d)
        $.ajax({
          type: "POST",
          url: "predict",
          data: fd,
          contentType: false,
          processData: false
        }).done(function(o) {
          $('#predictionResult').text(o)
        });
      });

    });

    function addClick(x, y, dragging) {
      clickX.push(x);
      clickY.push(y);
      clickDrag.push(dragging);
    }

    function clearCanvas() {
      context.beginPath();
      context.rect(0, 0, context.canvas.width, context.canvas.height);
      context.fillStyle = "white";
      context.fill();
    }

    function redraw() {
      clearCanvas();

      context.strokeStyle = "#000";
      context.lineJoin = "round";
      context.lineWidth = 20;

      for(var i=0; i < clickX.length; i++) {
        context.beginPath();
        if(clickDrag[i] && i){
          context.moveTo(clickX[i-1], clickY[i-1]);
         }else{
           context.moveTo(clickX[i]-1, clickY[i]);
         }
         context.lineTo(clickX[i], clickY[i]);
         context.closePath();
         context.stroke();
      }
    }
}