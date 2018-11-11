/**
 * 读取缓存分类
 */
$(function () {
  var hideClass = 'qs-hidden';
  var tempArr = $('.qs-temp');
  var isSubsite = eval(qscms.is_subsite); // 是否是分站
  var subsiteLevelNum = eval(qscms.subsite_level); // 分站级数
  var subsiteLevel1 = true; // 是否是一级分站

  if (!isSubsite) { // 如果不是分站 一级分站始终为false
    subsiteLevel1 = false;
  } else {
    if (subsiteLevelNum > 1) { // 如果是分站，分站级数大于1 则为false
      subsiteLevel1 = false;
    }
  }
  $.each(tempArr, function() {
    var that = $(this);
    var thistype = $(this).data('type');
    var database = eval($(this).data('base'));
    var datasource = eval($(this).data('source'));
    var multiple = eval($(this).data('multiple')); // 多选单选
    var maxNum = $(this).data('num');
    var tempLevel = eval($(this).data('level')); // 二三级分类标识
    var thisBox = '.f-box-' + thistype;
    var selectedBox = '.f-selected-group-' + thistype;
    var thisLink = eval($(this).data('link')); // 是否是搜索
      var addJob = eval($(this).data('addjob')); // 是否是发布职位

    if (thisLink && isSubsite) { // 分站下的搜索页面
      if (database) {
        var tempHtml = '<div class="f-box-inner">';
        if (thisLink) {
          tempHtml += '<div class="level1Link">';
          tempHtml += '<li><a class="f-item f-none" href="javascript:;" data-code="" data-title="地区">不限</a></li>';
          tempHtml += '</div>';
          if (that.data('range')) {
            tempHtml += '<div class="level1Group">';
            tempHtml += '<li><a class="f-item" href="javascript:;" data-code="near" data-title="附近">附近</a></li>';
            tempHtml += '</div>';
          }
        }
        if (subsiteLevel1) { // 一级分站
          $.each(database, function(key, value) {
            if (value.split(',')) {
              tempHtml += '<div class="level1Link">';
              tempHtml += '<li><a class="f-item f-none" href="javascript:;" data-code="' + value.split(',')[0] + '" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
              tempHtml += '</div>';
            }
          })
        } else {
          $.each(database, function(key, value) {
            if (value.split(',')) {
              var tempLevel2Str = datasource[value.split(',')[0]];
              if (tempLevel2Str) {
                tempHtml += '<div class="level1Group">';
                tempHtml += '<li><a class="f-item" href="javascript:;" data-code="' + value.split(',')[0] + '" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                tempHtml += '</div>';
              } else {
                tempHtml += '<div class="level1Link">';
                tempHtml += '<li><a class="f-item f-none" href="javascript:;" data-code="' + value.split(',')[0] + '" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                tempHtml += '</div>';
              }
            }
          })
        }
        tempHtml += '</div>';
        $(thisBox).html(tempHtml);

        // 恢复选中
        var rvalue = $('.qs-temp-code-' + thistype).val();
        if (rvalue.length) {
          var rvalueArr = rvalue.split(',');
          if (subsiteLevel1) { // 一级分站
            $(thisBox + ' .level1Link .f-none').each(function(){
              if ($(this).data('code') == rvalue) {
                $(this).addClass('select');
              }
            });
          } else { // 二级分站
            // 先循环一级地区列表看是否能匹配
            $(thisBox + ' .level1Link .f-none').each(function(){
              if ($(this).data('code') == rvalue) {
                $(this).addClass('select');
              }
            });
            var recoverLevel1Id = '';
            $.each(database, function(key, value) { // 先找出与二级分类id对应的一级分类id
              if (value.split(',')) {
                if (datasource[value.split(',')[0]]) {
                  var recoverTempLevel2Str = datasource[value.split(',')[0]];
                  if (recoverTempLevel2Str) {
                    var recoverTempLevel22Str = recoverTempLevel2Str.split('`');
                    if (recoverTempLevel22Str) {
                      $.each(recoverTempLevel22Str, function(key2, value2) {
                        if (value2.split(',')[0] == rvalue) {
                          recoverLevel1Id = value.split(',')[0];
                        }
                      })
                    }
                  }
                }
              }
            })
            // 恢复一级
            $(thisBox + ' .level1Group .f-item').each(function(){
              if ($(this).data('code') == recoverLevel1Id) {
                $(this).addClass('select');
              }
            });
            // 生成需要恢复的二级
            var clickTempHtml = '<div class="f-box-inner">';
            var rtempLevel2Str = datasource[recoverLevel1Id];
            if (rtempLevel2Str) {
              var rtempLevel2Array = rtempLevel2Str.split('`');
              if (rtempLevel2Array) {
                clickTempHtml += '<div class="level2Group">';
                $.each(rtempLevel2Array, function(key, value) {
                  if (value.split(',')) {
                    if (value.split(',')[0]) {
                      if (value.split(',')[0] == rvalue) {
                        clickTempHtml += '<li><a class="f-item select" href="javascript:;" data-code="' + value.split(',')[0] + '" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                      } else {
                        clickTempHtml += '<li><a class="f-item" href="javascript:;" data-code="' + value.split(',')[0] + '" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                      }
                    }
                  }
                })
                clickTempHtml += '</div>';
              }
            }
            clickTempHtml += '</div>';
            $(thisBox).append(clickTempHtml);
            /**
             * 二级分类点击
             */
            $(thisBox + ' .level2Group a.f-item').on('click', function() {
              if ($(this).hasClass('select')) { // 先判断是否是选中状态
                $(this).removeClass('select');
                if (multiple) { // 多选条件下才同步
                  copyItem(thisBox, selectedBox);
                }
              } else {
                if (multiple) {
                  // 判断是否点击的是不限
                  var allCodeArr = $(this).data('code').split('.');
                  if (!eval(allCodeArr[2])) {
                    $(this).parents('.level3Group').find('.f-item').removeClass('select');
                  } else {
                    $(this).parents('.level3Group').find('.f-item').eq(0).removeClass('select');
                  }
                  if (overFlow(thisBox, maxNum)) {
                    $(this).addClass('select');
                    $(this).parents('.level3Group').prev().find('.f-item').addClass('select');
                    var levelIndex = getIndex($(this).closest('.level2Group'), thisBox + ' .level2Group');
                    $(thisBox + ' .level1Group').eq(levelIndex).find('.f-item').addClass('select');
                    copyItem(thisBox, selectedBox);
                  } else {
                    qsToast({context: '最多可选' + maxNum + '个'});
                  }
                } else {
                  $(thisBox + ' .f-item').removeClass('select');
                  $(this).addClass('select');
                  $(thisBox + ' .level1Group .f-item.active').addClass('select');
                  var qtcode = $(this).data('code');
                  var qttitle = $(this).data('title');
                  $('.qs-temp-txt-' + thistype).text(qttitle);
                  $('.qs-temp-code-' + thistype).val(qtcode);
                  if (thisLink) {
                    if ($(this).hasClass('f-range')) {
                      // 附近
                      $('.qs-temp-code-range').val(qtcode);
                      $('.qs-temp-code-' + thistype).val('');
                    } else {
                      $('.qs-temp-code-range').val('');
                    }
                    clearFilter();
                    goPage();
                  } else {
                    $('.js-actionsheet').removeClass('qs-actionsheet-toggle');
                    $('.qs-mask').fadeOut(200);
                  }
                }
              }
            })
          }
          if (multiple) {
            copyItem(thisBox, selectedBox);
          }
        } else {
          if (thisLink) {
            // 判断是否是筛选
            var rangeValue = $('.qs-temp-code-range').val();
            if (rangeValue.length) {
              $(thisBox + ' .level1Group').eq(0).find('.f-item').addClass('select');
              var rclickTempHtml = '<div class="f-box-inner">';
              rclickTempHtml += '<div class="level2Group">';
              rclickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="20" data-title="不限">不限</a></li>';
              rclickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="1" data-title="1公里">1公里</a></li>';
              rclickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="3" data-title="3公里">3公里</a></li>';
              rclickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="5" data-title="5公里">5公里</a></li>';
              rclickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="10" data-title="10公里">10公里</a></li>';
              rclickTempHtml += '</div>';
              rclickTempHtml += '</div>';
              $(thisBox).append(rclickTempHtml);
              var recoverRangeItemArr = $(thisBox + ' .level2Group').eq(0).find('.f-item');
              $.each(recoverRangeItemArr, function () {
                if ($(this).data('code') == rangeValue) {
                  $(this).addClass('select');
                }
              })
              /**
               * 二级分类点击
               */
              $(thisBox + ' .level2Group a.f-item').on('click', function() {
                if ($(this).hasClass('select')) { // 先判断是否是选中状态
                  $(this).removeClass('select');
                  if (multiple) { // 多选条件下才同步
                    copyItem(thisBox, selectedBox);
                  }
                } else {
                  if (multiple) {
                    // 判断是否点击的是不限
                    var allCodeArr = $(this).data('code').split('.');
                    if (!eval(allCodeArr[2])) {
                      $(this).parents('.level3Group').find('.f-item').removeClass('select');
                    } else {
                      $(this).parents('.level3Group').find('.f-item').eq(0).removeClass('select');
                    }
                    if (overFlow(thisBox, maxNum)) {
                      $(this).addClass('select');
                      $(this).parents('.level3Group').prev().find('.f-item').addClass('select');
                      var levelIndex = getIndex($(this).closest('.level2Group'), thisBox + ' .level2Group');
                      $(thisBox + ' .level1Group').eq(levelIndex).find('.f-item').addClass('select');
                      copyItem(thisBox, selectedBox);
                    } else {
                      qsToast({context: '最多可选' + maxNum + '个'});
                    }
                  } else {
                    $(thisBox + ' .f-item').removeClass('select');
                    $(this).addClass('select');
                    $(thisBox + ' .level1Group .f-item.active').addClass('select');
                    var qtcode = $(this).data('code');
                    var qttitle = $(this).data('title');
                    $('.qs-temp-txt-' + thistype).text(qttitle);
                    $('.qs-temp-code-' + thistype).val(qtcode);
                    if (thisLink) {
                      if ($(this).hasClass('f-range')) {
                        // 附近
                        $('.qs-temp-code-range').val(qtcode);
                        $('.qs-temp-code-' + thistype).val('');
                      } else {
                        $('.qs-temp-code-range').val('');
                      }
                      clearFilter();
                      goPage();
                    } else {
                      $('.js-actionsheet').removeClass('qs-actionsheet-toggle');
                      $('.qs-mask').fadeOut(200);
                    }
                  }
                }
              })
            } else {
              $(thisBox + ' .level1Link .f-item').eq(0).addClass('active');
            }
          } else {
            // $(thisBox + ' .level1Group .f-item').eq(0).addClass('active');
            $(thisBox + ' .level2Group').eq(0).removeClass(hideClass); // 不恢复默认二级分类的第一个显示出来
          }
        }

        /**
         * 确定
         */
        $('#qs-temp-confirm-' + thistype).on('click', function () {
          var selectedArr = $(selectedBox + ' .s-list-cell');
          var codeArr = new Array();
          var titleArr = new Array();
          $.each(selectedArr, function (key, value) {
            var code = $(this).data('code');
            var title = $(this).data('title');
            codeArr.push(code);
            titleArr.push(title);
          })
          $('.qs-temp-code-' + thistype).val(codeArr.join(','));
          var htxt = '';
          titleArr.length ? htxt = titleArr.join(',') : htxt = $('.qs-temp-txt-' + thistype).data('otxt');
          $('.qs-temp-txt-' + thistype).text(htxt);
        });

        /**
         * 筛选点击一级跳转
         */
        $(thisBox + ' .level1Link a.f-none').on('click', function () {
          var qtcode = $(this).data('code');
          var qttitle = $(this).data('title');
          $('.qs-temp-code-' + thistype).val(qtcode);
          $('.qs-temp-txt-' + thistype).text(qttitle);
          if (qtcode.length == 0) {
            $('.qs-temp-code-range').val(qtcode);
          }
          clearFilter();
          goPage();
        })

        /**
         * 一级分类点击
         */
        $(thisBox + ' .level1Group a.f-item').not('.f-none').on('click', function() {
          $(thisBox + ' .level1Group a.f-item').removeClass('active');
          if (thisLink) {
            $(thisBox + ' .level1Link a.f-item').removeClass('active');
          }
          $(this).addClass('active');
          var thisIndex = getIndex($(this).closest('.level1Group'), thisBox + ' .level1Group');
          var clickTempHtml = '';
          var thisItemCode = $(this).data('code'); // 获取点击的title
          clickTempHtml += '<div class="f-box-inner">';
          if (thisItemCode == 'near') { // 点击的为附近
            clickTempHtml += '<div class="level2Group">';
            clickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="20" data-title="不限">不限</a></li>';
            clickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="1" data-title="1公里">1公里</a></li>';
            clickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="3" data-title="3公里">3公里</a></li>';
            clickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="5" data-title="5公里">5公里</a></li>';
            clickTempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="10" data-title="10公里">10公里</a></li>';
            clickTempHtml += '</div>';
          } else {
            var tempLevel2Str = datasource[thisItemCode];
            if (tempLevel2Str) {
              var tempLevel2Array = tempLevel2Str.split('`');
              if (tempLevel2Array) {
                clickTempHtml += '<div class="level2Group">';
                $.each(tempLevel2Array, function(key, value) {
                  if (value.split(',')) {
                    if (value.split(',')[0]) {
                      clickTempHtml += '<li><a class="f-item" href="javascript:;" data-code="' + value.split(',')[0] + '" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                    }
                  }
                })
                clickTempHtml += '</div>';
              }
            }
          }
          clickTempHtml += '</div>';
          if ($(thisBox + ' .f-box-inner').length > 1) {
            $(thisBox + ' .f-box-inner').eq(1).remove();
          }
          $(thisBox).append(clickTempHtml);
          /**
           * 二级分类点击
           */
          $(thisBox + ' .level2Group a.f-item').on('click', function() {
            if ($(this).hasClass('select')) { // 先判断是否是选中状态
              $(this).removeClass('select');
              if (multiple) { // 多选条件下才同步
                copyItem(thisBox, selectedBox);
              }
            } else {
              if (multiple) {
                // 判断是否点击的是不限
                var allCodeArr = $(this).data('code').split('.');
                if (!eval(allCodeArr[2])) {
                  $(this).parents('.level3Group').find('.f-item').removeClass('select');
                } else {
                  $(this).parents('.level3Group').find('.f-item').eq(0).removeClass('select');
                }
                if (overFlow(thisBox, maxNum)) {
                  $(this).addClass('select');
                  $(this).parents('.level3Group').prev().find('.f-item').addClass('select');
                  var levelIndex = getIndex($(this).closest('.level2Group'), thisBox + ' .level2Group');
                  $(thisBox + ' .level1Group').eq(levelIndex).find('.f-item').addClass('select');
                  copyItem(thisBox, selectedBox);
                } else {
                  qsToast({context: '最多可选' + maxNum + '个'});
                }
              } else {
                $(thisBox + ' .f-item').removeClass('select');
                $(this).addClass('select');
                $(thisBox + ' .level1Group .f-item.active').addClass('select');
                var qtcode = $(this).data('code');
                var qttitle = $(this).data('title');
                $('.qs-temp-txt-' + thistype).text(qttitle);
                $('.qs-temp-code-' + thistype).val(qtcode);
                if (thisLink) {
                  if ($(this).hasClass('f-range')) {
                    // 附近
                    $('.qs-temp-code-range').val(qtcode);
                    $('.qs-temp-code-' + thistype).val('');
                  } else {
                    $('.qs-temp-code-range').val('');
                  }
                  clearFilter();
                  goPage();
                } else {
                  $('.js-actionsheet').removeClass('qs-actionsheet-toggle');
                  $('.qs-mask').fadeOut(200);
                }
              }
            }
          })
        })
      }
    } else { // 不是分站以及分站下除搜索页面之外的
      if (database) {
        var tempHtml = '<div class="f-box-inner">';
        if (thisLink) {
          tempHtml += '<div class="level1Link">';
          tempHtml += '<li><a class="f-item f-none" href="javascript:;" data-code="" data-title="地区">不限</a></li>';
          tempHtml += '</div>';
          if (that.data('range')) {
            tempHtml += '<div class="level1Group">';
            tempHtml += '<li><a class="f-item" href="javascript:;" data-code="" data-title="附近">附近</a></li>';
            tempHtml += '</div>';
          }
        }
        $.each(database, function(key, value) {
          if (value.split(',')) {
            tempHtml += '<div class="level1Group">';
            tempHtml += '<li><a class="f-item" href="javascript:;" data-code="' + value.split(',')[0] + '.0.0" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
            tempHtml += '</div>';
          }
        })
        tempHtml += '</div>';
        tempHtml += '<div class="f-box-inner">';
        if (thisLink) { // 判断是否是筛选
          if (that.data('range')) { // 是否开启附近地区
            tempHtml += '<div class="' + hideClass + ' level2Group">';
            tempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="20" data-title="不限">不限</a></li>';
            tempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="1" data-title="1公里">1公里</a></li>';
            tempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="3" data-title="3公里">3公里</a></li>';
            tempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="5" data-title="5公里">5公里</a></li>';
            tempHtml += '<li><a class="f-item f-range" href="javascript:;" data-code="10" data-title="10公里">10公里</a></li>';
            tempHtml += '</div>';
          }
        }
        for (var i = 0; i < database.length; i++) {
          if (database[i].split(',')) {
            var tempLevel2Str = datasource[database[i].split(',')[0]];
            if (tempLevel2Str) {
              var tempLevel2Array = tempLevel2Str.split('`');
              if (tempLevel2Array) {
                tempHtml += '<div class="' + hideClass + ' level2Group">';
                if (!(tempLevel > 2) && !addJob) {
                    tempHtml += '<li><a class="f-item" href="javascript:;" data-code="' + database[i].split(',')[0] + '.0.0" data-title="' + database[i].split(',')[1] + '">不限</a></li>';
                }
                $.each(tempLevel2Array, function(key, value) {
                  if (value.split(',')) {
                    if (value.split(',')[0]) {
                      if (datasource[value.split(',')[0]]) {
                        if (tempLevel > 2) { // 判断二三级分类
                          var tempLevel3Str = datasource[value.split(',')[0]];
                          if (tempLevel3Str) {
                            var tempLevel3Array = tempLevel3Str.split('`');
                            if (tempLevel3Array) {
                              tempHtml += '<li><a class="f-item c-next" href="javascript:;" data-code="' + database[i].split(',')[0] + '.' + value.split(',')[0] + '.0" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                              tempHtml += '<div class="' + hideClass + ' level3Group">';
                              if (!addJob) {
                                  tempHtml += '<li class="l-next"><a class="font12 f-item" href="javascript:;" data-code="' + database[i].split(',')[0] + '.' + value.split(',')[0] + '.0" data-title="' + value.split(',')[1] + '">不限</a></li>';
                              }
                              $.each(tempLevel3Array, function (keyNext, valueNext) {
                                if (valueNext.split(',')[0]) {
                                  tempHtml += '<li class="l-next"><a class="font12 f-item" href="javascript:;" data-code="' + database[i].split(',')[0] + '.' + value.split(',')[0] + '.' + valueNext.split(',')[0] +'" data-title="' + valueNext.split(',')[1] + '">' + valueNext.split(',')[1] + '</a></li>';
                                }
                              })
                              tempHtml += '</div>';
                            }
                          }
                        } else {
                          tempHtml += '<li><a class="f-item" href="javascript:;" data-code="' + database[i].split(',')[0] + '.' + value.split(',')[0] + '.0" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                        }
                      } else {
                         if (tempLevel > 2 && addJob) { // 三级分类下无最后一级分类
                             tempHtml += '<li><a class="f-item c-next" href="javascript:;" data-code="' + database[i].split(',')[0] + '.' + value.split(',')[0] + '.0" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                             tempHtml += '<div class="' + hideClass + ' level3Group">';
                             tempHtml += '<li class="l-next"><a class="font12" href="javascript:;">无子分类！</a></li>';
                             tempHtml += '</div>';
                         } else {
                             tempHtml += '<li><a class="f-item" href="javascript:;" data-code="' + database[i].split(',')[0] + '.' + value.split(',')[0] + '.0" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                         }
                      }
                    }
                  }
                })
                tempHtml += '</div>';
              }
            } else {
                tempHtml += '<div class="' + hideClass + ' level2Group">';
                if (tempLevel > 2) {
                    tempHtml += '<li><a class="" href="javascript:;">无子分类！</a></li>';
                } else {
                    if (addJob) {
                        tempHtml += '<li><a class="" href="javascript:;">无子分类！</a></li>';
                    } else {
                        tempHtml += '<li><a class="f-item" href="javascript:;" data-code="' + database[i].split(',')[0] + '.0.0" data-title="' + database[i].split(',')[1] + '">不限</a></li>';
                    }
                }
                tempHtml += '</div>';
            }
          }
        }
        tempHtml += '</div>';
        $(thisBox).html(tempHtml);

        var rvalue = $('.qs-temp-code-' + thistype).val();
        if (rvalue.length) { // 恢复选中
          var rvalueArr = rvalue.split(',');
          var itemArr = $(thisBox + ' .level2Group a.f-item').not('.c-next');
          $.each(rvalueArr, function (key, value) {
            $.each(itemArr, function () {
              if ($(this).data('code') == value) {
                $(this).addClass('select');
                $(this).parents('.level3Group').prev().find('.f-item').addClass('active select');
                $(this).parents('.level3Group').removeClass(hideClass);
                $(this).parents('.level2Group').removeClass(hideClass);
                var tindex = getIndex($(this).parents('.level2Group'), thisBox + ' .level2Group');
                $(thisBox + ' .level1Group').eq(tindex).find('.f-item').addClass('select');
              }
            })
          })
          var firstSelectedItem = $(thisBox + ' .level2Group a.select').not('.c-next').eq(0);
          firstSelectedItem.parents('.level3Group').prev().find('.f-item').addClass('active select');
          firstSelectedItem.parents('.level3Group').removeClass(hideClass);
          firstSelectedItem.parents('.level2Group').removeClass(hideClass);
          var rindex = getIndex(firstSelectedItem.parents('.level2Group'), thisBox + ' .level2Group');
          $(thisBox + ' .level1Group').eq(rindex).find('.f-item').addClass('select');
          if (multiple) {
            copyItem(thisBox, selectedBox);
          }
        } else {
          if (thisLink) {
            // 判断是否是筛选
            var rangeValue = $('.qs-temp-code-range').val();
            if (rangeValue.length) {
              $(thisBox + ' .level1Group').eq(0).find('.f-item').addClass('select');
              $(thisBox + ' .level2Group').eq(0).removeClass(hideClass);
              var recoverRangeItemArr = $(thisBox + ' .level2Group').eq(0).find('.f-item');
              $.each(recoverRangeItemArr, function () {
                if ($(this).data('code') == rangeValue) {
                  $(this).addClass('select');
                }
              })
            } else {
              $(thisBox + ' .level1Link .f-item').eq(0).addClass('active');
            }
          } else {
            // $(thisBox + ' .level1Group .f-item').eq(0).addClass('active');
            $(thisBox + ' .level2Group').eq(0).removeClass(hideClass); // 不恢复默认二级分类的第一个显示出来
          }
        }

        /**
         * 确定
         */
        $('#qs-temp-confirm-' + thistype).on('click', function () {
          var selectedArr = $(selectedBox + ' .s-list-cell');
          var codeArr = new Array();
          var titleArr = new Array();
          $.each(selectedArr, function (key, value) {
            var code = $(this).data('code');
            var title = $(this).data('title');
            codeArr.push(code);
            titleArr.push(title);
          })
          $('.qs-temp-code-' + thistype).val(codeArr.join(','));
          var htxt = '';
          titleArr.length ? htxt = titleArr.join(',') : htxt = $('.qs-temp-txt-' + thistype).data('otxt');
          $('.qs-temp-txt-' + thistype).text(htxt);
        });

        /**
         * 筛选点击不限
         */
        $(thisBox + ' .level1Link a.f-none').on('click', function () {
          var qtcode = $(this).data('code');
          var qttitle = $(this).data('title');
          $('.qs-temp-code-' + thistype).val(qtcode);
          $('.qs-temp-txt-' + thistype).text(qttitle);
          $('.qs-temp-code-range').val(qtcode);
          $('#lat').val('');
          $('#lng').val('');
          clearFilter();
          goPage();
        })

        /**
         * 一级分类点击
         */
        $(thisBox + ' .level1Group a.f-item').not('.f-none').on('click', function() {
          $(thisBox + ' .level1Group a.f-item').removeClass('active');
          if (thisLink) {
            $(thisBox + ' .level1Link a.f-item').removeClass('active');
          }
          $(this).addClass('active');
          var thisIndex = getIndex($(this).closest('.level1Group'), thisBox + ' .level1Group');
          $(thisBox + ' .level2Group').addClass(hideClass);
          $(thisBox + ' .level2Group').eq(thisIndex).removeClass(hideClass);
        })

        /**
         * 二三级分类点击
         */
        $(thisBox + ' .level2Group a.f-item').on('click', function() {
          if ($(this).hasClass('c-next')) { // 如果是二级分类
            if ($(this).hasClass('active')) { // 是否是展开状态
              $(this).closest('li').next().addClass(hideClass);
              $(this).removeClass('active');
            } else {
              $('.level2Group').find('a.c-next').removeClass('active');
              $(this).addClass('active');
              $(thisBox + ' .level3Group').addClass(hideClass);
              $(this).parents('li').next().removeClass(hideClass);
            }
          } else { // 三级分类
            if ($(this).hasClass('select')) { // 先判断是否是选中状态
              $(this).removeClass('select');
              if (!$(this).parents('.level3Group').find('.select').length) {
                $(this).parents('.level3Group').prev().find('.f-item').removeClass('select');
                if (!$(this).closest('.level2Group').find('.select').length) {
                  var levelIndex = getIndex($(this).closest('.level2Group'), thisBox + ' .level2Group');
                  $(thisBox + ' .level1Group').eq(levelIndex).find('.f-item').removeClass('select');
                }
              }
              if (multiple) { // 多选条件下才同步
                copyItem(thisBox, selectedBox);
              }
            } else {
              if (multiple) {
                // 判断是否点击的是不限
                var allCodeArr = $(this).data('code').split('.');
                if (!eval(allCodeArr[2])) {
                  $(this).parents('.level3Group').find('.f-item').removeClass('select');
                } else {
                  $(this).parents('.level3Group').find('.f-item').eq(0).removeClass('select');
                }
                if (overFlow(thisBox, maxNum)) {
                  $(this).addClass('select');
                  $(this).parents('.level3Group').prev().find('.f-item').addClass('select');
                  var levelIndex = getIndex($(this).closest('.level2Group'), thisBox + ' .level2Group');
                  $(thisBox + ' .level1Group').eq(levelIndex).find('.f-item').addClass('select');
                  copyItem(thisBox, selectedBox);
                } else {
                  qsToast({context: '最多可选' + maxNum + '个'});
                }
              } else {
                $(thisBox + ' .f-item').removeClass('select');
                $(this).addClass('select');
                $(this).parents('.level3Group').prev().find('.f-item').addClass('select');
                $(thisBox + ' .level1Group .f-item.active').addClass('select');
                var qtcode = $(this).data('code');
                var qttitle = $(this).data('title');
                $('.qs-temp-txt-' + thistype).text(qttitle);
                $('.qs-temp-code-' + thistype).val(qtcode);
                if (thisLink) {
                  if ($(this).hasClass('f-range')) {
                    // 附近
                    $('.qs-temp-code-range').val(qtcode);
                    $('.qs-temp-code-' + thistype).val('');
                  } else {
                    $('.qs-temp-code-range').val('');
                  }
                  clearFilter();
                  goPage();
                } else {
                  $('.js-actionsheet').removeClass('qs-actionsheet-toggle');
                  $('.qs-mask').fadeOut(200);
                }
              }
            }
          }
        })
      }
    }

    /**
     * 清除筛选
     */
    function clearFilter() {
      $('body').removeClass('filter-fixed');
      $(thisBox).addClass(hideClass);
      $('#f-mask').hide();
      $('.qs-temp').removeClass('active');
    }
  })

  /**
   * 同步
   * @param container 当前容器
   * @param selectedContainer 显示选中的容器
   */
  function copyItem(container, selectedContainer) {
    var selectedArr = $(container + ' .level2Group a.select').not('.c-next');
    var selectedHtml = '';
    if (selectedArr.length) {
      $.each(selectedArr, function(key, value) {
        var code = $(this).data('code');
        var title = $(this).data('title');
        selectedHtml += '<div class="s-list-cell" data-code="' + code + '" data-title="' + title + '">' + title + '</div>';
      })
      $(selectedContainer + ' .s-list').html(selectedHtml);
      $(selectedContainer + ' .s-list').removeClass(hideClass);
    } else {
      $(selectedContainer + ' .s-list').html(selectedHtml);
      $(selectedContainer + ' .s-list').addClass(hideClass);
      return false;
    }

    // 已选列表点击
    $(selectedContainer + ' .s-list-cell').on('click', function () {
      var code = $(this).data('code');
      $.each(selectedArr, function () {
        var concode = $(this).data('code');
        if (concode == code) {
          $(this).removeClass('select');
          if (!$(this).parents('.level3Group').find('.select').length) {
            $(this).parents('.level3Group').prev().find('.f-item').removeClass('select');
            if (!$(this).closest('.level2Group').find('.select').length) {
              var levelIndex = getIndex($(this).closest('.level2Group'), container + ' .level2Group');
              $(container + ' .level1Group').eq(levelIndex).find('.f-item').removeClass('select');
            }
          }
        }
      })
      copyItem(container, selectedContainer);
    })
  }

  /**
   * 获取当前对象的下标
   * @param obj 当前对象
   * @param container 当前容器
   * @returns {*|jQuery} 下标
   */
  function getIndex(obj, container) {
    return $(container).index(obj);
  }

  /**
   * 选中项目的数量是否超出最大限制
   * @param container 当前容器
   * @param num 最大数量
   * @returns {boolean} 是否可以继续选择
   */
  function overFlow(container, num) {
    return $(container + ' .level2Group a.select').not('.c-next').length >= num ? false : true;
  }

  /**
   * 只有一级分类
   */
  var tempLevel1Arr = $('.qs-temp-level1');
  $.each(tempLevel1Arr, function () {
    var thistype = $(this).data('type');
    var database = eval($(this).data('base'));
    var multiple = eval($(this).data('multiple')); // 多选单选
    var maxNum =$(this).data('num');
    var thisBox = '.f-box-' + thistype;
    var selectedBox = '.f-selected-group-' + thistype;
    var thisLink = eval($(this).data('link'));

    if (database) {
      var tempHtml = '<div class="f-box-inner">';
      $.each(database, function(key, value) {
        if (value.split(',')) {
          tempHtml += '<li><a class="font12 f-item f-item-only" href="javascript:;" data-code="' + value.split(',')[0] + '" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
        }
      })
      tempHtml += '</div>';
      $(thisBox).html(tempHtml);

      /**
       * 恢复选中
       */
      var rvalue = $('.qs-temp-code-' + thistype).val();
      if (rvalue.length) {
        var rvalueArr = rvalue.split(',');
        var itemArr = $(thisBox + ' .f-item-only');
        $.each(rvalueArr, function (key, value) {
          $.each(itemArr, function () {
            if ($(this).data('code') == value) {
              $(this).addClass('select');
            }
          })
        })
        if (multiple) {
          copyItemOnly(thisBox, selectedBox);
        }
      }

      /**
       * 分类点击
       */
      $(thisBox + ' .f-item-only').on('click', function () {
        if ($(this).hasClass('select')) {
          $(this).removeClass('select');
          if (multiple) { // 多选条件下才同步
            copyItemOnly(thisBox, selectedBox);
          }
        } else {
          if (multiple) {
            if (overFlowOnly(thisBox, maxNum)) {
              $(this).addClass('select');
              copyItemOnly(thisBox, selectedBox);
            } else {
              qsToast({context: '最多可选' + maxNum + '个'});
            }
          } else {
            $(thisBox + ' .f-item-only').removeClass('select');
            $(this).addClass('select');
            var qtcode = $(this).data('code');
            var qttitle = $(this).data('title');
            $('.qs-temp-code-' + thistype).val(qtcode);
            $('.qs-temp-txt-' + thistype).text(qttitle);
            $('.js-actionsheet').removeClass('qs-actionsheet-toggle');
            $('.qs-mask').fadeOut(200);
          }
        }
      })
      /**
       * 确定
       */
      $('#qs-temp-confirm-' + thistype).on('click', function () {
        var selectedArr = $(selectedBox + ' .s-list-cell');
        var codeArr = new Array();
        var titleArr = new Array();
        $.each(selectedArr, function (key, value) {
          var code = $(this).data('code');
          var title = $(this).data('title');
          codeArr.push(code);
          titleArr.push(title);
        })
        $('.qs-temp-code-' + thistype).val(codeArr.join(','));
        var htxt = '';
        titleArr.length ? htxt = titleArr.join(',') : htxt = $('.qs-temp-txt-' + thistype).data('otxt');
        $('.qs-temp-txt-' + thistype).text(htxt);
      });
    }
  })

  /**
   * 红包多选
   */
  var tempLevelAlwArr = $('.qs-temp-level-alw');
  $.each(tempLevelAlwArr, function () {
    var thistype = $(this).data('type');
    var database = eval($(this).data('base'));
    var multiple = eval($(this).data('multiple')); // 多选单选
    var maxNum =$(this).data('num');
    var thisBox = '.f-box-' + thistype;
    var selectedBox = '.f-selected-group-' + thistype;
    var thisLink = eval($(this).data('link'));

    if (database) {
      var tempHtml = '<div class="f-box-inner">';
      tempHtml += '<li><a class="font12 f-item f-item-only only-nm" href="javascript:;" data-code="0" data-title="不限">不限</a></li>';
      $.each(database, function(key, value) {
        if (value.split(',')) {
          tempHtml += '<li><a class="font12 f-item f-item-only" href="javascript:;" data-code="' + value.split(',')[0] + '" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
        }
      })
      tempHtml += '</div>';
      $(thisBox).html(tempHtml);

      /**
       * 恢复选中
       */
      var rvalue = $('.qs-temp-code-' + thistype).val();
      if (rvalue.length) {
        var rvalueArr = rvalue.split(',');
        var itemArr = $(thisBox + ' .f-item-only');
        $.each(rvalueArr, function (key, value) {
          $.each(itemArr, function () {
            if ($(this).data('code') == value) {
              $(this).addClass('select');
            }
          })
        })
        if (multiple) {
          copyItemOnly(thisBox, selectedBox);
        }
      }

      /**
       * 分类点击
       */
      $(thisBox + ' .f-item-only').on('click', function () {
        var thisCode = $(this).data('code');
        if ($(this).hasClass('select')) {
          $(this).removeClass('select');
          $(thisBox + ' .only-nm').removeClass('select');
          if (!eval(thisCode)) {
            // 不限
            $(thisBox + ' .f-item-only').each(function(index, el) {
              if (index) {
                $(this).removeClass('select');
              }
            })
          }
          if (multiple) { // 多选条件下才同步
            copyItemOnly(thisBox, selectedBox);
          }
        } else {
          if (multiple) {
            if (overFlowOnly(thisBox, maxNum)) {
              $(this).addClass('select');
              if (!eval(thisCode)) {
                // 不限
                $(thisBox + ' .f-item-only').each(function(index, el) {
                  if (index) {
                    $(this).addClass('select');
                  }
                })
              }
              var othersArray = $(thisBox + ' .f-item-only');
              var thisIndex = $(thisBox + ' .f-item-only').index($(this));
              var checkedNum = $(thisBox + ' .f-item-only.select').length;
              if (checkedNum <= 1) {
                $.each(othersArray, function (index, value) {
                  if (index > thisIndex) {
                    $(this).addClass('select');
                  }
                })
              }
              copyItemOnly(thisBox, selectedBox);
            } else {
              qsToast({context: '最多可选' + maxNum + '个'});
            }
          } else {
            $(thisBox + ' .f-item-only').removeClass('select');
            $(this).addClass('select');
            var qtcode = $(this).data('code');
            var qttitle = $(this).data('title');
            $('.qs-temp-code-' + thistype).val(qtcode);
            $('.qs-temp-txt-' + thistype).text(qttitle);
            $('.js-actionsheet').removeClass('qs-actionsheet-toggle');
            $('.qs-mask').fadeOut(200);
          }
        }
      })
      /**
       * 确定
       */
      $('#qs-temp-confirm-' + thistype).on('click', function () {
        var selectedArr = $(selectedBox + ' .s-list-cell');
        var codeArr = new Array();
        var titleArr = new Array();
        $.each(selectedArr, function (key, value) {
          var code = $(this).data('code');
          var title = $(this).data('title');
          codeArr.push(code);
          titleArr.push(title);
        })
        $('.qs-temp-code-' + thistype).val(codeArr.join(','));
        var htxt = '';
        titleArr.length ? htxt = titleArr.join(',') : htxt = $('.qs-temp-txt-' + thistype).data('otxt');
        $('.qs-temp-txt-' + thistype).text(htxt);
      });
    }
  })

  /**
   * 选中项目的数量是否超出最大限制
   * @param container 当前容器
   * @param num 最大数量
   * @returns {boolean} 是否可以继续选择
   */
  function overFlowOnly(container, num) {
    return $(container + ' .f-item-only.select').length >= num ? false : true;
  }

  /**
   * 同步
   * @param container 当前容器
   * @param selectedContainer 显示选中的容器
   */
  function copyItemOnly(container, selectedContainer) {
    var selectedArr = $(container + ' .f-item-only.select');
    var selectedHtml = '';
    if (selectedArr.length) {
      $.each(selectedArr, function(key, value) {
        if (!$(this).hasClass('only-nm')) {
          var code = $(this).data('code');
          var title = $(this).data('title');
          selectedHtml += '<div class="s-list-cell" data-code="' + code + '" data-title="' + title + '">' + title + '</div>';
        }
      })
      $(selectedContainer + ' .s-list').html(selectedHtml);
      $(selectedContainer + ' .s-list').removeClass(hideClass);
    } else {
      $(selectedContainer + ' .s-list').html(selectedHtml);
      $(selectedContainer + ' .s-list').addClass(hideClass);
      return false;
    }

    // 已选列表点击
    $(selectedContainer + ' .s-list-cell').on('click', function () {
      var code = $(this).data('code');
      $.each(selectedArr, function () {
        var concode = $(this).data('code');
        if (concode == code) {
          $(this).removeClass('select');
        }
      })
      copyItemOnly(container, selectedContainer);
    })
  }

  /**
   * 专业分类
   */
  var tempLevelmajorArr = $('.qs-temp-level-major');
  $.each(tempLevelmajorArr, function() {
    var thistype = $(this).data('type');
    var database = eval($(this).data('base'));
    var datasource = eval($(this).data('source'));
    var multiple = eval($(this).data('multiple')); // 多选单选
    var maxNum =$(this).data('num');
    var thisBox = '.f-box-' + thistype;
    var selectedBox = '.f-selected-group-' + thistype;
    var thisLink = eval($(this).data('link'));

    if (database) {
      var tempHtml = '<div class="f-box-inner">';
      $.each(database, function(key, value) {
        if (value.split(',')) {
          tempHtml += '<div class="level1Group">';
          tempHtml += '<li><a class="font12 f-item" href="javascript:;" data-code="' + value.split(',')[0] + '.0.0" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
          tempHtml += '</div>';
        }
      })
      tempHtml += '</div>';
      tempHtml += '<div class="f-box-inner">';
      for (var i = 0; i < database.length; i++) {
        if (database[i].split(',')) {
          var tempLevel2Str = datasource[database[i].split(',')[0]];
          if (tempLevel2Str) {
            var tempLevel2Array = tempLevel2Str.split('`');
            if (tempLevel2Array) {
              tempHtml += '<div class="' + hideClass + ' level2Group">';
              $.each(tempLevel2Array, function(key, value) {
                if (value.split(',')) {
                  if (value.split(',')[0]) {
                    tempHtml += '<li><a class="font12 f-item" href="javascript:;" data-code="' + value.split(',')[0] + '" data-title="' + value.split(',')[1] + '">' + value.split(',')[1] + '</a></li>';
                  }
                }
              })
              tempHtml += '</div>';
            }
          }
        }
      }
      tempHtml += '</div>';
      $(thisBox).html(tempHtml);

      var rvalue = $('.qs-temp-code-' + thistype).val();
      if (rvalue.length) { // 恢复选中
        var rvalueArr = rvalue.split(',');
        var itemArr = $(thisBox + ' .level2Group a.f-item').not('.c-next');
        $.each(rvalueArr, function (key, value) {
          $.each(itemArr, function () {
            if ($(this).data('code') == value) {
              $(this).addClass('select');
              $(this).parents('.level2Group').removeClass(hideClass);
              var tindex = getIndex($(this).parents('.level2Group'), thisBox + ' .level2Group');
              $(thisBox + ' .level1Group').eq(tindex).find('.f-item').addClass('select');
            }
          })
        })
        var firstSelectedItem = $(thisBox + ' .level2Group a.select').not('.c-next').eq(0);
        firstSelectedItem.parents('.level2Group').removeClass(hideClass);
        var rindex = getIndex(firstSelectedItem.parents('.level2Group'), thisBox + ' .level2Group');
        $(thisBox + ' .level1Group').eq(rindex).find('.f-item').addClass('select');
        if (multiple) {
          copyItem(thisBox, selectedBox);
        }
      } else {
        $(thisBox + ' .level1Group .f-item').eq(0).addClass('active');
        $(thisBox + ' .level2Group').eq(0).removeClass(hideClass); // 不恢复默认二级分类的第一个显示出来
      }

      /**
       * 确定
       */
      $('#qs-temp-confirm-' + thistype).on('click', function () {
        var selectedArr = $(selectedBox + ' .s-list-cell');
        var codeArr = new Array();
        var titleArr = new Array();
        $.each(selectedArr, function (key, value) {
          var code = $(this).data('code');
          var title = $(this).data('title');
          codeArr.push(code);
          titleArr.push(title);
        })
        $('.qs-temp-code-' + thistype).val(codeArr.join(','));
        var htxt = '';
        titleArr.length ? htxt = titleArr.join(',') : htxt = $('.qs-temp-txt-' + thistype).data('otxt');
        $('.qs-temp-txt-' + thistype).text(htxt);
      });

      /**
       * 一级分类点击
       */
      $(thisBox + ' .level1Group a.f-item').not('.f-none').on('click', function() {
        $(thisBox + ' .level1Group a.f-item').removeClass('active');
        $(this).addClass('active');
        var thisIndex = getIndex($(this).closest('.level1Group'), thisBox + ' .level1Group');
        $(thisBox + ' .level2Group').addClass(hideClass);
        $(thisBox + ' .level2Group').eq(thisIndex).removeClass(hideClass);
      })

      /**
       * 二三级分类点击
       */
      $(thisBox + ' .level2Group a.f-item').on('click', function() {
        if ($(this).hasClass('c-next')) { // 如果是二级分类
          if ($(this).hasClass('active')) { // 是否是展开状态
            $(this).closest('li').next().addClass(hideClass);
            $(this).removeClass('active');
          } else {
            $(this).closest('.level2Group').find('a.c-next').removeClass('active');
            $(this).addClass('active');
            $(thisBox + ' .level3Group').addClass(hideClass);
            $(this).parents('li').next().removeClass(hideClass);
          }
        } else { // 三级分类
          if ($(this).hasClass('select')) { // 先判断是否是选中状态
            $(this).removeClass('select');
            if (!$(this).parents('.level3Group').find('.select').length) {
              $(this).parents('.level3Group').prev().find('.f-item').removeClass('select');
              if (!$(this).closest('.level2Group').find('.select').length) {
                var levelIndex = getIndex($(this).closest('.level2Group'), thisBox + ' .level2Group');
                $(thisBox + ' .level1Group').eq(levelIndex).find('.f-item').removeClass('select');
              }
            }
            if (multiple) { // 多选条件下才同步
              copyItem(thisBox, selectedBox);
            }
          } else {
            if (multiple) {
              // 判断是否点击的是不限
              var allCodeArr = $(this).data('code').split('.');
              if (!eval(allCodeArr[2])) {
                $(this).parents('.level3Group').find('.f-item').removeClass('select');
              } else {
                $(this).parents('.level3Group').find('.f-item').eq(0).removeClass('select');
              }
              if (overFlow(thisBox, maxNum)) {
                $(this).addClass('select');
                $(this).parents('.level3Group').prev().find('.f-item').addClass('select');
                var levelIndex = getIndex($(this).closest('.level2Group'), thisBox + ' .level2Group');
                $(thisBox + ' .level1Group').eq(levelIndex).find('.f-item').addClass('select');
                copyItem(thisBox, selectedBox);
              } else {
                qsToast({context: '最多可选' + maxNum + '个'});
              }
            } else {
              $(thisBox + ' .f-item').removeClass('select');
              $(this).addClass('select');
              $(this).parents('.level3Group').prev().find('.f-item').addClass('select');
              $(thisBox + ' .level1Group .f-item.active').addClass('select');
              var qtcode = $(this).data('code');
              var qttitle = $(this).data('title');
              $('.qs-temp-code-' + thistype).val(qtcode);
              $('.qs-temp-txt-' + thistype).text(qttitle);
              if (thisLink) {
                clearFilter();
                goPage();
              } else {
                $('.js-actionsheet').removeClass('qs-actionsheet-toggle');
                $('.qs-mask').fadeOut(200);
              }
            }
          }
        }
      })
    }

    /**
     * 清除筛选
     */
    function clearFilter() {
      $('body').removeClass('filter-fixed');
      $(thisBox).addClass(hideClass);
      $('#f-mask').hide();
      $('.qs-temp').removeClass('active');
    }
  })
});