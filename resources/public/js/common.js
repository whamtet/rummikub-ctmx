htmx.config.defaultSwapStyle = 'outerHTML';
htmx.config.defaultSettleDelay = 0;

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

const setPosition = el => {
  const position = el.getBoundingClientRect();
  el.children[0].value = position.x + ', ' + position.y;
}

interact('#table').dropzone({
  ondrop: function() {
    console.log('drop table');
  }
});

const dropRow = (row) => (e) => {
  const dropped = e.dragEvent.target;
  document.querySelector('#drop-row').value = row;
  document.querySelector('#drop-tile').value = dropped.id;
  document.querySelectorAll('.board .tile').forEach(setPosition);
  document.querySelector('#board-submit').click();
};

interact('#board0').dropzone({
  ondrop: dropRow(0)
});

interact('#board1').dropzone({
  ondrop: dropRow(1)
});
