var popWin = {
	param: {
		title: "",
		html: "",
		handle: "",
		from: "bottom",
	},
	mod: document.createElement("modernizr").style,
	_testPrefix: function() {
		var a = ["Webkit", "Moz", "O", "ms"],
			c, b = this;
		for (c in a) {
			if (this._testProps([a[c] + "Transform"])) {
				return "-" + a[c].toLowerCase() + "-"
			}
		}
		return ""
	},
	_testProps: function(b) {
		var a;
		for (a in b) {
			if (this.mod[b[a]] !== undefined) {
				return true
			}
		}
		return false
	},
	init: function(e) {
		var b = this._testPrefix(),
			g = b.replace(/^\-/, "").replace(/\-$/, "").replace("moz", "Moz"),
			d = this,
			f = "",
			a = "",
			e = $.extend({}, this.param, e);
		switch (e.from) {
			case "top":
				f = "translateY(-100%)";
				a = "translateY(0)";
				break;
			case "left":
				f = "translateX(-100%)";
				a = "translateX(0)";
				break;
			case "right":
				f = "translateX(100%)";
				a = "translateX(0)";
				break;
			default:
				f = "translateY(100%)";
				a = "translateY(0)";
				break
		}
		if ($("#popWin").length) {
			var c = '<div id="popWinSub" style="' + b + "transform: " + f + ';"><div class="popheader"><ul class="clear"><li class="popSubBack"><i class="l_arow"></i></li><li class="poptitle"><p>' + e.title + '</p></li></ul></div><div class="popBody">' + e.html + "</div></div>";
			$("body").append(c);
			d._lock();
			setTimeout(function() {
				$("#popWinSub").attr("style", b + "transform: " + a);
				if (e.handle && typeof e.handle == "function") {
					e.handle(d)
				}
				$(".popSubBack").on("click", function() {
					d.close("sub", e.from)
				});
				$(".header").css("z-index", 7)
			}, 100)
		} else {
			var c = '<div id="popWin" style="' + b + "transform: " + f + ';"><div class="popheader"><ul class="clear"><li class="popBack"><i class="l_arow"></i></li><li class="poptitle"><p>' + e.title + '</p></li></ul></div><div class="popBody">' + e.html + "</div></div>";
			$("body").append(c);
			d._lock();
			setTimeout(function() {
				$("#popWin").attr("style", b + "transform: " + a);
				if (e.handle && typeof e.handle == "function") {
					e.handle(d)
				}
				$(".popBack").on("click", function() {
					d.close()
				});
				$(".header").css("z-index", 7)
			}, 100)
		}
		$('.js-back').on('click', function () {
			//history.back();
			d.close()
		});
	},
	_lock: function(a) {
		switch (a) {
			case "off":
				$(".popLock").remove();
				break;
			default:
				$("body").append('<div class="popLock"></div>');
				break
		}
	},
	close: function(c, d) {
		if (c == "sub") {
			var a = this._testPrefix(),
				b = "";
			switch (d) {
				case "top":
					b = "translateY(-100%)";
					break;
				case "left":
					b = "translateX(-100%)";
					break;
				case "right":
					b = "translateX(100%)";
					break;
				default:
					b = "translateY(100%)";
					break
			}
			$("#popWinSub").attr("style", a + "transform: " + b);
			this._lock("off");
			setTimeout(function() {
				$("#popWinSub").remove()
			}, 300)
		} else {
			$("#popWin").hide();
			this._lock("off");
			setTimeout(function() {
				$(".header").css("z-index", 999);
				$("#popWin").remove()
			}, 300)
		}
	}
};
$(window).on('hashchange', function () {
    if (!(location.hash.indexOf('#') === 0)) {
    	$('#popWin').remove();
    	$('.popLock').remove();
    }
});