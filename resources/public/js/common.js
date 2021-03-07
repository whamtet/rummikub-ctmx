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

  return { y: Math.round(top), x: Math.round(left) };
}

const getPosition = el => {
  const position = getCoords(el);
  return position.x + ', ' + position.y;
};
const setPosition = el => el.children[0].value = getPosition(el);

const dropRow = (row) => (e) => {
  const dropped = e.dragEvent.target;
  const tile = dropped.children[1].value;

  const form = document.querySelector('.board' + row);
  form.appendChild(dropped.cloneNode(true));

  if (dropped.id === tile) {
    dropped.style.display = 'none';
  } else {
    dropped.remove();
  }

  form.querySelectorAll('.tile').forEach(setPosition);
  form.children[0].click();
};

const dropBody = (e) => {
  const dropped = e.dragEvent.target;
  const tile = dropped.children[1].value;

  // first submit update
  const position = getCoords(dropped);
  position.tile = tile;
  const button = document.querySelector('.table-update');
  button.setAttribute('hx-vals', JSON.stringify(position));
  button.click();

  if (dropped.id !== tile) {
    const bodyTile = document.getElementById(tile);
    const {style} = bodyTile;
    style.display = 'inline-block';
    style.left = position.x + 'px';
    style.top = position.y + 'px';
    style.position = 'absolute';

    // and remove original
    dropped.remove();
  }
};

const main = () => {
  interact('.tile').draggable({
    autoScroll: true,
    listeners: {
      move: dragMoveListener,
    }
  });

  interact(document.body).dropzone({
    ondrop: dropBody
  });

  interact('.board0').dropzone({
    ondrop: dropRow(0)
  });

  interact('.board1').dropzone({
    ondrop: dropRow(1)
  });

  const keyBindings = {
    p: '#pickup',
    s: '#sort',
    Enter: '#pass'
  };

  document.addEventListener("keydown", e => {
    const id = keyBindings[e.key];
    if (id && document.querySelector(id)) {
      document.querySelector(id).click();
    }
  });
};
main();

function playSound(url) {
  var a = new Audio(url);
  a.play();
}

const pass = (i) => {
  playSound(`/pass${i}.mp4`);
};

const rummikub = (user, i) => {
  playSound(`/win${i}.mp4`);
  alert(`${user} says: Rummikub!`);
};
