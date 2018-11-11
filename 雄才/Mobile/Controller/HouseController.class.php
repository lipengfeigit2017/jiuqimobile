<?php
namespace Mobile\Controller;

use Mobile\Controller\MobileController;

class HouseController extends MobileController {
    public $authentication_user;
    public $cache_apply_info;

    // 初始化函数
    public function _initialize() {
        parent::_initialize();
        $this->authentication_user = session('authentication_user') ? session('authentication_user') : cookie('authentication_user');
        $this->cache_apply_info = session('cache_apply_info') ? session('cache_apply_info') : cookie('cache_apply_info');
    }

    /**
     * 出租信息首页
     */
    public function index() {
        $rent_arr = array('0-500' => '500元以下', '500-1000' => '500-1000元', '1000-1500' => '1000-1500元', '1500-2000' => '1500-2000元', '2000-3000' => '2000-3000元', '3000-5000' => '3000-5000元', '5000' => '5000元以上');
        $map = array();
        $key = I('get.key', '', 'trim');
        $district = I('get.district', '', 'trim');
        $house = I('get.house', 0, 'intval');
        $rent = I('get.rent', '', 'trim');
        if ($district) {
            $district_info = get_city_info($district);
            $district_id = $district_info['district'];
            $district_id_arr = explode(".", $district_id);
            $map['district' . count($district_id_arr)] = array('eq', $district_id);
        } else {
            if (C('SUBSITE_VAL.s_id') > 0) {
                $district_info = get_city_info(C('SUBSITE_VAL.s_district'));
                $district_id = $district_info['district'];
                $district_id_arr = explode(".", $district_id);
                $map['district' . count($district_id_arr)] = array('eq', $district_id);
            }
        }
        if ($rent) {
            if (stripos($rent, '-') === false) {
                $map['rent'] = array('egt', $rent);
            } else {
                $sub_rent_arr = explode("-", $rent);
                $map['rent'] = array(array('egt', $sub_rent_arr[0]), array('lt', $sub_rent_arr[1]), 'and');
            }
        }
        $key && $map['key'] = array('like', '%' . $key . '%');
        $house && $map['house'] = array('eq', $house);
        $total = M('HouseRentSearch')->where($map)->count();
        $pager = pager($total, 10);
        $page = $pager->fshow();
        $limit = $pager->firstRow . ',' . $pager->listRows;
        $ids = M('HouseRentSearch')->where($map)->limit($limit)->order('refreshtime desc,id desc')->getField('id', true);
        if ($ids) {
            $list = D('House/HouseRent')->where(array('id' => array('in', $ids)))->order('refreshtime desc,id desc')->select();
        } else {
            $list = array();
        }
        $category_house = D('House/HouseCategory')->get_category_cache('QS_house');
        $this->assign('category_house', $category_house);
        $this->assign('rent_arr', $rent_arr);
        $this->assign('infolist', $list);
        $this->assign('page', $page);
        $this->_config_seo(array('title' => '房屋出租 - ' . C('qscms_site_name'), 'header_title' => '房屋出租'));
        $this->display();
    }

    /**
     * 信息管理
     * @param String $type 类型
     */
    public function manage($type= 'rent') {
        $this->check_auth();
        if ($type == 'rent'){
            $class = 'House/HouseRent';
            $tpl = 'manage_rent';
        } else {
            $class = 'House/HouseSeek';
            $tpl = 'manage_seek';
        }
        $model = D($class);
        $map['uid'] = $this->authentication_user['uid'];
        $total = $model->where($map)->count();
        $pager = pager($total, 10);
        $page = $pager->fshow();
        $limit = $pager->firstRow . ',' . $pager->listRows;
        $infolist = $model->where($map)->limit($limit)->order('refreshtime desc,id desc')->select();
        $audit_status_cn = $model->audit_status;
        $this->assign('infolist', $infolist);
        $this->assign('page', $page);
        $this->assign('audit_status_cn', $audit_status_cn);
        $this->_config_seo(array('title' => '租房信息管理 - ' . C('qscms_site_name'), 'header_title' => '租房信息管理'));
        $this->display($tpl);
    }

