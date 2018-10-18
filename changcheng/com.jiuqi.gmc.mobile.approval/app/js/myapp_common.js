/**
 * Created by handengke on 2015/9/22.
 */
var ipandport = "http://10.2.6.16:9996";//自己的服务
//var ipandport = "http://10.2.27.36:8899";//二十冶本地测试
//var ipandport = "http://jq.sh13mcc.cn:8014";//二十冶现场测试
//var ipandport = "http://jq.sh20mcc.cn:9000";//二十冶生产
//var ipandport = "http://10.2.6.72:9797";
//var ipandport = "http://10.1.65.116:9909";//现场
//var ipandport = "http://10.2.12.58:9909";//测试
var bIsIphoneOs = false;
function getParam(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '='
            + '([^&;]+?)(&|#|;|$)').exec(location.search) || [ , "" ])[1]
            .replace(/\+/g, '%20'))
        || null;
}

function getMobileType() {

    var sUserAgent = navigator.userAgent.toLowerCase();
    //alert(sUserAgent);
    var bIsIpad = sUserAgent.match(/ipad/i) == "ipad";
    bIsIphoneOs = sUserAgent.match(/iphone os/i) == "iphone os";
    var bIsMidp = sUserAgent.match(/midp/i) == "midp";
    var bIsUc7 = sUserAgent.match(/rv:1.2.3.4/i) == "rv:1.2.3.4";
    var bIsUc = sUserAgent.match(/ucweb/i) == "ucweb";
    var bIsAndroid = sUserAgent.match(/android/i) == "android";
    var bIsCE = sUserAgent.match(/windows ce/i) == "windows ce";
    var bIsWM = sUserAgent.match(/windows mobile/i) == "windows mobile";
    /*pad*/
    if(bIsIpad){
        return "pad";
    } else if(bIsIphoneOs || bIsAndroid){
        return "phone";
    } else {
        return "pc";
    }
}

function initLayOut() {
    if(getMobileType() == "phone") {
        if(sessionStorage.getItem("sso") == null) {
            $(".page").prepend("<div class='welcome'>当前用户："+sessionStorage.getItem("user")+"&nbsp;&nbsp;<span id='logout'>注销</span></div>");
            $(".welcome").css({"position":"fixed","z-index":"999","top":"10px"});
        } else {

        }
        $("#logout").click(function () {
            logout("注销成功！");
        });
    }
    if(getMobileType() == "pad") {
        $(".header_menu").css({"position":"fixed","z-index":"999","top":"0px"});
        $(".footer").css({"position":"fixed","bottom":"0"});
        $(".page").css({"overflow":"auto"});
    } else if(getMobileType() == "phone") {
        if(sessionStorage.getItem("sso") == null) {
            $(".header_menu").css({"position":"fixed","z-index":"999","top":"50px"});
            $("#content").css({"margin-top":"90px"});
            $(".content").css({"margin-top":"90px"});
            $("#searchdiv").css({"position":"fixed","top":"91px"});
        } else {
            $(".header_menu").css({"position":"fixed","z-index":"999","top":"0px"});
            if(bIsIphoneOs) {
            	$("#content").css({"margin-top":"55px"});
            	$(".content").css({"margin-top":"55px"});
            	$(".submenu").css("top","55px");
            	$(".header_menu").css({"width": "100%","height": "55px","background": "#3199E8",
"border-right": "2px solid gainsboro", "line-height": "55px","border-bottom": "1px solid gainsboro","border-top": "1px solid gainsboro"});
            } else {
            	$("#content").css({"margin-top":"40px"});
            	$(".content").css({"margin-top":"40px"});
            	$(".submenu").css("top","40px");
            	$(".header_menu").css({"width": "100%","height": "40px","background": "#3199E8",
"border-right": "2px solid gainsboro", "line-height": "40px","border-bottom": "1px solid gainsboro","border-top": "1px solid gainsboro"});
            }
            $("#searchdiv").css({"position":"fixed","top":"41px"});
        }
        $(".footer").css({"display":"none"});
        $(".page").css({"margin-top":"0px"});
        //$(".welcome").css({"position":"fixed","z-index":"999","top":"0px"});
        //$(".header_menu").css({"position":"fixed","z-index":"999","top":"30px"});
        //$("#content").css({"margin-top":"70px"});
    }
}

function logout(title) {
    swal({
            title: "",
            text: "确定要执行注销操作吗？",
            type: "warning",
            showCancelButton: true,
            cancelButtonText: "取消",
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "确定",
            closeOnConfirm: false
        },
        function(){
            $.ajax({
                type : "GET",
                dataType : "json",
                contentType: "application/x-www-form-urlencoded; charset=UTF-8",
                url : ipandport+"/flow_login_app?action=logout",
                data : "",
                success : function(msg) {
                    swal({
                        title : title,
                        type : "success"},function() {
                        window.sessionStorage.clear();
                        window.location = "login.html";
                    });

                }
            });
        }
    );

}
