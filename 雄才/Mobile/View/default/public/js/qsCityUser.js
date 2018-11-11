/**
 * 新地区
 */
$(function() {
  // 新地区
  $('.qs-temp-new-city').each(function() {
    var that = $(this);
    var dataBase = QS_city_parent;
    var dataSource = QS_city;
    var multiple = eval($(this).data('multiple'));
    var defaultCity = qscms.default_district;
    var cateLevel = qscms.subsite_level;
    var maximumValue = 3;
    var checkedPool = new Array();
    var recoveryValue = $('.qs-temp-code-city').val();
    var recoverKeepVal = $('.qs-temp-code-city').attr('keep');
    var idp = '';
    var fxHtml = '';
    if (cateLevel > 1) {
      fxHtml += '<div class="ncf-list-g for-type cusType">';
      fxHtml += '<div class="ncf-list-c Js-ncf-list-c">当前选择：<span class="cuPo"></span></div>';
      fxHtml += '<div class="ncf-list-bt font10 backPrev" code="">返回上一级</div>';
      fxHtml += '<div class="clear"></div>';
      fxHtml += '</div>';
    }
    $('.f-box-city').before(fxHtml);
    if (dataBase) {
      var tempHtml = '<div class="f-box-inner">';
      tempHtml += '<div class="ncc-list-g">';
      tempHtml += '<div class="ncc-list-c Js-ncc-list-c">';
      for (i = 0; i < dataBase.length; i++) {
        tempHtml += '<div class="c normalC" data-code="'+ dataBase[i].split(',')[0] +'"><div class="c-icb"><div class="c-ic"></div></div><div class="c-txb">'+ dataBase[i].split(',')[1] +'</div><div class="clear"></div></div>';
      }
      tempHtml += '</div>';
      tempHtml += '</div>';
      tempHtml += '</div>';
      $('.f-box-city').html(tempHtml);
      // 恢复选中和设置默认值
      if (recoveryValue) {
        // 处理默认数据
        var deCodeArr = $('.qs-temp-code-city').val().split(',');
        var newCodeArr = new Array();
        for (var i = 0; i < deCodeArr.length; i++) {
          var sArr = deCodeArr[i].split('.');
          newCodeArr.push(sArr[sArr.length-1]);
        }
        $('.qs-temp-code-city').val(newCodeArr);
        // 恢复选中
        var recoverKeepArr = recoverKeepVal.split(',');
        var firstKeepArr = recoverKeepArr[0].split('.');
        if (firstKeepArr.length > 1) {
          // 不是第一级
          $('.Js-ncc-list-c').html(listFactory(dataSource[firstKeepArr[firstKeepArr.length-2]].split('`'), 'normalC'));
        }
        setPool(recoverKeepArr);
        // 恢复当前选择
        if (firstKeepArr.length > 1) {
            setCurrentText(firstKeepArr, firstKeepArr[firstKeepArr.length-3]);
        }
      } else {
        // 没有选中判断是否有设置默认地区
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
                            $('.Js-ncc-list-c').html(listFactory(dataSource[defaultArr[defaultArr.length-1]].split('`'), 'normalC'));
                        }
                    }
            if (cateLevel > 1) {
                // 设置当前选择
                setCurrentText(defaultArr, defaultArr[defaultArr.length-2]);
            }
      }
      recoverChk();
      if (multiple) {
        syncOptionSelected();
      }
        // 设置当前选择文字
        function setCurrentText(arr, code) {
            $('.cuPo').text(getName(arr[0], 1));
            var cuHtml = '';
            for (var i = 0; i < arr.length; i++) {
                if (i > 0 && i < arr.length) {
                    cuHtml += '>' + getName(arr[i], 0);
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
        if (dataSource[code]) {
          setCheckText(code, text);
          $('.Js-ncc-list-c').html(listFactory(dataSource[code].split('`'), 'normalC'));
          backRecoverStatus();
        } else {
          if (multiple) {
            // 多选
            var poolSub = code + '`' + text;
            // 是否已选中
            if ($(this).hasClass('chk')) {
              $(this).removeClass('chk');
              checkedPool.splice($.inArray(poolSub, checkedPool), 1);
            } else {
              // 判断是否超出可选的最大数量
              if ((checkedPool.length + 1) > maximumValue) {
                $(this).removeClass('chk');
                qsToast({type:2,context: '最多选择' + maximumValue + '个'});
                return false;
              } else {
                checkedPool.push(poolSub);
              }
            }
            // 同步已选列表
            syncOptionSelected();
          } else {
            // 单选
            $('.normalC').each(function(index, el) {
                $(this).removeClass('chk');
            })
            $(this).addClass('chk');
            $('.qs-temp-txt-city').text(text);
            $('.qs-temp-code-city').val(code);
            idp = '';
            var keepItem = getParentsId(code);
            if (keepItem) {
              keepItem = keepItem + '.' + code;
            } else {
              keepItem = code;
            }
            $('.qs-temp-code-city').attr('keep', keepItem);
            $('.js-actionsheet').removeClass('qs-actionsheet-toggle');
            $('.qs-mask').fadeOut(200);
          }
        }
      })
      // 设置还原数据
      function setPool(poolArr) {
        for (var i = 0; i < poolArr.length; i++) {
          var itemName = '';
          if (poolArr[i].length > 1) {
            var poolSubArr = poolArr[i].split('.');
            itemName = getName(poolSubArr[poolSubArr.length-1], 0);
            checkedPool.push(poolSubArr[poolSubArr.length-1] + '`' + itemName);
          } else {
            itemName = getName(poolArr[i], 1);
            checkedPool.push(poolArr[i] + '`' + itemName);
          }
        }
      }
      // 根据id获取对应文字
      function getName(id, pid) {
        var name = '';
        if (pid) {
          // 只有一级
          for (var i = 0; i < dataBase.length; i++) {
            if (id == dataBase[i].split(',')[0]) {
              name = dataBase[i].split(',')[1];
            }
          }
        } else {
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
      // 复原选中状态
      function recoverChk() {
        for (var i = 0; i < checkedPool.length; i++) {
          $('.Js-ncc-list-c .c').each(function () {
            if ($(this).data('code') == checkedPool[i].split('`')[0]) {
              $(this).addClass('chk');
            }
          })
        }
      }
      // 同步
      function syncOptionSelected() {
        if (checkedPool.length) {
          var checkedHtml = '';
          for (var i = 0; i < checkedPool.length; i++) {
            var pollArr = checkedPool[i].split('`');
            checkedHtml += '<div class="s-list-cell" data-code="' + pollArr[0] + '" data-title="' + pollArr[1] + '">' + pollArr[1] + '</div>';
          }
          $('.f-selected-group-city .s-list').html(checkedHtml);
          $('.f-selected-group-city .s-list').removeClass('qs-hidden');
        } else {
          $('.f-selected-group-city .s-list').html(checkedHtml);
          $('.f-selected-group-city .s-list').addClass('qs-hidden');
        }
        backRecoverStatus();
      }
      // 已选列表点击
      $('.f-selected-group-city .s-list-cell').die().live('click', function () {
        var code = $(this).data('code');
        var codeSub = $(this).data('code') + '`' + $(this).data('title');
        checkedPool.splice($.inArray(codeSub, checkedPool), 1);
        syncOptionSelected();
      })
      // 返回上一级复原选中状态
      function backRecoverStatus() {
        $('.Js-ncc-list-c .c').each(function () {
          $(this).removeClass('chk');
        })
        var keepArray = new Array();
        for (var i = 0; i < checkedPool.length; i++) {
          var cArr = checkedPool[i].split('`');
          idp = '';
          var keepItem = getParentsId(cArr[0]);
          if (keepItem) {
            keepArray[i] = keepItem + '.' + cArr[0];
          } else {
            keepArray[i] = cArr[0];
          }
        }
        for (var i = 0; i < keepArray.length; i++) {
          var cArr = keepArray[i].split('.');
          for (var j = 0; j < cArr.length; j++) {
            $('.Js-ncc-list-c .c').each(function () {
              if ($(this).data('code') == cArr[j]) {
                $(this).addClass('chk');
              }
            })
          }
        }
      }
      // 返回上一级
      $('.backPrev').die().live('click', function () {
        var code = $(this).attr('code');
        $('.backPrev').attr('code', getParentId(code));
        if (code) {
          $('.Js-ncc-list-c').html(listFactory(dataSource[code].split('`'), 'normalC'));
          var oTxtArr = $('.cuPo').text().split('>');
          oTxtArr.pop();
          $('.cuPo').text(oTxtArr.join('>'));
        } else {
          $('.Js-ncc-list-c').html(listFactory(dataBase, 'normalC'));
          $('.cuPo').text('');
          $('.backPrev').hide();
          $('.cusType').hide();
        }
        backRecoverStatus();
      })
      // 设置选中状态
      function setChkStatus(id) {
        $('.Js-ncc-list-c .c').each(function () {
          if ($(this).data('code') == id) {
            $(this).addClass('chk');
          }
        })
      }
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
      // 确定
      $('#qs-temp-confirm-city').die().live('click', function () {
        var codeArray = new Array();
        var titleArray = new Array();
        var keepArray = new Array();
        for (var i = 0; i < checkedPool.length; i++) {
          var cArr = checkedPool[i].split('`');
          codeArray[i] = cArr[0];
          idp = '';
          var keepItem = getParentsId(cArr[0]);
          if (keepItem) {
            keepArray[i] = keepItem + '.' + cArr[0];
          } else {
            keepArray[i] = cArr[0];
          }
          titleArray[i] = cArr[1];
        }
        $('.qs-temp-code-city').val(codeArray.join(','));
        $('.qs-temp-code-city').attr('keep', keepArray.join(','));
        var htxt = '';
        titleArray.length ? htxt = titleArray.join(',') : htxt = $('.qs-temp-txt-city').data('otxt');
        $('.qs-temp-txt-city').text(htxt);
      })
      // 生产列表
      function listFactory(data, className) {
        var dataHtml = '';
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
    } else {
      console.log('No cache!!!');
    }
  })
});