    /**
     * 发布出租信息
     */
    public function add_rent() {
        if (IS_POST) {
            $post_data = I('post.');
            if (!$this->authentication_user) {
                $this->auth_mobile_code();
                $post_data['uid'] = $this->authentication_user['uid'];
            } else {
                $auth_info = D('Members')->where(array('mobile' => $this->authentication_user['mobile'], 'secretkey' => $this->authentication_user['secretkey']))->find();
                if (!$auth_info) {
                    $this->ajaxReturn(0, '请先验证身份！');
                } else {
                    $post_data['uid'] = $this->authentication_user['uid'];
                }
            }
            $count = D('House/HouseRent')->where(array('uid' => $this->authentication_user['uid']))->count();
            if ($count >= C('qscms_house_max')) {
                $this->ajaxReturn(0, '你已发布的信息数已超出限制！');
            }
            if (C('SUBSITE_VAL.s_id') > 0) {
                $post_data['subsite_id'] = C('SUBSITE_VAL.s_id');
            }
            if (false === $reg = D('House/HouseRent')->create($post_data)) {
                $this->ajaxReturn(0, D('House/HouseRent')->getError());
            } else {
                if (false === $insertid = D('House/HouseRent')->add($reg)) {
                    $this->ajaxReturn(0, D('House/HouseRent')->getError());
                } else {
                    $house_imgid_tmp = session('?house_imgid_tmp') ? session('house_imgid_tmp') : array();
                    if (!empty($house_imgid_tmp)) {
                        M('HouseRentImg')->where(array('id' => array('in', $house_imgid_tmp)))->save(array('pid' => $insertid, 'uid' => $post_data['uid'], 'display' => 1));
                    }
                    session('house_imgid_tmp', null);
                    $this->ajaxReturn(1, '发布成功！', array('url' => U('House/manage',array('type'=>'rent'))));
                }
            }
        } else {
            session('house_imgid_tmp', null);
            $count = D('House/HouseRent')->where(array('uid' => $this->authentication_user['uid']))->count();
            if ($count >= C('qscms_house_max')) {
                exit('你已发布的信息数已超出限制！');
            }
            $category = D('House/HouseCategory')->get_category_cache();
            $this->assign('need_mobile_audit', $this->authentication_user ? 0 : 1);
            $this->assign('contact', $this->authentication_user['contact']);
            $this->assign('mobile', $this->authentication_user['mobile']);
            $this->assign('new_record', 1);
            $this->assign('category', $category);
            $this->assign('leave_info_num', C('qscms_house_max') - $count);
            $this->_config_seo(array('title' => '发布房屋出租信息 - ' . C('qscms_site_name'), 'header_title' => '发布房屋出租信息'));
            $this->display();
        }
    }

