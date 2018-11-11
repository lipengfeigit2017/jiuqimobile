(function($){
	/* 投递简历 电话联系 对应相应的职位 */
    function load_jobs(index,utype)
    {
        var _job = $($(".posit_details").get(index));
        if(utype=="2")
        {
        	$(".job_btn_list > .add").attr("jobs_id", _job.attr("jobs_id"));
	        $(".job_btn_list > .add").attr("jobs_name", _job.attr("jobs_name"));
	        $(".job_btn_list > .add").attr("company_id", _job.attr("company_id"));
	        $(".job_btn_list > .add").attr("company_name", _job.attr("company_name"));
	        $(".job_btn_list > .add").attr("company_uid", _job.attr("company_uid"));
        }
        else
        {
        	$(".job_btn_list > .add").attr("href",$('#cmsRoot').val()+'?m=Mobile&c=Members&a=login');
        }
        
        var isTel = $("#jobs_phone_code").attr('code');
        if (isTel > 0) {
          $(".job_btn_list > .tel").attr("pho", "tel:"+_job.attr("jobs_tel"));
        };
    }
	/* 滑动 效果 */
	function swipe_self()
	{
        var winWidth = window.innerWidth;
        var winHeight = window.innerHeight;
        var screenFix = function(){
          $("#poster_contain").css({
            width:winWidth+"px",
            height:winHeight+"px"
          });
        };
        screenFix();
        var orientationEvent = ('onorientationchange' in window) ? 'orientationchange' : 'resize';
        window.addEventListener(orientationEvent, function() {
          window.setTimeout(function(){
            screenFix();
          }, 600);
        }, false);

        $(".layer, .wx_layer").on("click", function(){
          $(this).hide();
        });
        var _indexSwipeUp=0;
        var _indexSwipe=0;
        // 滑动
        $("#poster_contain").swipeUp({
          index:_indexSwipeUp,
          childrenClass:".poster_wrap",
          init:function()
          {
            $(".arrow_con").addClass("show");
            setTimeout(function(){
              $($("#poster_contain .poster_wrap").get(_indexSwipeUp)).addClass("focus");
            }, 300);
          },
          afterSwipe:function(index)
          {
            var _pw = $("#poster_contain .poster_wrap");
            _pw.removeClass("focus");
            $(_pw.get(index)).addClass("focus");
            if(index == _pw.length-1){
            $(".arrow_con").removeClass("show");
            }else{
            $(".arrow_con").addClass("show");
            }

            var rewardHomeCon=$('.reward_home_con'),
            rewardHomeConLength=rewardHomeCon.length; 
            if(rewardHomeConLength>0){
              if(rewardHomeCon.hasClass("homt_active")){
                rewardHomeCon.removeClass("homt_active");
              }
              var rewardPlus=$('.reward_plus');
              if(rewardPlus.hasClass("plus_animate")){
                rewardPlus.removeClass("plus_animate");
              }
            }

            if(index == 2){
              welfInterval = setInterval(function(){
              var _s = parseInt(Math.random()*6);
              $(".welf_bg > div").css({"-webkit-animation":"none"});
              $($(".welf_bg > div").get(_s)).css({"-webkit-animation":"fuli"+(_s%2)+" 1s ease-out"});
              }, 1000);
            }else{
              if(typeof(welfInterval) != "undefined"){
              clearInterval(welfInterval);
              }
              $(".welf_bg > div").css({"-webkit-animation":"none"});
            }
          }
        });
        // 职位滑动
        var _width = winWidth;
        _width = _width-30*2;
        $(".posit_details").css({width:_width+"px"})
        $(".posit_details img").css({width:_width+"px"})
        var _ulWidth = $(".posit_details").length * (_width+15) + 15;

        $(".posit_list_ul").css({width:_ulWidth+"px"});

        $(".posit_list_ul").swipe({
          index:_indexSwipe,
          width:_width + 15,
          afterSwipe:function(index)
          {
            load_jobs(index,utype);
          }
        });
        // 企业图片
        var swipePage = {
            winWidth: document.documentElement.clientWidth,
            agent: navigator.userAgent.toLowerCase(),
            _indexSwipeUp: 0,
            _indexSwipe: 0,
            otherPage: false,
            isOpen: false,
            init: function() {
                if (swipePage.versions.iPhone && (swipePage.versions.ucbrowser || swipePage.versions.amqqbrowser)) {
                    $(".arrow_con, .job_btn_con").css({
                        bottom: "50px"
                    })
                }
                swipePage.swipeUpInit();
                swipePage.swipeInit()
            },
            swipeUpInit: function() {
                var infoidParam = getparam.getUrlParam("infoid"),
                    b = $("#poster_contain .poster_wrap");
                if (infoidParam != undefined && infoidParam != "") {
                    b.each(function(c) {
                        if ($(this).hasClass("posit")) {
                            swipePage._indexSwipeUp = c
                        }
                    })
                }
                var isWxBack = pageConfig.isWeixinBack;
                if (typeof isWxBack != "undefined" && isWxBack == "true") {
                    b.each(function(c) {
                        if ($(this).hasClass("praise")) {
                            swipePage._indexSwipeUp = c;
                            swipePage.isOpen = true
                        }
                    })
                }
                try {
                    if (guideAppWeixilie.isShow()) {
                        swipePage._indexSwipeUp = 1
                    }
                    swipePage.addBtn()
                } catch (e) {}
                if (localStorage) {
                    swipePage.otherPage = window.localStorage.getItem("otherPage")
                }
                if (typeof swipePage.otherPage !== "undefined" && swipePage.otherPage === "true") {
                    b.each(function(i) {
                        if ($(this).hasClass("posit")) {
                            swipePage._indexSwipeUp = i
                        }
                    });
                    window.localStorage.setItem("otherPage", false)
                }
                swipePage.addBtn(".focus");
                $("#poster_contain").swipeUp({
                    index: swipePage._indexSwipeUp,
                    childrenClass: ".poster_wrap",
                    init: function() {
                        $(".arrow_con").addClass("show");
                        setTimeout(function() {
                            $(b.get(swipePage._indexSwipeUp)).addClass("focus")
                        }, 300)
                    },
                    afterSwipe: function(index) {
                        var _pw = $("#poster_contain .poster_wrap");
                        _pw.removeClass("focus");
                        $(_pw.get(index)).addClass("focus");
                        if (index == _pw.length - 1) {
                            $(".arrow_con").removeClass("show")
                        } else {
                            $(".arrow_con").addClass("show")
                        }
                        if (index == 2) {
                            welfInterval = setInterval(function() {
                                var _s = parseInt(Math.random() * 6);
                                $(".welf_bg div").css({
                                    "-webkit-animation": "none",
                                    animation: "none"
                                });
                                $($(".welf_bg div").get(_s)).css({
                                    "-webkit-animation": "fuli" + _s % 2 + " 1s ease-out",
                                    animation: "fuli" + _s % 2 + " 1s ease-out"
                                })
                            }, 1e3)
                        } else {
                            if (typeof welfInterval != "undefined") {
                                clearInterval(welfInterval)
                            }
                            $(".welf_bg div").css({
                                "-webkit-animation": "none",
                                animation: "none"
                            })
                        }
                        if (userid == getparam.getUserInfo().uid) {
                            var isshare = $(".poster_wrap.posit");
                            var jianli = $(".poster_wrap.jianli");
                            if (isshare.hasClass("focus") || jianli.hasClass("focus")) {
                                $(".nav_btn_con,.reward_btn_modular").hide()
                            } else {
                                $(".nav_btn_con,.reward_btn_modular").show()
                            }
                        }
                        try {
                            guideAppWeixilie.init({
                                index: index
                            })
                        } catch (e) {}
                    }
                })
            },
            swipeInit: function() {
                var infoidParam = getparam.getUrlParam("infoid");
                if (infoidParam != "") {
                    $(".posit_list_ul .posit_details").each(function(i) {
                        if ($(this).attr("data-infoid") == infoidParam) {
                            swipePage._indexSwipe = i;
                            return false
                        }
                    })
                }
                var a = swipePage.winWidth;
                if (swipePage.versions.android && swipePage.versions.micromessenger) {
                    if (window.devicePixelRatio > 1) {
                        if (swipePage.agent.miuibrowser) {
                            a = swipePage.winWidth
                        } else {
                            a = window.screen.width / window.devicePixelRatio
                        }
                    }
                }
                a = a - 30 * 2;
                var b = $(".posit_details").length * (a + 15) + 15;
                $(".posit_details").css({
                    width: a + "px"
                });
                $(".posit_list_ul").css({
                    width: b + "px"
                });
                if ($(".posit").length > 0) {
                    $(".posit_list_ul").attr("data-indexNum", swipePage._indexSwipe);
                    $(".posit_list_ul").swipe({
                        index: swipePage._indexSwipe,
                        width: a + 15,
                        childrenClass: ".posit_details",
                        afterSwipe: function(index) {
                            var _job = $($(".posit_details").get(index));
                            if (typeof weizhan !== "undefined" && typeof weizhan.reward !== "undefined" && typeof eval(weizhan.reward.setRewardJobBtn) == "function") {
                                weizhan.reward.setRewardJobBtn(_job)
                            } else {
                                $(".job_btn_list > .add").attr("href", _job.attr("url"));
                                var telNum = _job.attr("tel");
                                var tel = telNum.replace("-", "");
                                $(".job_btn_list > .tel").attr("href", "tel:" + _job.attr("tel").replace("-", ""))
                            }
                        }
                    })
                }
                if ($(".imgs").length > 0) {
                    $(".img_bg").css("width", a + "px");
                    $(".img_bg img").css("width", a - 6 + "px");
                    var imgWidth = $(".img_bg").length * (a + 15) + 15;
                    $(".img_msg_con").css("width", imgWidth + "px");
                    $(".imgs .img_msg_con").swipe({
                        index: 0,
                        width: a + 15,
                        childrenClass: ".img_bg",
                        afterSwipe: function(index) {}
                    })
                }
            },
            screenFix: function() {
                var winWidth = document.documentElement.clientWidth,
                    winHeight = window.innerHeight;
                $("#poster_contain").css({
                    width: winWidth + "px",
                    height: winHeight + "px"
                })
            },
            openApp: function() {
                guideApp.init();
            },
            replaceTel: function(obj) {
                var telNum = obj.href;
                var tel = telNum.replace("-", "");
                obj.removeAttribute("href");
                obj.setAttribute("href", tel)
            },
            addBtn: function(param) {
                if (typeof param == "undefined") {
                    param = ""
                }
                var active_page = $(".poster_wrap" + param);
                var about_text = $(active_page).find(".about_us_msg");
                var about_text_p = $(active_page).find("p");
                if (about_text[0]) {
                    var about_text_height = about_text[0].clientHeight;
                    var about_text_width = $(".about_us_msg_p")[0].clientWidth;
                    var i = about_text_p.length - 1;
                    var str_temp = $(".about_us_msg_p p").text().replace(/\s/g, "");
                    var tatolfontNum = str_temp.length;
                    var a_temp = Math.round(about_text_height / 22);
                    var b_temp = about_text_width / 13;
                    var c_temp = Math.round(tatolfontNum / b_temp);
                    if (a_temp < c_temp) {
                        var more_html = '<div class="more_detail"><span>查看更多>></span></div>';
                        var about_bottom = $(active_page).find(".about_bottom")[0];
                        $(about_bottom).html(more_html);
                        $(".more_detail").on("click", function() {
                            Dialog.init(about_text.find("p"))
                        })
                    }
                }
            },
            versions: function() {
                var u = navigator.userAgent,
                    app = navigator.appVersion;
                return {
                    ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/),
                    android: u.indexOf("Android") > -1 || u.indexOf("Linux") > -1,
                    iPhone: u.indexOf("iPhone") > -1,
                    iPad: u.indexOf("iPad") > -1,
                    ucbrowser: u.indexOf("ucbrowser") > -1,
                    mqqbrowser: u.indexOf("mqqbrowser") > -1,
                    micromessenger: u.indexOf("micromessenger") > -1
                }
            }()
        };
        var a = swipePage.winWidth;
        if (swipePage.versions.android && swipePage.versions.micromessenger) {
            if (window.devicePixelRatio > 1) {
                if (swipePage.agent.miuibrowser) {
                    a = swipePage.winWidth
                } else {
                    a = window.screen.width / window.devicePixelRatio
                }
            }
        }
        a = a - 30 * 2;
        if ($(".imgs").length > 0) {
            $(".img_bg").css("width", a + "px");
            $(".img_bg img").css("width", a - 6 + "px");
            var imgWidth = $(".img_bg").length * (a + 15) + 15;
            $(".img_msg_con").css("width", imgWidth + "px");
            $(".imgs .img_msg_con").swipe({
                index: 0,
                width: a + 15,
                childrenClass: ".img_bg",
                afterSwipe: function(index) {}
            })
        }
        // 企业简介更多
        if (typeof param == "undefined") {
            param = ""
        }
        var active_page = $(".poster_wrap_about_us");
        var about_text = $(active_page).find(".about_us_msg");
        var about_text_p = $(active_page).find("p");
        if (about_text[0]) {
            var about_text_height = about_text[0].clientHeight;
            var about_text_width = $(".about_us_msg_p")[0].clientWidth;
            var i = about_text_p.length - 1;
            var str_temp = $(".about_us_msg_p p").text().replace(/\s/g, "");
            var tatolfontNum = str_temp.length;
            var a_temp = Math.round(about_text_height / 22);
            var b_temp = about_text_width / 13;
            var c_temp = Math.round(tatolfontNum / b_temp);
            if (tatolfontNum > 120) { // a_temp < c_temp
                var more_html = '<div class="more_detail"><span>查看更多>></span></div>';
                var about_bottom = $(active_page).find(".about_bottom")[0];
                $(about_bottom).html(more_html);
                $(".more_detail").on("click", function() {
                    $('.dialog_bg').show();
                    $('.dia-arrow').on('click', function () {
                        $('.dialog_bg').hide();
                    });
                })
            }
        }
        //分享按钮
        $('.praise_share_btn').on('click',function(){
          var agent = navigator.userAgent.toLowerCase();
          if(agent.indexOf('micromessenger') < 0)
          {
            share_();
          }
          else
          {
            share();
          }
        });
        $(".layer, .wx_layer").on("click", function(){
          $(this).hide();
        });
        
    };
    // 点赞
    function praise(company_id)
    {
    	$(".praise_btn_click").on('click',function(event)
        {
          setCookie('praise_'+company_id+'','1');
          if($(".praise_btn").hasClass('praise_btn_click')){
            $.getJSON(qscms.root+"?m=Mobile&c=Wzp&a=com_praise_click",{id:company_id},function(result){
              if(result.status==1){
                $("#praise_num").html(result.data);
                $(".praise_btn").addClass('on').removeClass('praise_btn_click');
              }
            });
          }
        });
    }
    function setCookie(name,value) 
    { 
        var Days = 30; 
        var exp = new Date(); 
        exp.setTime(exp.getTime() + Days*24*60*60*1000); 
        document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString(); 
    } 

    //读取cookies 
    function getCookie(name) 
    { 
        var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
     
        if(arr=document.cookie.match(reg))
     
            return unescape(arr[2]); 
        else 
            return null; 
    } 
    function delCookie(name) 
    { 
        var exp = new Date(); 
        exp.setTime(exp.getTime() - 1); 
        var cval=getCookie(name); 
        if(cval!=null) 
            document.cookie= name + "="+cval+";expires="+exp.toGMTString(); 
    }
        
    /* 延时加载 time */
   	function loading(time){
   		var time=time?time:1000;
    	setTimeout(function(){
	      $("#load").hide();
	      swipe_self();
	    }, time);
    };
    /* 申请职位 弹出框 */
    function showFloatBox()
    {
        var width = window.innerWidth;
        var height = window.innerHeight;
        $("body").prepend("<div class=\"menu_bg_layer\"></div>");
        $(".menu_bg_layer").css({ height:height+'px',width: width+"px", position: "absolute",left:"0", top:"0","z-index":"999","background-color":"#000000"});
        $(".menu_bg_layer").css("opacity",0.3);
    };
    /* 申请职位 操作 */
    function jobs_apply()
    {
    	$("#jobs_apply").on('click',function()
      {
          var href= $(this).attr("href")
          if(href=="javascript:;")
          {
              var jobs_id = $(this).attr("jobs_id");
              if(qscms.resume_id){
                  $.getJSON(qscms.root+'?m=Mobile&c=AjaxPersonal&a=resume_apply',{jid:jobs_id},function(result){
                      if(result.status==1){
                        alert(result.msg);
                      }else{
                        alert(result.msg);
                      }
                  },'json');
                }else{
                  alert('请选择简历');
                }
          }

      });
    };
	
	
		$("#jobs_phone").on('click',function() {
      var href= $(this).attr("pho");
      if(href){
        var result = href.replace('tel:','');
      }else{
        var result = '';
      }
      if(result=='') {
          var shopping = document.getElementById("jobs_phone");
          var phone =  shopping.getAttribute("phone");
          showFloatBox();
          $("#jobs_phone_menu").show();
          var height = window.innerHeight;
          var choose_menu_h = document.getElementById('jobs_phone_menu').offsetHeight;
          var top_ = (height-choose_menu_h)/2;
          $("#jobs_phone_menu").css("top",top_+"px");
          $("#jobs_phone_menu").css({"opacity":1,"z-index":9999});
          $(".but_right,.menu_bg_layer").on('click', function(event) {
             $("#jobs_phone_menu").hide();
             $(".menu_bg_layer").remove();
          });
      } else {
        window.location.href=href;
      }
	  });
	
    /* 左侧 菜单*/
    function left_menu()
    {
    	// 显示菜单
    	$(".nav_btn_con").on("touchstart", function(){
			$(".reward_manager_list_con, .reward_manager_list_con_bg").addClass("on");
		});
		// 隐藏菜单
		$(".reward_manager_list_con_bg").on("touchstart", function(){
			$(".reward_manager_list_con, .reward_manager_list_con_bg").removeClass("on");
		});
    };
    /* 显示分享 覆盖层 */
    function share(){
      $(".wx_layer").show();
      
	};

	function share_(){
    $(".layer").show();
	};
    console.log(utype);
	loading();
	left_menu();
	load_jobs(0,utype);
	praise(company_id);
    jobs_apply();
  if(getCookie('praise_'+company_id+'')==1)
  {
    $(".praise_btn").addClass('on').removeClass('praise_btn_click');
  }
})(jq);