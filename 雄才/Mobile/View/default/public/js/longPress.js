$.fn.longPress = function(fn) {
    var timeout = undefined;
    var $this = this;
    $this[0].addEventListener('touchstart', function(event) {
        timeout = setTimeout(fn, 800);  //长按时间超过800ms，则执行传入的方法
    }, false);
    $this[0].addEventListener('touchend', function(event) {
        clearTimeout(timeout);  //长按时间少于800ms，不会执行传入的方法
    }, false);
}