    /**
     * 修改出租信息
     */
    public function edit_rent($id) {
        $model = D('House/HouseRent');
        if (IS_POST) {
            $post_data = I('post.');
            if (!$this->authentication_user) {
                $this->ajaxReturn(0, '请先验证身份！');
            } else {
                $auth_info = D('Members')->where(array('mobile' => $this->authentication_user['mobile'], 'secretkey' => $this->authentication_user['secretkey']))->find();
                if (!$auth_info) {
                    $this->ajaxReturn(0, '请先验证身份！');
                }
            }
            if (C('qscms_audit_edit_house') == '1') {
                $post_data['audit'] = $model::AUDIT_WAIT;
            }
            if (false === $reg = $model->create($post_data)) {
                $this->ajaxReturn(0, $model->getError());
            } else {
                if (false === $model->save($reg)) {
                    $this->ajaxReturn(0, $model->getError());
                } else {
                    $house_imgid_tmp = session('?house_imgid_tmp') ? session('house_imgid_tmp') : array();
                    if (!empty($house_imgid_tmp)) {
                        M('HouseRentImg')->where(array('id' => array('in', $house_imgid_tmp)))->save(array('pid' => $reg['id'], 'uid' => $this->authentication_user['uid'], 'display' => 1));
                    }
                    session('house_imgid_tmp', null);
                    $model->update_search($reg['id']);
                    $this->ajaxReturn(1, '修改成功！', array('url' => U('House/manage',array('type'=>'rent'))));
                }
            }
        } else {
            $info = $model->where(array('id' => $id, 'uid' => $this->authentication_user['uid']))->find();
            $tags = $info['tag'] ? explode(",", $info['tag']) : array();
            $tagArr = array('id' => array(), 'cn' => array());
            if (!empty($tags)) {
                foreach ($tags as $key => $value) {
                    $arr = explode("|", $value);
                    $tagArr['id'][] = $arr[0];
                    $tagArr['cn'][] = $arr[1];
                }
            }
            $tagStr = array('id' => '', 'cn' => '');
            if (!empty($tagArr['id']) && !empty($tagArr['cn'])) {
                $tagStr['id'] = implode(",", $tagArr['id']);
                $tagStr['cn'] = implode(",", $tagArr['cn']);
            }
            $this->assign('tagArr', $tagArr);
            $this->assign('tagStr', $tagStr);
            session('house_imgid_tmp', null);
            $this->check_auth();
            $category = D('House/HouseCategory')->get_category_cache();
            $rent_img = M('HouseRentImg')->where(array('pid' => $info['id']))->select();
            $this->assign('mobile', $this->authentication_user['mobile']);
            $this->assign('new_record', 0);
            $this->assign('info', $info);
            $this->assign('rent_img', $rent_img);
            $this->assign('category', $category);
            $this->_config_seo(array('title' => '修改出租信息 - ' . C('qscms_site_name'), 'header_title' => '修改出租信息'));
            $this->display('add_rent');
        }
    }

    /**
     * 出租信息详情
     */
    public function show_rent($id) {
        $model = D('House/HouseRent');
        $info = $model->find($id);
        if (!$info) {
            $controller = new \Common\Controller\BaseController;
            $controller->_empty();
        }
        $model->where(array('id' => $id))->setInc('click', 1);
        if (C('qscms_contact_img_house') == 2) {
            $info['show_mobile'] = '<img src="' . C('qscms_site_domain') . U('Home/Qrcode/get_font_img', array('str' => encrypt($info['mobile'], C('PWDHASH')))) . '"/>';
        } else {
            $info['show_mobile'] = $info['mobile'];
        }
        $tags = $info['tag'] ? explode(",", $info['tag']) : array();
        $tagArr = array();
        if (!empty($tags)) {
            foreach ($tags as $key => $value) {
                $arr = explode("|", $value);
                $tagArr[] = $arr[1];
            }
        }
        $info['tag'] = $tagArr;
        $recommend_map['id'] = array('neq', $id);
        $recommend_map['category'] = array('eq', $info['category']);
        $recommend_map['audit'] = $model::AUDIT_PASS;
        $recommend = $model->where($recommend_map)->order('refreshtime desc,id desc')->limit(3)->select();
        $img = D('House/HouseRentImg')->where(array('pid' => $id))->order('id asc')->select();
        $this->assign('img', $img);
        $this->assign('recommend', $recommend);
        $this->assign('info', $info);
        $this->wx_share();
        $this->_config_seo(array('title' => '房屋出租详情 - ' . C('qscms_site_name'), 'header_title' => '房屋出租详情'));
        $this->display();
    }

    /**
     * 刷新信息
     * @param String $type 类型
     */
    public function refresh($type = 'rent') {
        $class = $type == 'rent' ? 'House/HouseRent' : 'House/HouseSeek';
        if (!$this->authentication_user) {
            $this->ajaxReturn(0, '请先验证身份！');
        }
        $yid = I('get.id', 0, 'intval');
        if (!$yid) {
            $this->ajaxReturn(0, '请选择信息！');
        } else {
            D($class)->refresh_info($yid, $this->authentication_user['uid']);
            $this->ajaxReturn(1, '刷新成功！');
        }
    }

