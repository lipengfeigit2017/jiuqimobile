/**
 * 显示提示消息
 * @param options 自定义参数
 */
function qsToast(options) {
    if ($('.qs-toast').css('visibility') == 'visible') { // 防止重复点击
        return false;
    }
    var defaultOptions = {
        type: 1, // 消息类型 1:成功 2:警告 3:金币
        context: '', // 内容
        delay: 2000 // 自动消失时间(毫秒)
    };
    $.extend(defaultOptions, options);
    var typeClass = 'success';
    if (defaultOptions.type == 2) {
        typeClass = 'remind';
    }
    if (defaultOptions.type == 3) {
        typeClass = 'coins';
    }
    var toastHtml = '<div id="toast" style="display: none;">';
    toastHtml += '<div class="qs-mask-transparent"></div>';
    toastHtml += '<div class="qs-toast">';
    // toastHtml += '<i class="qs-icon qs-icon-toast qs-icon-toast-' + typeClass + '"></i>';
    toastHtml += '<p class="qs-toast-content">' + defaultOptions.context + '</p>';
    toastHtml += '<div class="clear"></div>';
    toastHtml += '</div>';
    toastHtml += '</div>';

    $('body').append(toastHtml);
    $('#toast').fadeIn(200);
    $('.qs-toast').css({
        left: ($(window).width() - $('.qs-toast').width())/2
    });
	setTimeout(function() {
		$('#toast').remove();
	}, defaultOptions.delay);
}