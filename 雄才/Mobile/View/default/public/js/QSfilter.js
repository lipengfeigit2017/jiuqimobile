/**
 * 下拉类
 * @param obj 点击对象
 * @constructor
 */
function QSfilter(obj) {
    this.obj = obj;
    this.init();
}

QSfilter.prototype = {
    /**
     * 初始化
     */
    init: function() {
        var that = this;
        this.index = this.obj.data('tag');
        this.box = $('.f-box');
        if ($('body').hasClass('filter-fixed')) {
            if (this.obj.hasClass('active')) {
                that.clear();
            } else {
                this.obj.siblings('.js-filter').removeClass('active');
                that.showItem();
            }
            this.obj.toggleClass('active');
        } else {
            $('body').toggleClass('filter-fixed');
            this.obj.toggleClass('active');
            $('.qs-mask').show();
            that.showItem();
        }
        this.item = this.getItem();
        $('#f-mask').on('click', function() {
            that.clear();
            that.obj.removeClass('active');
        })
    },
    /**
     * 获取列表项
     * @returns {*|{set, expr}|{ID, NAME, TAG}}
     */
    getItem: function() {
        return this.box.eq(this.index).find('.f-item');
    },
    /**
     * 清除
     */
    clear: function() {
        $('body').removeClass('filter-fixed');
        this.hideItem();
        $('#f-mask').hide();
    },
    /**
     * 显示列表
     */
    showItem: function() {
        this.hideItem();
        this.box.eq(this.index).removeClass('qs-hidden');
        $('#f-mask').show();
    },
    /**
     * 隐藏列表
     */
    hideItem: function() {
        this.box.not('.f-more-content').addClass('qs-hidden');
    }
}