    /**
     * 删除信息
     * @param String $type 类型
     */
    public function delete($type = 'rent') {
        $class = $type == 'rent' ? 'House/HouseRent' : 'House/HouseSeek';
        if (!$this->authentication_user) {
            $this->ajaxReturn(0, '请先验证身份！');
        }
        $id = I('request.id', 0, 'intval');
        if (!$id) {
            $this->ajaxReturn(0, '请选择信息！');
        } else {
            D($class)->where(array('id' => $id, 'uid' => $this->authentication_user['uid']))->delete();
            $this->ajaxReturn(1, '删除成功！');
        }
    }

    /**
     * 检测已发布出租信息数
     * @param String $type 类型
     */
    public function check_num($type = 'rent') {
        $class = $type == 'rent' ? 'House/HouseRent' : 'House/HouseSeek';
        if ($this->authentication_user) {
            $count = D($class)->where(array('uid' => $this->authentication_user['uid']))->count();
            if ($count >= C('qscms_house_max')) {
                $this->ajaxReturn(0, '你发布的信息数已达最大限制！');
            } else {
                $this->ajaxReturn(1, '可以发布');
            }
        } else {
            $this->ajaxReturn(1, '可以发布');
        }
    }

    public function upload_img() {
        $config_params = array(
            'upload_ok' => false,
            'save_path' => '',
            'show_path' => ''
        );
        $savePicName = uniqid() . '.jpg';
        $pic = base64_decode($_POST['base64_string']);
        $date = date('y/m/d/');
        $save_avatar = C('qscms_attach_path') . 'house_rent/' . $date;//图片存储路径
        if (!is_dir($save_avatar)) {
            mkdir($save_avatar, 0777, true);
        }
        $filename = $save_avatar . $savePicName;
        file_put_contents($filename, $pic);
        $config_params['save_path'] = $date . $savePicName;
        $config_params['show_path'] = attach($config_params['save_path'], 'house_rent');
        $config_params['upload_ok'] = true;
        if ($config_params['upload_ok']) {
            $setsqlarr['pid'] = 0;
            $setsqlarr['uid'] = 0;
            $setsqlarr['display'] = 0;
            $setsqlarr['audit'] = 0;
            $setsqlarr['img'] = $config_params['save_path'];
            $insertid = M('HouseRentImg')->add($setsqlarr);
            if ($insertid) {
                $tmpid = session('?house_imgid_tmp') ? session('house_imgid_tmp') : array();
                $tmpid[] = $insertid;
                session('house_imgid_tmp', $tmpid);
            }
            $data = array('path' => $config_params['show_path'], 'img' => $config_params['save_path']);
            $this->ajaxReturn(1, L('upload_success'), $data);
        } else {
            $this->ajaxReturn(0, $config_params['info']);
        }
    }

    /**
     * 求租信息首页
     */
    public function seek() {
        $map = array();
        $key = I('get.key', '', 'trim');
        $district = I('get.district', '', 'trim');
        $house = I('get.house', 0, 'intval');
        $rent = I('get.rent', 0, 'intval');
        if ($district) {
            $district_info = get_city_info($district);
            $district_id = $district_info['district'];
            $district_id_arr = explode(".", $district_id);
            $map['district' . count($district_id_arr)] = array('eq', $district_id);
        } else {
            if (C('SUBSITE_VAL.s_id') > 0) {
                $district_info = get_city_info(C('SUBSITE_VAL.s_district'));
                $district_id = $district_info['district'];
                $district_id_arr = explode(".", $district_id);
                $map['district' . count($district_id_arr)] = array('eq', $district_id);
            }
        }
        $key && $map['key'] = array('like', '%' . $key . '%');
        $house && $map['house'] = array('eq', $house);
        $rent && $map['rent'] = array('eq', $rent);
        $total = M('HouseSeekSearch')->where($map)->count();
        $pager = pager($total, 10);
        $page = $pager->fshow();
        $limit = $pager->firstRow . ',' . $pager->listRows;
        $ids = M('HouseSeekSearch')->where($map)->limit($limit)->order('refreshtime desc,id desc')->getField('id', true);
        if ($ids) {
            $list = D('House/HouseSeek')->where(array('id' => array('in', $ids)))->order('refreshtime desc,id desc')->select();
        } else {
            $list = array();
        }
        $category_house = D('House/HouseCategory')->get_category_cache('QS_house');
        $category_rent = D('House/HouseCategory')->get_category_cache('QS_rent');
        $this->assign('category_house', $category_house);
        $this->assign('category_rent', $category_rent);
        $this->assign('infolist', $list);
        $this->assign('page', $page);
        $this->_config_seo(array('title' => '房屋求租 - ' . C('qscms_site_name'), 'header_title' => '房屋求租'));
        $this->display();
    }

