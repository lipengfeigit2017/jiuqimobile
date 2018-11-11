/**
 * @author feiwen
 */
(function($){
	$.fn.textSlider = function(settings){    
            settings = Zepto.extend({
                speed : "normal",
                line : 2,
                timer : 3000
            }, settings);
            return this.each(function() {
                $.fn.textSlider.scllor( $( this ), settings );
            });
        };
	$.fn.textSlider.scllor = function($this, settings){
            var ul = $this;
            var timerID;
            var li = ul.children();
            var liHight=li.eq(0).height();
            var upHeight=0-settings.line*liHight;//滚动的高度；
            var scrollUp=function(){
                ul.animate({marginTop:upHeight},settings.speed,function(){
                    for(i=0;i<settings.line;i++){
                    	ul.find("li").slice(0, 1).appendTo(ul);
                    }
                    ul.css({"margin-top":0});
                });
            };
           function autoPlay(){
                timerID = window.setInterval(scrollUp,settings.timer);
            };
            //事件绑定
            autoPlay();
	};
})(Zepto);
