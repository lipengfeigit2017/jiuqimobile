var isSwipeX = null;
$(function() {
    $.fn.swipe = function(options) {
        var _data = this.data("data");
        if (_data != null) {
            if (_data.index != options.index) {
                this.css({
                    "-webkit-transform": "translate3d(" + options.index * -1 * _data.width + "rem,0,0)"
                })
            }
            this.data("data", $.extend({}, _data, options));
            _data = this.data("data");
            return
        }
        var defaults = {
            index: 0,
            isRem: false
        };
        var opts = $.extend({}, defaults, options);
        this.data("data", opts);
        this.each(function() {
            var that = $(this);
            var _this = $(this)[0];
            var _width = opts.width || parseInt($(this).children().css("width"));
            var sx = 0,
                sy = 0,
                mx = 0,
                my = 0,
                fontNum = 1,
                cRem = "px";
            if (opts.isRem) {
                cRem = "rem";
                fontNum = parseInt($("html").css("font-size"))
            }
            that.css({
                "-webkit-transition-duration": "300ms",
                "-webkit-transition-style": "preserve-3d",
                "-webkit-transition-timing-function": "linear",
                "-webkit-transform": "translate3d(" + opts.index * -1 * opts.width + cRem + ",0,0)"
            });
            _this.addEventListener("touchstart", function(e) {
                sx = e.touches[0].pageX, sy = e.touches[0].pageY, mx = 0, my = 0;
                that.css({
                    "-webkit-transition-duration": "0ms"
                })
            }, false);
            _this.addEventListener("touchmove", function(e) {
                mx = (e.touches[0].pageX - sx) / fontNum, my = (e.touches[0].pageY - sy) / fontNum;
                if (isSwipeX == null) {
                    isSwipeX = Math.abs(mx) > Math.abs(my)
                }
                if (isSwipeX) {
                    e.preventDefault();
                    that.css({
                        "-webkit-transform": "translate3d(" + (opts.index * -1 * opts.width + mx) + cRem + ",0,0)"
                    })
                }
            }, false);
            _this.addEventListener("touchend", function(e) {
                if (isSwipeX) {
                    var num = 45 / fontNum;
                    if (Math.abs(mx) > num) {
                        var _n = that.children(opts.childrenClass).length;
                        if (mx < 0) {
                            if (opts.index < _n - 1) opts.index++
                        } else {
                            if (opts.index > 0) opts.index--
                        }
                    }
                    that.attr("data-indexNum", opts.index);
                    that.css({
                        "-webkit-transition-duration": "200ms",
                        "-webkit-transform": "translate3d(" + opts.index * -1 * opts.width + cRem + ",0,0)"
                    });
                    if (opts.afterSwipe) {
                        opts.afterSwipe(opts.index)
                    }
                }
                isSwipeX = null
            }, false)
        });
        return this
    };
    $.fn.swipeUp = function(options) {
        var _data = this.data("data");
        if (_data != null) {
            if (_data.index != options.index) {
                var _c = this.children(_data.childrenClass);
                _c.css({
                    zIndex: "18",
                    display: "none"
                });
                $(_c.get(options.index)).css({
                    zIndex: "20",
                    display: "block"
                })
            }
            this.data("data", $.extend({}, _data, options));
            _data = this.data("data");
            return
        }
        var defaults = {
            index: 0,
            speed: 300,
            childrenClass: ".house_section"
        };
        var opts = $.extend({}, defaults, options);
        this.data("data", opts);
        return this.each(function() {
            var that = $(this);
            var w = parseInt(that.css("width"));
            var h = parseInt(that.css("height"));
            var o = 0,
                c = "white";
            var childrenObj = that.children(opts.childrenClass);
            if (opts.index >= childrenObj.length) {
                alert("index value is too big.")
            }
            var currObj, prevObj, nextObj;
            var freshObj = function() {
                childrenObj.css({
                    "-webkit-transform": "translate3d(0,0,0)",
                    "-webkit-transition-duration": "0ms",
                    opacity: 1,
                    display: "none"
                });
                currObj = prevObj = nextObj = null;
                if (opts.index < 0) {
                    currObj = $(childrenObj[childrenObj.length - 1]).show().css({
                        zIndex: "20"
                    });
                    opts.index = childrenObj.length - 1
                } else {
                    if (opts.index == childrenObj.length) {
                        currObj = $(childrenObj[0]).show().css({
                            zIndex: "20"
                        });
                        opts.index = 0
                    } else {
                        currObj = $(childrenObj[opts.index]).show().css({
                            zIndex: "20"
                        })
                    }
                }
                if (opts.index > 0) {
                    prevObj = $(childrenObj[opts.index - 1])
                } else {
                    prevObj = $(childrenObj[childrenObj.length - 1])
                }
                if (opts.index < childrenObj.length - 1) {
                    nextObj = $(childrenObj[opts.index + 1])
                } else {
                    if (!currObj.hasClass("isshare_wrap")) {
                        nextObj = $(childrenObj[0])
                    }
                }
            };
            freshObj();
            var startY = 0,
                moveY = 0,
                endY = 0;
            swipeUpFlag = true;
            this.addEventListener("touchstart", function(e) {
                if (!swipeUpFlag) return;
                opts = that.data("data");
                freshObj();
                if (opts.beforeSwipe) {
                    opts.beforeSwipe(opts.index)
                }
                startY = e.touches[0].pageY, endY = 0;
                c = currObj.css("backgroundColor");
                childrenObj.css({
                    "-webkit-transition-duration": "0ms"
                })
            });
            this.addEventListener("touchmove", function(e) {
                if (!swipeUpFlag || isSwipeX != null && isSwipeX != false) return;
                e.preventDefault();
                moveY = e.touches[0].pageY;
                endY = moveY - startY;
                o = Math.abs(endY) / h;
                var e = false;
                if (prevObj) {
                    if (endY > 0) {
                        prevObj.css({
                            zIndex: 18,
                            display: "block",
                            opacity: .5 + o
                        })
                    } else {
                        prevObj.hide()
                    }
                } else if (endY > 0) {
                    e = true
                }
                if (nextObj) {
                    if (endY < 0) {
                        nextObj.css({
                            zIndex: 18,
                            display: "block",
                            opacity: .5 + o
                        })
                    } else {
                        nextObj.hide()
                    }
                } else if (endY < 0) {
                    e = true
                }
                if (e) {
                    o = 0;
                    endY = endY / 2;
                    $("body").css("backgroundColor", c)
                } else {
                    $("body").css("backgroundColor", "white")
                }
                currObj.css({
                    "-webkit-transform": "translate3d(0," + endY + "px,0)",
                    opacity: 1 - o / 2
                })
            });
            this.addEventListener("touchend", function(e) {
                if (!swipeUpFlag || isSwipeX != null && isSwipeX != false) return;
                if (Math.abs(endY) > 0) {
                    childrenObj.css({
                        "-webkit-transition-duration": opts.speed + "ms"
                    });
                    if (Math.abs(endY) < h / 8) {
                        currObj.css({
                            "-webkit-transform": "translate3d(0,0,0)",
                            opacity: 1
                        });
                        if (nextObj) {
                            nextObj.css({
                                display: "none",
                                opacity: 1
                            })
                        }
                    } else {
                        e.preventDefault();
                        var _my = 0;
                        if (endY < 0 && nextObj) {
                            nextObj.css({
                                opacity: 1
                            });
                            opts.index++;
                            _my = -1 * h
                        } else if (endY > 0 && prevObj) {
                            prevObj.css({
                                opacity: 1
                            });
                            opts.index--;
                            _my = h
                        } else {
                            _my = 0
                        }
                        endY = 0;
                        swipeUpFlag = false;
                        currObj.css({
                            "-webkit-transform": "translate3d(0," + _my + "px,0)",
                            opacity: _my == 0 ? 1 : 0,
                            zIndex: 20
                        });
                        setTimeout(function() {
                            freshObj();
                            swipeUpFlag = true;
                            if (opts.afterSwipe) {
                                opts.afterSwipe(opts.index)
                            }
                        }, opts.speed)
                    }
                }
                that.attr("data-indexNum", opts.index)
            });
            if (opts.init) {
                opts.init()
            }
        });
        return this
    }
})