/**
 * 新地区
 */
$(function() {
  // 新地区
  $('.qs-temp-new-city').each(function() {
    var that = $(this);
    var thisType = $(this).data('type');
    var dataBase = QS_city_parent;
    var dataSource = QS_city;
    var linkHtml = '<div class="c noC" data-code="no"><div class="c-icb"><div class="c-ic"></div></div><div class="c-txb">不限</div><div class="clear"></div></div>';
    var multiple = eval($(this).data('multiple'));
    var defaultCity = qscms.default_district;
    var cateLevel = qscms.subsite_level;
    var thisBox = '.f-box-' + thisType;
    var recoveryValue = $('.qs-temp-code-' + thisType).val();
    var idp = '';
    if (dataBase) {
      var tempHtml = '<div class="f-box-inner">';
      if (cateLevel > 1) {
        tempHtml += '<div class="ncf-list-g for-type cusType">';
        tempHtml += '<div class="ncf-list-c Js-ncf-list-c">当前选择：<span class="cuPo"></span></div>';
        tempHtml += '<div class="ncf-list-bt font10 backPrev" code="">返回上一级</div>';
        tempHtml += '<div class="clear"></div>';
        tempHtml += '</div>';
      }
      tempHtml += '<div class="ncc-list-g">';
      tempHtml += '<div class="ncc-list-c Js-ncc-list-c">';
      tempHtml += linkHtml;
      for (i = 0; i < dataBase.length; i++) {
        tempHtml += '<div class="c normalC" data-code="'+ dataBase[i].split(',')[0] +'"><div class="c-icb"><div class="c-ic"></div></div><div class="c-txb">'+ dataBase[i].split(',')[1] +'</div><div class="clear"></div></div>';
      }
      tempHtml += '</div>';
      tempHtml += '</div>';
      tempHtml += '</div>';
      $(thisBox).html(tempHtml);
      // 恢复选中和设置默认值
      if (recoveryValue) {
        idp = '';
        var firstKeepArr = new Array();
        if (eval($.fn.cookie('camFlag'))) {
            var camId = getParentsId(recoveryValue) + '.' + recoveryValue;
            firstKeepArr = camId.toString().split('.');
        } else {
            firstKeepArr = getParentsId(recoveryValue).toString().split('.');
            console.log(firstKeepArr);
        }
        if (firstKeepArr.length > 1) {
            // 不是第一级
            $('.Js-ncc-list-c').html(listFactory(dataSource[firstKeepArr[firstKeepArr.length-1]].split('`'), firstKeepArr[firstKeepArr.length-1], 'normalC'));
            setCurrentText(firstKeepArr, firstKeepArr[firstKeepArr.length-2]);
        }
        backRecoverStatus();
    } else {
        var defaultArr = new Array();
        if (defaultCity) {
            defaultArr = defaultCity.split('.');
        } else {
            if (getSubCateLevel(dataBase[0].split(',')[0],'')) {
                defaultArr = getSubCateLevel(dataBase[0].split(',')[0],'').split('.');
            }
        }
        if (defaultArr.length) {
            if (cateLevel > 1) {
               $('.Js-ncc-list-c').html(listFactory(dataSource[defaultArr[defaultArr.length-1]].split('`'), defaultArr[defaultArr.length-1], 'normalC'));
            }
        }
          // 设置当前选择
          if (cateLevel > 1) {
              setCurrentText(defaultArr, defaultArr[defaultArr.length-2]);
          }
      }
        // 设置当前选择文字
        function setCurrentText(arr, code) {
            $('.cuPo').text(getName(arr[0]));
            var cuHtml = '';
            for (var i = 0; i < arr.length; i++) {
                if (i > 0 && i < arr.length) {
                    cuHtml += '>' + getName(arr[i]);
                }
            }
            $('.cuPo').append(cuHtml);
            $('.cusType').show();
            // 显示返回上一级
            $('.backPrev').attr('code', code);
            $('.backPrev').show();
        }
        // 获得级数
        function getSubCateLevel(id, arr) {
            if (QS_city[id]) {
                var levelIdArr = QS_city[id].split('`');
                if (arr.length) {
                    arr = arr + '.' + id;
                } else {
                    arr = id;
                }
                return getSubCateLevel(levelIdArr[0].split(',')[0],arr);
            } else {
                return arr;
            }
        }
      // 根据id获取对应文字
        function getName(id) {
            var name = '';
            for (var i = 0; i < dataBase.length; i++) {
                if (id == dataBase[i].split(',')[0]) {
                    name = dataBase[i].split(',')[1];
                }
            }
            if (!name){
                for (var i = 0; i < dataSource.length; i++) {
                    if (dataSource[i]) {
                        var iArr = dataSource[i].split('`');
                        for (var j = 0; j < iArr.length; j++) {
                            if (id == iArr[j].split(',')[0]) {
                                name = iArr[j].split(',')[1];
                            }
                        }
                    }
                }
            }
            return name;
        }
      // 返回上一级复原选中状态
        function backRecoverStatus() {
            $('.Js-ncc-list-c .c').each(function () {
                $(this).removeClass('chk');
            })
            idp = '';
            var keepItem = getParentsId(recoveryValue);
            if (keepItem) {
                keepItem = keepItem + '.' + recoveryValue;
            } else {
                keepItem = recoveryValue;
            }
            var cArr = keepItem.split('.');
            for (var j = 0; j < cArr.length; j++) {
                if (eval($.fn.cookie('camFlag'))) {
                    $('.Js-ncc-list-c .c').each(function () {
                        if ($(this).hasClass('camFlag')) {
                            if ($(this).data('code') == cArr[j]) {
                                $(this).addClass('chk');
                            }
                        }
                    })
                } else {
                    $('.Js-ncc-list-c .c').each(function () {
                        if (!$(this).hasClass('camFlag')) {
                            if ($(this).data('code') == cArr[j]) {
                                $(this).addClass('chk');
                            }
                        }
                    })
                }

            }
        }
      // 统一处理当前选择的文字
      function setCheckText(id, text) {
        if ($('.cuPo').text().length) {
          $('.cuPo').append('>' + text);
        } else {
          $('.cuPo').text(text);
        }
        $('.cusType').show();
        // 显示返回上一级
        $('.backPrev').attr('code', getParentId(id));
        $('.backPrev').show();
      }
        // 地区选择
        $('.normalC').die().live('click', function () {
            var code = $(this).data('code'),
                text = $(this).text();
            if ($(this).hasClass('camFlag')) {
                $.fn.cookie('camFlag',1);
                goPageSome(code, $(this).data('title'));
            } else {
                if (dataSource[code]) {
                    $.fn.cookie('camFlag',0);
                    setCheckText(code, text);
                    $('.Js-ncc-list-c').html(listFactory(dataSource[code].split('`'), code, 'normalC'));
                } else {
                    $.fn.cookie('camFlag',0);
                    goPageSome(code, text);
                }
            }
            backRecoverStatus();
        })
        // 跳转处理方法
        function goPageSome(code, text) {
            $('.qs-temp-txt-' + thisType).text(text);
            $('.qs-temp-code-' + thisType).val(code);
            $('.qs-temp-code-range').val('');
            $('.cuPo').append(text);
            $.fn.cookie('sRange',0);
            clearFilter();
            goPage();
        }
      // 返回上一级
      $('.backPrev').die().live('click', function () {
        var code = $(this).attr('code');
        $('.backPrev').attr('code', getParentId(code));
        if (code) {
          $('.Js-ncc-list-c').html(listFactory(dataSource[code].split('`'), code, 'normalC'));
          var oTxtArr = $('.cuPo').text().split('>');
          oTxtArr.pop();
          $('.cuPo').text(oTxtArr.join('>'));
        } else {
          $('.Js-ncc-list-c').html(linkHtml + listFactory(dataBase, '', 'normalC'));
          $('.cuPo').text('');
          $('.backPrev').hide();
          $('.cusType').hide();
        }
        backRecoverStatus();
      })
      // 筛选不限
      $('.noC').die().live('click', function () {
        $('.qs-temp-code-' + thisType).val('');
        $('.qs-temp-txt-' + thisType).text('地区');
        $('.qs-temp-code-range').val('');
        clearFilter();
        goPage();
      })
      // 获取父级id
      function getParentsId(id) {
        for (var i = 0; i < QS_city.length; i++) {
          if (QS_city[i]) {
            var subArr = QS_city[i].split('`');
            for (var j = 0; j < subArr.length; j++) {
              if (id == subArr[j].split(',')[0]) {
                if (idp) {
                  idp =  i + '.' + idp;
                } else {
                  idp =  i;
                }
                getParentsId(i);
              }
            }
          }
        }
        return idp;
      }
      // 获取上一级id
      function getParentId(id) {
        var ids = '';
        for (var i = 0; i < dataSource.length; i++) {
          if (dataSource[i]) {
            var subArr = dataSource[i].split('`');
            for (var j = 0; j < subArr.length; j++) {
              if (id == subArr[j].split(',')[0]) {
                ids =  i;
              }
            }
          }
        }
        return ids;
      }
      // 生产列表
        function listFactory(data, dataParentId, className) {
            var dataHtml = '';
            if (dataParentId) {
                dataHtml += '<div class="camFlag c '+ className +'" data-code="'+ dataParentId +'" data-title="' + getName(dataParentId) +'"><div class="c-icb"><div class="c-ic"></div></div><div class="c-txb">全'+ getName(dataParentId) +'</div><div class="clear"></div></div>';
            }
            for (i = 0; i < data.length; i++) {
                dataHtml += '<div class="c '+ className +'" data-code="'+ data[i].split(',')[0] +'"><div class="c-icb"><div class="c-ic"></div></div><div class="c-txb">'+ data[i].split(',')[1] +'</div><div class="clear"></div></div>';
            }
            return dataHtml;
        }
      // 去除选中状态
      function removeCheckStatus() {
        $('.Js-ncc-list-c .c').each(function () {
          $(this).removeClass('chk');
        })
      }
      /**
       * 清除筛选
       */
      function clearFilter() {
        $('body').removeClass('filter-fixed');
        $(thisBox).addClass('qs-hidden');
        $('#f-mask').hide();
        $('.qs-temp').removeClass('active');
      }
    } else {
      console.log('No cache!!!');
    }
  })
});