    /**
     * 发布求租信息
     */
    public function add_seek() {
        if (IS_POST) {
            $post_data = I('post.');
            if (!$this->authentication_user) {
                $this->auth_mobile_code();
                $post_data['uid'] = $this->authentication_user['uid'];
            } else {
                $auth_info = D('Members')->where(array('mobile' => $this->authentication_user['mobile'], 'secretkey' => $this->authentication_user['secretkey']))->find();
                if (!$auth_info) {
                    $this->ajaxReturn(0, '请先验证身份！');
                } else {
                    $post_data['uid'] = $this->authentication_user['uid'];
                }
            }
            $count = D('House/HouseSeek')->where(array('uid' => $this->authentication_user['uid']))->count();
            if ($count >= C('qscms_house_max')) {
                $this->ajaxReturn(0, '你已发布的信息数已超出限制！');
            }
            if (C('SUBSITE_VAL.s_id') > 0) {
                $post_data['subsite_id'] = C('SUBSITE_VAL.s_id');
            }
            if (false === $reg = D('House/HouseSeek')->create($post_data)) {
                $this->ajaxReturn(0, D('House/HouseSeek')->getError());
            } else {
                if (false === $insertid = D('House/HouseSeek')->add($reg)) {
                    $this->ajaxReturn(0, D('House/HouseSeek')->getError());
                } else {
                    D('House/HouseSeek')->update_search($insertid);
                    $this->ajaxReturn(1, '发布成功！', array('url' => U('House/manage',array('type'=>'seek'))));
                }
            }
        } else {
            $count = D('House/HouseSeek')->where(array('uid' => $this->authentication_user['uid']))->count();
            if ($count >= C('qscms_house_max')) {
                exit('你已发布的信息数已超出限制！');
            }
            $category = D('House/HouseCategory')->get_category_cache();
            $this->assign('need_mobile_audit', $this->authentication_user ? 0 : 1);
            $this->assign('contact', $this->authentication_user['contact']);
            $this->assign('mobile', $this->authentication_user['mobile']);
            $this->assign('new_record', 1);
            $this->assign('category', $category);
            $this->assign('leave_info_num', C('qscms_house_max') - $count);
            $this->_config_seo(array('title' => '发布房屋求租信息 - ' . C('qscms_site_name'), 'header_title' => '发布房屋求租信息'));
            $this->display();
        }
    }

