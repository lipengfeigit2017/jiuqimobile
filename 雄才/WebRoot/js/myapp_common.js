/**
 * Created by handengke on 2015/9/22.
 */
var ipandport = "";
function getParam(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '='
            + '([^&;]+?)(&|#|;|$)').exec(location.search) || [ , "" ])[1]
            .replace(/\+/g, '%20'))
        || null;
}

function GetQueryString(name){
    var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if(r!=null)return  unescape(r[2]); return null;
}

function getMobileType() {
    var sUserAgent = navigator.userAgent.toLowerCase();
    //alert(sUserAgent);
    var bIsIpad = sUserAgent.match(/ipad/i) == "ipad";
    var bIsIphoneOs = sUserAgent.match(/iphone os/i) == "iphone os";
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
    var sessionExpire = sessionStorage.getItem("user");
    if(sessionExpire == null) {
        if(getMobileType() == "pad") {
            window.parent.location = "logout.html";
        } else {
            window.location = "logout.html";
        }

    }
    $.ajax ({
        type: "GET",
        dataType: "json",
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        url: ipandport+"/mapp/flow_login_app?action=checksession",
        data: "",
        success: function (jsonobject) {
            if(jsonobject.msg == 0) {
                if (getMobileType() == "pad") {
                    window.parent.location = "logout.html";
                }
                else {
                    window.location = "logout.html";
                }
                return;
            }
        }
    });
    /*
    if(getMobileType() == "phone") {
        if(sessionStorage.getItem("sso") == null) {
            $(".page").prepend("<div class='welcome'>当前用户："+sessionStorage.getItem("user")+"&nbsp;&nbsp;<span id='logout'>注销</span></div>");
            $(".welcome").css({"position":"fixed","z-index":"999","top":"0px"});
        } else {

        }
        $("#logout").click(function () {
            logout("注销成功！");
        });
    }
    */
    if(getMobileType() == "pad") {
        $(".header_menu").css({"position":"fixed","z-index":"999","top":"0px"});
        $(".footer").css({"position":"fixed","bottom":"0"});
        $(".page").css({"overflow":"auto"});
    } else if(getMobileType() == "phone") {
        if(sessionStorage.getItem("sso") == null) {
            $(".header_menu").css({"position":"fixed","z-index":"999","top":"50px"});
            $("#content").css({"margin-top":"75px"});
            $(".content").css({"margin-top":"75px"});
            $("#searchdiv").css({"position":"fixed","top":"91px"});
        } else {
            $(".header_menu").css({"position":"fixed","z-index":"999","top":"0px"});
            $("#content").css({"margin-top":"40px"});
            $(".content").css({"margin-top":"40px"});
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
                url : ipandport+"/mapp/flow_login_app?action=logout",
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
