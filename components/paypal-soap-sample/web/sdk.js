function toggleDisplay(id) {
    var elem = document.getElementById(id);
    var className = elem.className;
    if(className == 'open') {
        elem.className = 'closed';
    } else {
        elem.className = 'open';
    }
}
