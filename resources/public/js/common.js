htmx.config.defaultSwapStyle = 'outerHTML';
htmx.config.defaultSettleDelay = 0;

import interact from 'https://cdn.interactjs.io/v1.10.3/interactjs/index.js';

function dragMoveListener (event) {
  var target = event.target
  // keep the dragged position in the data-x/data-y attributes
  var x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx
  var y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy

  // translate the element
  target.style.webkitTransform =
    target.style.transform =
    'translate(' + x + 'px, ' + y + 'px)'

  // update the posiion attributes
  target.setAttribute('data-x', x)
  target.setAttribute('data-y', y)
}

// interact('#table').dropzone();
interact('.tile').draggable({
  // enable autoScroll
  autoScroll: true,

  listeners: {
    // call this function on every dragmove event
    move: dragMoveListener,
  }
});

interact('#table').dropzone({
  ondrop: function() {
    console.log('drop table');
  }
});

interact('#board').dropzone({
  ondrop: function() {
    console.log('drop board');
  }
});
