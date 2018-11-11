RongIMClient.init(rongConfig.Key);
// 设置连接监听状态 （ status 标识当前连接状态）
// 连接状态监听器
RongIMClient.setConnectionStatusListener({
    onChanged: function (status) {
        switch (status) {
            //链接成功
            case RongIMLib.ConnectionStatus.CONNECTED:
                console.log('链接成功');
                break;
            //正在链接
            case RongIMLib.ConnectionStatus.CONNECTING:
                console.log('正在链接');
                break;
            //重新链接
            case RongIMLib.ConnectionStatus.DISCONNECTED:
                console.log('断开连接');
                break;
            //其他设备登录
            case RongIMLib.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT:
                console.log('其他设备登录');
                break;
              //网络不可用
            case RongIMLib.ConnectionStatus.NETWORK_UNAVAILABLE:
              console.log('网络不可用');
              break;
        }
    }
});
// 消息监听器
RongIMClient.setOnReceiveMessageListener({
    // 接收到的消息
    onReceived: function (message) {
        // 判断消息类型
        switch(message.messageType){
            case RongIMClient.MessageType.TextMessage:
                //接收发送给别人的消息
                if(rongUser.sendUser && message.senderUserId==rongUser.sendUser.id){
                    var time = String(message.sentTime).substr(0,10);
                    if(time <= rongUser.newTime) return !1;
                    var a = {
                        message : message.content.content,
                        avatars : rongUser.sendUser.avatars,
                        addtime : time
                    };
                    var str = '<div class="pmsshow-left"><div class="pic"><img src="'+a.avatars+'"></div><div class="txt">'+a.message+'<div class="times font10">'+a.addtime+'</div><div class="arrow"></div></div><div class="clear"></div></div><div class="split-block"></div>';
                    $('#send').before(str);
                    rongChangeScrollHeight();
                    rongMessageRead(message.targetId);
                }else{
                    if(rongConfig.messageList){
                        var t = message.sentTime,
                            f = $('.imlist-img[uid="'+message.senderUserId+'"]'),
                            time = new Date(t).getHours()+':'+new Date(t).getMinutes()+':'+new Date(t).getSeconds();
                        if(f[0]){
                            var n = $.trim(f.find('.arrow').html());
                            f.find('.txt').html(message.content.content);
                            f.find('.sendtime').html(time);
                            f.find('.arrow').removeClass('qs-hidden').html(++n);
                            $('.imlist-img[uid="'+message.senderUserId+'"]').insertAfter('.headernavfixed');
                        }else{
                            var a = $.parseJSON(message.content.extra),
                                str = $('<div class="imlist-img" onClick="javascript:location.href=\''+rongConfig.messageInfoUrl.replace('_touid',a.id)+'\'" uid="'+a.id+'"><div class="pic"><img src="'+a.avatars+'"><div class="arrow font12">1</div></div><div class="describe font12"><div class="tit font14 substring">'+a.name+'<span class="font12">'+time+'</span></div><div class="txt substring">'+message.content.content+'</div><div class="del_dialog" uid="'+a.id+'"></div></div><div class="clear"></div></div>');
                            $('.headernavfixed').after(str);
                            $('.del_dialog').unbind('click').click(function(){
                                if(confirm('你确定要删除与该会员的会话吗？')){
                                    var f = $(this),
                                        uid = $(this).attr('uid');
                                    $.getJSON(rongConfig.delDialog,{uid:uid},function(r){
                                        if(r.status == 1){
                                            f.closest('.imlist-img').remove();
                                        }else{
                                            qsToast({type:2,context: r.msg});
                                        }
                                    });
                                }
                                return !1;
                            });
                        }
                    }
                    rongMessageUnread();
                }
                break;
            case RongIMClient.MessageType.ImageMessage:
                // do something...
                break;
            case RongIMClient.MessageType.DiscussionNotificationMessage:
                // do something...
                break;
            case RongIMClient.MessageType.LocationMessage:
                // do something...
                break;
            case RongIMClient.MessageType.RichContentMessage:
                // do something...
                break;
            case RongIMClient.MessageType.DiscussionNotificationMessage:
                // do something...
                break;
            case RongIMClient.MessageType.InformationNotificationMessage:
                // do something...
                break;
            case RongIMClient.MessageType.ContactNotificationMessage:
                // do something...
                break;
            case RongIMClient.MessageType.ProfileNotificationMessage:
                // do something...
                break;
            case RongIMClient.MessageType.CommandNotificationMessage:
                // do something...
                break;
            case RongIMClient.MessageType.CommandMessage:
                // do something...
                break;
            case RongIMClient.MessageType.UnknownMessage:
                // do something...
                break;
            default:
                // 自定义消息
                // do something...
        }
    }
});
// 连接融云服务器。
RongIMClient.connect(rongConfig.Token, {
    onSuccess: function(userId) {
        // 获取会话列表
        RongIMClient.getInstance().getRemoteConversationList({
            onSuccess: function(list) {
                $.getJSON(rongConfig.messageUnreadUrl,function(r){
                    if(r.status == 1 && r.data){
                        $('#J_imStatus').show();
                    }
                });
            },
            onError: function(error) {}
        },null);

    },
    onTokenIncorrect: function() {
      console.log('token无效');
    },
    onError:function(errorCode){
        var info = '';
        switch (errorCode) {
            case RongIMLib.ErrorCode.TIMEOUT:
              info = '超时';
              break;
            case RongIMLib.ErrorCode.UNKNOWN_ERROR:
              info = '未知错误';
              break;
            case RongIMLib.ErrorCode.UNACCEPTABLE_PaROTOCOL_VERSION:
              info = '不可接受的协议版本';
              break;
            case RongIMLib.ErrorCode.IDENTIFIER_REJECTED:
              info = 'appkey不正确';
              break;
            case RongIMLib.ErrorCode.SERVER_UNAVAILABLE:
              info = '服务器不可用';
              break;
        }
        console.log(errorCode);
    }
});
/**
 * 发送消息
 * @param  {integer} uid  用户id
 * @param  {string}  word 发送的消息
 */
