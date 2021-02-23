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
  autoScroll: true,
  listeners: {
    move: dragMoveListener,
  }
});

const setPosition = el => {
  const position = el.getBoundingClientRect();
  el.children[0].value = position.x + ', ' + position.y;
}

const dropRow = (row) => (e) => {
  const dropped = e.dragEvent.target;
  document.querySelector('#drop-row').value = row;
  document.querySelector('#drop-tile').value = dropped.id;
  document.querySelectorAll('.play-area .tile').forEach(setPosition);
  document.querySelector('#play-area-submit').click();
};

interact('#board0').dropzone({
  ondrop: dropRow(0)
});

interact('#board1').dropzone({
  ondrop: dropRow(1)
});

interact('#table').dropzone({
  ondrop: dropRow(2)
});
