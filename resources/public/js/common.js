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

const getPosition = el => {
  const position = getCoords(el);
  return position.left + ', ' + position.top;
};
const setPosition = el => el.children[0].value = getPosition(el);

const dropRow = (row) => (e) => {
  const dropped = e.dragEvent.target;
  const form = document.querySelector('.board' + row);
  form.appendChild(dropped);
  form.querySelectorAll('.tile').forEach(setPosition);
  form.children[0].click();
};

const dropBody = (e) => {
  const dropped = e.dragEvent.target;
  setPosition(dropped);
  dropped.children[1].click();
  const position = getCoords(dropped);
  const style = dropped.style;
  style.transform = '';
  style.position = 'absolute';
  style.left = position.left + 'px';
  style.top = position.top + 'px';
  document.querySelector('#table').appendChild(dropped);
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

  document.addEventListener("keydown", e => {
    if (e.key === 'p') {
      document.querySelector('#pickup').click();
    }
  });
};
main();

function playSound(url) {
    var a = new Audio(url);
    a.play();
}

const pass = () => {
  const i = Math.max(0, Math.floor(Math.random() * 100) - 84);
  playSound(`/pass${i}.mp4`);
};

const rummikub = user => {
  const i = Math.floor(Math.random() * 7);
  playSound(`/win${i}.mp4`);
  alert(`${user} says: Rummikub!`);
};