    /**
     * 修改求租信息
     */
    public function edit_seek($id) {
        $model = D('House/HouseSeek');
        if (IS_POST) {
            $post_data = I('post.');
            if (!$this->authentication_user) {
                $this->ajaxReturn(0, '请先验证身份！');
            } else {
                $auth_info = D('Members')->where(array('mobile' => $this->authentication_user['mobile'], 'secretkey' => $this->authentication_user['secretkey']))->find();
                if (!$auth_info) {
                    $this->ajaxReturn(0, '请先验证身份！');
                }
            }
            if (C('qscms_audit_edit_house') == '1') {
                $post_data['audit'] = $model::AUDIT_WAIT;
            }
            if (false === $reg = $model->create($post_data)) {
                $this->ajaxReturn(0, $model->getError());
            } else {
                if (false === $model->save($reg)) {
                    $this->ajaxReturn(0, $model->getError());
                } else {
                    $model->update_search($reg['id']);
                    $this->ajaxReturn(1, '修改成功！', array('url' => U('House/manage',array('type'=>'seek'))));
                }
            }
        } else {
            $info = $model->where(array('id' => $id, 'uid' => $this->authentication_user['uid']))->find();
            $tags = $info['tag'] ? explode(",", $info['tag']) : array();
            $tagArr = array('id' => array(), 'cn' => array());
            if (!empty($tags)) {
                foreach ($tags as $key => $value) {
                    $arr = explode("|", $value);
                    $tagArr['id'][] = $arr[0];
                    $tagArr['cn'][] = $arr[1];
                }
            }
            $tagStr = array('id' => '', 'cn' => '');
            if (!empty($tagArr['id']) && !empty($tagArr['cn'])) {
                $tagStr['id'] = implode(",", $tagArr['id']);
                $tagStr['cn'] = implode(",", $tagArr['cn']);
            }
            $this->assign('tagArr', $tagArr);
            $this->assign('tagStr', $tagStr);
            $this->check_auth();
            $category = D('House/HouseCategory')->get_category_cache();
            $this->assign('mobile', $this->authentication_user['mobile']);
            $this->assign('new_record', 0);
            $this->assign('info', $info);
            $this->assign('category', $category);
            $this->_config_seo(array('title' => '修改求租信息 - ' . C('qscms_site_name'), 'header_title' => '修改求租信息'));
            $this->display('add_seek');
        }
    }

    /**
     * 求租信息详情
     */
    public function show_seek($id) {
        $model = D('House/HouseSeek');
        $info = $model->find($id);
        if (!$info) {
            $controller = new \Common\Controller\BaseController;
            $controller->_empty();
        }
        $model->where(array('id' => $id))->setInc('click', 1);
        if (C('qscms_contact_img_house') == 2) {
            $info['show_mobile'] = '<img src="' . C('qscms_site_domain') . U('Home/Qrcode/get_font_img', array('str' => encrypt($info['mobile'], C('PWDHASH')))) . '"/>';
        } else {
            $info['show_mobile'] = $info['mobile'];
        }
        $tags = $info['tag'] ? explode(",", $info['tag']) : array();
        $tagArr = array();
        if (!empty($tags)) {
            foreach ($tags as $key => $value) {
                $arr = explode("|", $value);
                $tagArr[] = $arr[1];
            }
        }
        $info['tag'] = $tagArr;
        $recommend_map['id'] = array('neq', $id);
        $recommend_map['category'] = array('eq', $info['category']);
        $recommend_map['audit'] = $model::AUDIT_PASS;
        $recommend = $model->where($recommend_map)->order('refreshtime desc,id desc')->limit(3)->select();
        $this->assign('recommend', $recommend);
        $this->assign('info', $info);
        $this->wx_share();
        $this->_config_seo(array('title' => '房屋求租详情 - ' . C('qscms_site_name'), 'header_title' => '房屋求租详情'));
        $this->display();
    }