function rongSendMessage(uid,word){
    // 定义消息类型,文字消息使用 RongIMLib.TextMessage
    var msg = new RongIMLib.TextMessage({content:word,extra:JSON.stringify(rongConfig.userInfo)});
    var conversationtype = RongIMLib.ConversationType.PRIVATE; // 私聊
    var targetId = uid; // 目标 Id
    RongIMClient.getInstance().sendMessage(conversationtype, targetId, msg, {
        // 发送消息成功
        onSuccess: function (message) {
            //message 为发送的消息对象并且包含服务器返回的消息唯一Id和发送消息时间戳
            $.ajax({
                url: rongConfig.messageUrl,
                type: 'POST',
                dataType: 'json',
                data: {formuid:message['senderUserId'],touid:message['targetId'],messageType:message['messageType'],time:message['sentTime'],message:message['content']['content']},
                success: function(r){
                    var data = {
                            avatars : rongConfig.userInfo.avatars,
                            content : r.data.message,
                            addtime : r.data.addtime
                        }
                    $('#send').before(rongCreateMessage(data));
                    rongChangeScrollHeight();
                    $('#J_val').val('');
                }
            })
        },
        onError: function (errorCode,message) {
            var info = '';
            switch (errorCode) {
                case RongIMLib.ErrorCode.TIMEOUT:
                    info = '超时';
                    break;
                case RongIMLib.ErrorCode.UNKNOWN_ERROR:
                    info = '未知错误';
                    break;
                case RongIMLib.ErrorCode.REJECTED_BY_BLACKLIST:
                    info = '在黑名单中，无法向对方发送消息';
                    break;
                case RongIMLib.ErrorCode.NOT_IN_DISCUSSION:
                    info = '不在讨论组中';
                    break;
                case RongIMLib.ErrorCode.NOT_IN_GROUP:
                    info = '不在群组中';
                    break;
                case RongIMLib.ErrorCode.NOT_IN_CHATROOM:
                    info = '不在聊天室中';
                    break;
                default :
                    info = x;
                    break;
            }
            alert('发送失败:' + info + '【请刷新页面重试！】');
        }
    });
}
/**
 * 组合聊天框中的消息
 * @param  {obj} messageContent 消息内容
 */
function rongCreateMessage(a){
    var str = '<div class="pmsshow-right"><div class="txt">'+a.content+'<div class="times font10">'+a.addtime+'</div><div class="arrow"></div></div><div class="pic"><img src="'+a.avatars+'"></div><div class="clear"></div></div><div class="split-block"></div>'
    return str;
}
/**
 * 调整对话框滚动轴位置
 * @param  {integer} num 滚动轴位置
 */
function rongChangeScrollHeight(a){
    $('body').scrollTo({
        toT : $('body').height(),
        durTime: typeof a === undefined ? 500 : a
    })
}
/**
 * [rongMessageRead 标记当前会话已读信息]
 */
function rongMessageRead(a){
    $.ajax({
        url: rongConfig.messageReadUrl,
        type: 'POST',
        dataType: 'json',
        data: {uid:a},
        success: function(r){}
    })
}
function rongMessageUnread(){
    $('#J_imStatus').show();
}
$.fn.slideUp = function(fn) {
    var a = $(this),
        v = 0;
    for(var i = 0;i<a.length;i++){
        a[i].addEventListener('touchstart', function(e) {
            v = e.changedTouches[0].pageY;
        }, false);
        a[i].addEventListener('touchmove', function(e) {
            if($(this).scrollTop() <= 0 && e.changedTouches[0].pageY > v) fn($(this));
        }, false);
    }
}
$('#J_btn').click(function(){
    var content = $.trim($('#J_val').val());
    if(content == ''){
        alert('消息内容不能为空！');
        return !1;
    }
    rongSendMessage(rongUser.sendUser.id,content);
});