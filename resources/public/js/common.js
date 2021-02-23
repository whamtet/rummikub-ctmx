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

function getCoords(elem) { // crossbrowser version
  var box = elem.getBoundingClientRect();

  var body = document.body;
  var docEl = document.documentElement;

  var scrollTop = window.pageYOffset || docEl.scrollTop || body.scrollTop;
  var scrollLeft = window.pageXOffset || docEl.scrollLeft || body.scrollLeft;

  var clientTop = docEl.clientTop || body.clientTop || 0;
  var clientLeft = docEl.clientLeft || body.clientLeft || 0;

  var top  = box.top +  scrollTop - clientTop;
  var left = box.left + scrollLeft - clientLeft;

  return { top: Math.round(top), left: Math.round(left) };
}

const setPosition = el => {
  const position = getCoords(el);
  el.children[0].value = position.left + ', ' + position.top;
}

const submit = () => document.querySelector('#play-area-submit').click();

const dropRow = (row) => (e) => {
  const dropped = e.dragEvent.target;
  document.querySelector('#drop-row').value = row;
  document.querySelector('#drop-tile').value = dropped.id;
  document.querySelectorAll('.play-area .tile').forEach(setPosition);
  submit();
};

const main = () => {
  interact('.tile').draggable({
    autoScroll: true,
    listeners: {
      move: dragMoveListener,
    }
  });

  interact(document.body).dropzone({
    ondrop: dropRow(2)
  });

  interact('#board0').dropzone({
    ondrop: dropRow(0)
  });

  interact('#board1').dropzone({
    ondrop: dropRow(1)
  });
};
main();
