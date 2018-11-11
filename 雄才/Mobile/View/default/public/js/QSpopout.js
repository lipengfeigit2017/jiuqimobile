/**
 * 弹出框类
 * @param title 标题
 * @constructor
 */
function QSpopout(title) {
    this.title = title;
    this.init();
}

QSpopout.prototype = {
    /**
     * 初始化
     */
    init: function() {
        var popoutHtml = '<div id="popout" style="display: none;">';
        popoutHtml += '<div class="qs-mask"></div>';
        popoutHtml += '<div class="qs-popout">';
        if (this.title) {
            popoutHtml += '<div class="qs-popout-hd"><div class="qs-popout-title">' + this.title + '</div></div>';
        }
        popoutHtml += '<div class="qs-popout-bd"></div>';
        popoutHtml += '<div class="qs-popout-ft">';
        popoutHtml += '<a href="javascript:;" class="qs-popout-btn qs-popout-btn-default"></a>';
        popoutHtml += '<a href="javascript:;" class="qs-popout-btn qs-popout-btn-primary"></a>';
        popoutHtml += '</div>';
        popoutHtml += '</div>';
        popoutHtml += '</div>';
        $('body').append(popoutHtml);

        this.popout = $('#popout');
        // 设置默认按钮
        this.defaultBtn = this.getDefaultBtn();
        this.primaryBtn = this.getPrimaryBtn();
        this.setBtn(2, ['取消', '确定']);
    },
    /**
     * 显示
     */
    show: function() {
        this.popout.fadeIn(200);
    },
    /**
     * 设置内容
     * @param value 内容
     */
    setContent: function(value) {
        $('.qs-popout-bd').html(value);
    },
    /**
     * 隐藏
     */
    hide: function() {
        this.popout.fadeOut(200);
        this.popout.remove();
    },
    /**
     * 设置按钮
     * @param num 按钮数量
     * @param value 按钮内容
     */
    setBtn: function(num, value) {
        var that = this;
        if (num == 1) {
            this.defaultBtn.hide();
            value = value ? value : '确定';
            this.primaryBtn.text(value);
        } else {
            this.defaultBtn.text(value[0]);
            this.primaryBtn.text(value[1]);
        }
        // 关闭
        $('.qs-popout-btn').on('click', function() {
            that.hide();
        });
    },
    /**
     * 获取主操作按钮
     * @returns {*|jQuery|HTMLElement}
     */
    getPrimaryBtn: function() {
        return $('.qs-popout-btn-primary');
    },
    /**
     * 获取辅助操作按钮
     * @returns {*|jQuery|HTMLElement}
     */
    getDefaultBtn: function() {
        return $('.qs-popout-btn-default');
    }
}