    private function get_current_url() {
        $sys_protocal = isset($_SERVER['SERVER_PORT']) && $_SERVER['SERVER_PORT'] == '443' ? 'https://' : 'http://';
        $php_self = $_SERVER['PHP_SELF'] ? $_SERVER['PHP_SELF'] : $_SERVER['SCRIPT_NAME'];
        $path_info = isset($_SERVER['PATH_INFO']) ? $_SERVER['PATH_INFO'] : '';
        $relate_url = isset($_SERVER['REQUEST_URI']) ? $_SERVER['REQUEST_URI'] : $php_self . (isset($_SERVER['QUERY_STRING']) ? '?' . $_SERVER['QUERY_STRING'] : $path_info);
        return $sys_protocal . (isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST'] : '') . $relate_url;
    }

    private function check_auth() {
        if (!$this->authentication_user) {
            if (IS_AJAX) {
                $this->ajaxReturn(0, '请先验证身份！');
            } else {
                session('auth_url_referrer', $this->get_current_url());
                $this->redirect('authenticate');
            }
        } else {
            $auth_info = D('Members')->where(array('mobile' => $this->authentication_user['mobile'], 'secretkey' => $this->authentication_user['secretkey']))->find();
            if (!$auth_info) {
                if (IS_AJAX) {
                    $this->ajaxReturn(0, '请先验证身份！');
                } else {
                    session('auth_url_referrer', $this->get_current_url());
                    $this->redirect('authenticate');
                }
            }
        }
    }

    // 发送短信验证码
    public function send_sms() {
        if (C('qscms_mobile_captcha_open') && C('qscms_wap_captcha_config.varify_mobile') && true !== $reg = \Common\qscmslib\captcha::verify('mobile')) $this->ajaxReturn(0, $reg);
        $mobile = I('post.mobile', '', 'trim');
        !$mobile && $this->ajaxReturn(0, '请填手机号码！');
        if (!fieldRegex($mobile, 'mobile')) $this->ajaxReturn(0, '手机号错误！');
        $rand = getmobilecode();
        $sendSms['tpl'] = 'set_login';
        $sendSms['data'] = array('rand' => $rand . '', 'sitename' => C('qscms_site_name'));
        $smsVerify = session('login_smsVerify');
        if ($smsVerify && $smsVerify['mobile'] == $mobile && time() < $smsVerify['time'] + 180) $this->ajaxReturn(0, '180秒内仅能获取一次短信验证码,请稍后重试');
        $sendSms['mobile'] = $mobile;
        if (true === $reg = D('Sms')->sendSms('captcha', $sendSms)) {
            session('login_smsVerify', array('rand' => substr(md5($rand), 8, 16), 'time' => time(), 'mobile' => $mobile));
            $this->ajaxReturn(1, '手机验证码发送成功！');
        } else {
            $this->ajaxReturn(0, $reg);
        }
    }

    private function auth_mobile_code() {
        $expire = I('post.expire', 1, 'intval');
        if ($mobile = I('post.mobile', '', 'trim')) {
            if (!fieldRegex($mobile, 'mobile')) $this->ajaxReturn(0, '手机号格式错误！');
            $smsVerify = session('login_smsVerify');
            !$smsVerify && $this->ajaxReturn(0, '手机验证码错误！');//验证码错误！
            if ($mobile != $smsVerify['mobile']) $this->ajaxReturn(0, '手机号不一致！');//手机号不一致
            if (time() > $smsVerify['time'] + 600) $this->ajaxReturn(0, '验证码过期！');//验证码过期
            $vcode_sms = I('post.mobile_vcode', 0, 'intval');
            $mobile_rand = substr(md5($vcode_sms), 8, 16);
            if ($mobile_rand != $smsVerify['rand']) $this->ajaxReturn(0, '手机验证码错误！');//验证码错误！
            $user = D('Members')->where(array('mobile' => $smsVerify['mobile']))->find();
            if (!$user) {
                $user = D('Members')->add_auth_info($smsVerify['mobile']);
            }elseif(!$user['secretkey']){
                $user['secretkey'] = D('Members')->randstr(16,true);
                M('Members')->where(array('uid'=>$user['uid']))->setfield('secretkey',$user['secretkey']);
            }
            $user['contact'] = I('post.contact', '', 'trim');
            session('authentication_user', $user);
            cookie('authentication_user', $user);
            session('login_smsVerify', null);
            $this->authentication_user = $user;
        }
        if ($user) $this->apply_login($smsVerify['mobile'],$expire);
    }

    public function do_auth() {
        $this->auth_mobile_code();
        $this->ajaxReturn(1, '验证通过');
    }

    public function authenticate() {
        $this->assign('auth_url_referrer', session('auth_url_referrer'));
        $this->_config_seo(array('title' => '身份验证 - ' . C('qscms_site_name'), 'header_title' => '身份验证'));
        $this->display();
    }
}

?>