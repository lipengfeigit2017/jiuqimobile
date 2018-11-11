<?php
namespace Mobile\Controller;

use Mobile\Controller\MobileController;

class StoretransferController extends MobileController {
    public $authentication_user;
    public $cache_apply_info;

    // 初始化函数
    public function _initialize() {
        parent::_initialize();
        $this->authentication_user = session('authentication_user') ? session('authentication_user') : cookie('authentication_user');
        $this->cache_apply_info = session('cache_apply_info') ? session('cache_apply_info') : cookie('cache_apply_info');
    }

    /**
     * 门店信息首页
     */
    public function index() {
        $area_arr = array('0-20' => '20㎡以下', '20-50' => '20-50㎡', '50-100' => '50-100㎡', '100-200' => '100-200㎡', '200-500' => '200-500㎡', '500' => '500㎡以上');
        $rent_arr = array('0-2000' => '2千元以下', '2000-5000' => '2-5千元', '5000-10000' => '5千-1万元', '10000-20000' => '1-2万元', '20000-50000' => '2-5万元', '50000' => '5万元以上');
        $map = array();
        $key = I('get.key', '', 'trim');
        $district = I('get.district', '', 'trim');
        $area = I('get.area', '', 'trim');
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
        if ($area) {
            if (stripos($area, '-') === false) {
                $map['area'] = array('egt', $area);
            } else {
                $sub_area_arr = explode("-", $area);
                $map['area'] = array(array('egt', $sub_area_arr[0]), array('lt', $sub_area_arr[1]), 'and');
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
        $total = M('StoretransferSearch')->where($map)->count();
        $pager = pager($total, 10);
        $page = $pager->fshow();
        $limit = $pager->firstRow . ',' . $pager->listRows;
        $ids = M('StoretransferSearch')->where($map)->limit($limit)->order('refreshtime desc,id desc')->getField('id', true);
        if ($ids) {
            $infolist = D('Store/Storetransfer')->where(array('id' => array('in', $ids)))->order('refreshtime desc,id desc')->select();
        } else {
            $infolist = array();
        }
        $this->assign('area_arr', $area_arr);
        $this->assign('rent_arr', $rent_arr);
        $this->assign('infolist', $infolist);
        $this->assign('page', $page);
        $this->_config_seo(array('title' => '门店转让 - ' . C('qscms_site_name'), 'header_title' => '门店转让'));
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

    /**
     * 信息管理
     */
    public function manage() {
        $this->check_auth();
        $model = D('Store/Storetransfer');
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
        $this->_config_seo(array('title' => '转让信息管理 - ' . C('qscms_site_name'), 'header_title' => '转让信息管理'));
        $this->display();
    }

    /**
     * 发布门店信息
     */
    public function add() {
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
            $count_jobs = D('Store/Storetransfer')->where(array('uid' => $this->authentication_user['uid']))->count();
            if ($count_jobs >= C('qscms_store_max')) {
                $this->ajaxReturn(0, '你已发布的信息数已超出限制！');
            }
            if (C('SUBSITE_VAL.s_id') > 0) {
                $post_data['subsite_id'] = C('SUBSITE_VAL.s_id');
            }
            if (false === $reg = D('Store/Storetransfer')->create($post_data)) {
                $this->ajaxReturn(0, D('Store/Storetransfer')->getError());
            } else {
                if (false === $insertid = D('Store/Storetransfer')->add($reg)) {
                    $this->ajaxReturn(0, D('Store/Storetransfer')->getError());
                } else {
                    $store_imgid_tmp = session('?store_imgid_tmp') ? session('store_imgid_tmp') : array();
                    if (!empty($store_imgid_tmp)) {
                        M('StoretransferImg')->where(array('id' => array('in', $store_imgid_tmp)))->save(array('pid' => $insertid, 'uid' => $post_data['uid'], 'display' => 1));
                    }
                    session('store_imgid_tmp', null);
                    $this->ajaxReturn(1, '发布成功！', array('url' => U('Storetransfer/manage')));
                }
            }
        } else {
            session('store_imgid_tmp', null);
            $count_jobs = D('Store/Storetransfer')->where(array('uid' => $this->authentication_user['uid']))->count();
            if ($count_jobs >= C('qscms_store_max')) {
                exit('你已发布的信息数已超出限制！');
            }
            $category_property_type = D('Store/StoreCategory')->get_category_cache('QS_property_type');
            $category_tag = D('Store/StoreCategory')->get_category_cache('QS_tag');
            $category_engagein_type = D('Store/StoreCategory')->get_category_cache('QS_store_type');
            $this->assign('need_mobile_audit', $this->authentication_user ? 0 : 1);
            $this->assign('contact', $this->authentication_user['contact']);
            $this->assign('mobile', $this->authentication_user['mobile']);
            $this->assign('new_record', 1);
            $this->assign('category_property_type', $category_property_type);
            $this->assign('category_engagein_type', $category_engagein_type);
            $this->assign('category_tag', $category_tag);
            $this->assign('leave_info_num', C('qscms_store_max') - $count_jobs);
            $this->_config_seo(array('title' => '发布门店转让 - ' . C('qscms_site_name'), 'header_title' => '发布门店转让'));
            $this->display();
        }
    }

    /**
     * 修改门店信息
     */
    public function edit($id) {
        $model = D('Store/Storetransfer');
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
            if (C('qscms_audit_edit_store') == '1') {
                $post_data['audit'] = $model::AUDIT_WAIT;
            }
            if (false === $reg = $model->create($post_data)) {
                $this->ajaxReturn(0, $model->getError());
            } else {
                if (false === $model->save($reg)) {
                    $this->ajaxReturn(0, $model->getError());
                } else {
                    $store_imgid_tmp = session('?store_imgid_tmp') ? session('store_imgid_tmp') : array();
                    if (!empty($store_imgid_tmp)) {
                        M('StoretransferImg')->where(array('id' => array('in', $store_imgid_tmp)))->save(array('pid' => $reg['id'], 'uid' => $this->authentication_user['uid'], 'display' => 1));
                    }
                    session('store_imgid_tmp', null);
                    $model->update_search($reg['id']);
                    $this->ajaxReturn(1, '修改成功！', array('url' => U('Storetransfer/manage')));
                }
            }
        } else {
            $info = $model->where(array('id' => $id, 'uid' => $this->authentication_user['uid']))->find();
            $storetag = $info['tag'] ? explode(",", $info['tag']) : array();
            $tagArr = array('id' => array(), 'cn' => array());
            if (!empty($storetag)) {
                foreach ($storetag as $key => $value) {
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
            session('store_imgid_tmp', null);
            $this->check_auth();
            $category_property_type = D('Store/StoreCategory')->get_category_cache('QS_property_type');
            $category_tag = D('Store/StoreCategory')->get_category_cache('QS_tag');
            $category_engagein_type = D('Store/StoreCategory')->get_category_cache('QS_store_type');
            $storetransfer_img = M('StoretransferImg')->where(array('pid' => $info['id']))->select();
            $this->assign('mobile', $this->authentication_user['mobile']);
            $this->assign('new_record', 0);
            $this->assign('info', $info);
            $this->assign('storetransfer_img', $storetransfer_img);
            $this->assign('category_property_type', $category_property_type);
            $this->assign('category_engagein_type', $category_engagein_type);
            $this->assign('category_tag', $category_tag);
            $this->_config_seo(array('title' => '修改门店转让 - ' . C('qscms_site_name'), 'header_title' => '修改门店转让'));
            $this->display('add');
        }
    }

    /**
     * 门店信息详情
     */
    public function show($id) {
        $model = D('Store/Storetransfer');
        $info = $model->find($id);
        if (!$info) {
            $controller = new \Common\Controller\BaseController;
            $controller->_empty();
        }
        $model->where(array('id' => $id))->setInc('click', 1);
        if (C('qscms_contact_img_store') == 2) {
            $info['show_mobile'] = '<img src="' . C('qscms_site_domain') . U('Home/Qrcode/get_font_img', array('str' => encrypt($info['mobile'], C('PWDHASH')))) . '"/>';
        } else {
            $info['show_mobile'] = $info['mobile'];
        }
        $storetag = $info['tag'] ? explode(",", $info['tag']) : array();
        $tagArr = array();
        if (!empty($storetag)) {
            foreach ($storetag as $key => $value) {
                $arr = explode("|", $value);
                $tagArr[] = $arr[1];
            }
        }
        $info['tag'] = $tagArr;
        $recommend_map['id'] = array('neq', $id);
        $recommend_map['category'] = array('eq', $info['category']);
        $recommend_map['audit'] = $model::AUDIT_PASS;
        $recommend = $model->where($recommend_map)->order('refreshtime desc,id desc')->limit(3)->select();
        $img = D('Store/StoretransferImg')->where(array('pid' => $id,'audit'=>1))->order('id asc')->select();
        $this->assign('img', $img);
        $this->assign('recommend', $recommend);
        $this->assign('info', $info);
        $this->wx_share();
        $this->_config_seo(array('title' => '门店转让详情 - ' . C('qscms_site_name'), 'header_title' => '门店转让详情'));
        $this->display();
    }

    /**
     * 刷新信息
     */
    public function refresh() {
        if (!$this->authentication_user) {
            $this->ajaxReturn(0, '请先验证身份！');
        }
        $yid = I('get.yid', 0, 'intval');
        if (!$yid) {
            $this->ajaxReturn(0, '请选择信息！');
        } else {
            D('Store/Storetransfer')->refresh_info($yid, $this->authentication_user['uid']);
            $this->ajaxReturn(1, '刷新成功！');
        }
    }

    /**
     * 删除信息
     */
    public function delete() {
        if (!$this->authentication_user) {
            $this->ajaxReturn(0, '请先验证身份！');
        }
        $id = I('request.id', 0, 'intval');
        if (!$id) {
            $this->ajaxReturn(0, '请选择信息！');
        } else {
            D('Store/Storetransfer')->where(array('id' => $id, 'uid' => $this->authentication_user['uid']))->delete();
            $this->ajaxReturn(1, '删除成功！');
        }
    }

    /**
     * 检测已发布信息数
     */
    public function check_info_num() {
        if ($this->authentication_user) {
            $count_jobs = D('Store/Storetransfer')->where(array('uid' => $this->authentication_user['uid']))->count();
            if ($count_jobs >= C('qscms_store_max')) {
                $this->ajaxReturn(0, '你发布的求租信息数已达最大限制！');
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
        $save_avatar = C('qscms_attach_path') . 'storetransfer/' . $date;//图片存储路径
        if (!is_dir($save_avatar)) {
            mkdir($save_avatar, 0777, true);
        }
        $filename = $save_avatar . $savePicName;
        file_put_contents($filename, $pic);
        $config_params['save_path'] = $date . $savePicName;
        $config_params['show_path'] = attach($config_params['save_path'], 'storetransfer');
        $config_params['upload_ok'] = true;
        if ($config_params['upload_ok']) {
            $setsqlarr['pid'] = 0;
            $setsqlarr['uid'] = 0;
            $setsqlarr['display'] = 0;
            $setsqlarr['audit'] = 0;
            $setsqlarr['img'] = $config_params['save_path'];
            $insertid = M('StoretransferImg')->add($setsqlarr);
            if ($insertid) {
                $tmpid = session('?store_imgid_tmp') ? session('store_imgid_tmp') : array();
                $tmpid[] = $insertid;
                session('store_imgid_tmp', $tmpid);
            }
            $data = array('path' => $config_params['show_path'], 'img' => $config_params['save_path']);
            $this->ajaxReturn(1, L('upload_success'), $data);
        } else {
            $this->ajaxReturn(0, $config_params['info']);
        }
    }
}

?>