<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class MembersController extends MobileController{
	public function _initialize(){
		parent::_initialize();
        //访问者控制
        $check_weixin_login = $this->check_weixin_login();
        if(!$this->visitor->is_login) {
            if(in_array(ACTION_NAME, array('index'))){
                IS_AJAX && $this->ajaxReturn(0, L('login_please'),'',1);
                //非ajax的跳转页面
                $this->redirect('members/login');
            }
        }else{
            $urls = array('1'=>'company/index','2'=>'personal/index');
            !IS_AJAX && !in_array(ACTION_NAME, array('logout','varify_email','choose_members','personal_add','company_add')) && $this->redirect($urls[C('visitor.utype')],array('uid'=>$this->visitor->info['uid']));
        }
	}
    /**
     * [login 用户登录]
     */
    public function login() {
        if(IS_POST){
            $expire = I('post.expire',1,'intval');
            $index_login = I('post.index_login',0,'intval');
            if (C('qscms_mobile_captcha_open')==1 && (C('qscms_wap_captcha_config.user_login')==0 || (session('?error_login_count') && session('error_login_count')>=C('qscms_wap_captcha_config.user_login')))){
                if(true !== $reg = \Common\qscmslib\captcha::verify('mobile')) $this->ajaxReturn(0,$reg);
            }
            $passport = $this->_user_server();
            if($mobile = I('post.mobile','','trim')){
                if(!fieldRegex($mobile,'mobile')) $this->ajaxReturn(0,'手机号格式错误！');
                $smsVerify = session('login_smsVerify');
                !$smsVerify && $this->ajaxReturn(0,'手机验证码错误！');//验证码错误！
                if($mobile != $smsVerify['mobile']) $this->ajaxReturn(0,'手机号不一致！');//手机号不一致
                if(time()>$smsVerify['time']+600) $this->ajaxReturn(0,'验证码过期！');//验证码过期
                $vcode_sms = I('post.mobile_vcode',0,'intval');
                $mobile_rand=substr(md5($vcode_sms), 8,16);
                if($mobile_rand!=$smsVerify['rand']) $this->ajaxReturn(0,'手机验证码错误！');//验证码错误！
                $user = M('Members')->where(array('mobile'=>$smsVerify['mobile']))->find();
                if($user){
                    $uid = $user['uid'];
                    if($user['utype'] == 1 && !$user['sitegroup_uid'] || !$user['uc_uid']){
                        $company = M('CompanyProfile')->field('companyname,contact,landline_tel')->where(array('uid'=>$user['uid']))->find();
                        $user = array_merge($user,$company);
                    }
                    if(!$user['sitegroup_uid'] && $passport->is_sitegroup()){
                        $temp = $passport->uc('sitegroup')->register($user);
                        $temp && $setsqlarr['sitegroup_uid'] = $temp['sitegroup_uid'];
                    }
                    if(!$user['uc_uid'] && !$passport->is_sitegroup() && $passport->is_uc()){
                        $temp = $passport->uc('ucenter')->register($user);
                        $temp && $setsqlarr['uc_uid'] = $temp['uc_uid'];
                    }
                    if(!$user['mobile_audit']){
                        $setsqlarr['mobile'] = $smsVerify['mobile'];
                        $setsqlarr['mobile_audit']=1;
                    }
                    if($setsqlarr){
                        if(false !== $passport->uc('sitegroup')->edit($user['uid'],array('mobile_audit'=>1))){
                            if(!$user['mobile_audit']){
                                D('Members')->update_user_info($setsqlarr,$user);
                                if($user['utype']=='1'){
                                    $rule=D('Task')->get_task_cache($user['utype'],22);
                                    D('TaskLog')->do_task($user,22);
                                }else{
                                    $rule=D('Task')->get_task_cache($user['utype'],7);
                                    D('TaskLog')->do_task($user,7);
                                }
                                write_members_log($user,'','手机验证通过（手机号：'.$smsVerify['mobile'].'）');
                            }
                        }
                    }
                    session('login_smsVerify',null);
                }elseif($passport->is_sitegroup() && false !== $sitegroup_user = $passport->uc('sitegroup')->get($smsVerify['mobile'], 'mobile')){
                    $this->_sitegroup_register($sitegroup_user,'mobile');
                }else{
                    $err = '账号不存在！';
                }
            }else{
                $username = I('post.username','','trim');
                $password = I('post.password','','trim');
                if(false === $uid = $passport->uc('default')->auth($username, $password)){
                    $err = $passport->get_error();
                    if($err == L('auth_null')){
                        if($passport->is_sitegroup()){
                            if(false === $passport->uc('sitegroup')->auth($username, $password)){
                                $err = $passport->get_error();
                            }else{
                                $this->_sitegroup_register($passport->_user);
                            }
                        }elseif($passport->is_uc() && false !== $passport->uc('ucenter')->auth($username, $password)){
                            cookie('members_uc_info', $passport->_user);
                            $this->ajaxReturn(1,'5',U('members/apilogin_ucenter'));
                        }
                    }
                }else{
                    $user = $passport->_user;
                    if($user['utype'] == 1 && (!$user['sitegroup_uid'] || !$user['uc_uid'])){
                        $company = M('CompanyProfile')->field('companyname,contact,landline_tel')->where(array('uid'=>$user['uid']))->find();
                        $user = array_merge($user,$company);
                    }
                    if(!$user['sitegroup_uid'] && $passport->is_sitegroup()){
                        $temp = $passport->uc('sitegroup')->register($user);
                        $temp && M('Members')->where(array('uid'=>$user['uid']))->setfield('sitegroup_uid',$temp['sitegroup_uid']);
                    }
                    if(!$user['uc_uid'] && !$passport->is_sitegroup() && $passport->is_uc()){
                        $temp = $passport->uc('ucenter')->register($user);
                        $temp && M('Members')->where(array('uid'=>$user['uid']))->setfield('uc_uid',$temp['uc_uid']);
                    }
                }
            }
            if($uid){
                if(false === $this->visitor->login($uid, $expire)) $this->ajaxReturn(0,$this->visitor->getError());
                if(!$login_url = cookie('return_url')){
                    $urls = array('1'=>'company/index','2'=>'personal/index');
                    $login_url = U($urls[$this->visitor->info['utype']],array('uid'=>$this->visitor->info['uid']));
                }else{
                    cookie('return_url',null);
                }
                //同步登录
                $passport->uc('ucenter')->synlogin($uid,$expire);
                $this->ajaxReturn(1,'登录成功！',$login_url);
            }
            //记录登录错误次数
            if(C('qscms_mobile_captcha_open')==1){
                if(C('qscms_wap_captcha_config.user_login')>0){
                    $error_login_count = session('?error_login_count')?(session('error_login_count')+1):1;
                    session('error_login_count',$error_login_count);
                    if(session('error_login_count')>=C('qscms_wap_captcha_config.user_login')){
                        $verify_userlogin = 1;
                    }else{
                        $verify_userlogin = 0;
                    }
                }else{
                    $verify_userlogin = 1;
                }
            }else{
                $verify_userlogin = 0;
            }
            $this->ajaxReturn(0,$err,$verify_userlogin);
        }else{
            //来路
            $return_url = isset($_SERVER['HTTP_REFERER']) ? $_SERVER['HTTP_REFERER'] : __APP__;
            if(!IS_AJAX && !strpos(strtolower($return_url),'login')){
                $s = true;
                cookie('return_url',$return_url);
            }
            if($this->visitor->is_login){
                cookie('return_url',null);
                $urls = array('1'=>'company/index','2'=>'personal/index');
                $url = $s ? $return_url : U($urls[C('visitor.utype')],array('uid'=>$this->visitor->info['uid']));
                $this->redirect($url);
            }
            if(false === $oauth_list = F('oauth_list')){
                $oauth_list = D('Oauth')->oauth_cache();
            }
            
            $this->assign('oauth_list',$oauth_list);
            $this->assign('verify_userlogin',$this->check_captcha_open(C('qscms_wap_captcha_config.user_login'),'error_login_count'));
            $this->_config_seo(array('title'=>'会员登录 - '.C('qscms_site_name'),'header_title'=>'会员登录'));
            $this->display();
        }
    }
    /**
     * [选择登录身份]
     */
    public function choose_members() { 
        $this->display();
    }
     /**
     * 兼职 门店 租房 普工 前台发布 登录会员类型选择 选择个人
     */
    public function personal_add(){
        $data['uid'] = C('visitor.uid');
        $data['mobile'] = C('visitor.mobile');
        $data['utype'] = 2;
        $members = D('Members')->Where(array('utype'=>1,'mobile'=>$data['mobile']))->select();
        $members && $this->_404('请勿重复选择会员类型');
        $utype=D('Members')->Where(array('uid'=>$data['uid']))->setField('utype',2);
        !$utype && $this->ajaxReturn(0,'更新会员类型失败');
        D('Members')->user_register($data);
        $this->visitor->login($data['uid'], $expire);
        $this->redirect('personal/index',array('uid'=>$data['uid']));
    }
    /**
     * 兼职 门店 租房 普工 前台发布 登录会员类型选择 选择企业
     */
    public function company_add(){
        $data['uid'] = C('visitor.uid');
        $data['mobile'] = C('visitor.mobile');
        $data['utype'] = 2;
        $members = D('Members')->Where(array('utype'=>2,'mobile'=>$data['mobile']))->select();
        $members && $this->_404('请勿重复选择会员类型');
        if(IS_POST && IS_AJAX){
            $utype=D('Members')->Where(array('uid'=>$data['uid']))->setField('utype',1);
            !$utype && $this->ajaxReturn(0,'更新会员类型失败');
            D('Members')->user_register($data);
            $com_setarr['audit'] = 0;
            $com_setarr['companyname']=I('post.companyname','','trim,badword');
            $com_setarr['contact']=I('post.contact','','trim,badword');
            $com_setarr['telephone']=$data['mobile'];
            $com_setarr['uid'] =$data['uid'];
            $company_mod = D('CompanyProfile');
            if(false === $company_mod->create($com_setarr)) $this->ajaxReturn(0,$company_mod->getError());
            C('SUBSITE_VAL.s_id') && $company_mod->subsite_id = C('SUBSITE_VAL.s_id');
            $insert_company_id = $company_mod->add();
            if($insert_company_id){
              D('Members')->user_register($data);
              $this->visitor->login($data['uid'], $expire);
              //$this->redirect('company/index',array('uid'=>$data['uid']));
              $result['url'] =  U('company/index');
             $this->ajaxReturn(1,'提交成功！',$result);
            }
        }
        $this->display();
    }
    /**
     * [login 用户手机动态码登录]
     */
    public function login_mobile() {
        if($this->visitor->is_login){
            $urls = array('1'=>'company/index','2'=>'personal/index');
            $this->redirect($urls[C('visitor.utype')],array('uid'=>$this->visitor->info['uid']));
        }
        if(false === $oauth_list = F('oauth_list')){
            $oauth_list = D('Oauth')->oauth_cache();
        }
        $this->assign('oauth_list',$oauth_list);
        $this->assign('verify_userlogin',$this->check_captcha_open(C('qscms_wap_captcha_config.user_login'),'error_login_count'));
        $this->_config_seo(array('title'=>'会员登录 - '.C('qscms_site_name'),'header_title'=>'会员登录'));
        $this->display();
    }
    // 注册发送短信/找回密码 短信
    public function reg_send_sms(){
        if(C('qscms_mobile_captcha_open') && C('qscms_wap_captcha_config.varify_mobile') && true !== $reg = \Common\qscmslib\captcha::verify('mobile')) $this->ajaxReturn(0,$reg);
        if($uid = I('post.uid',0,'intval')){
            $mobile=M('Members')->where(array('uid'=>$uid))->getfield('mobile');
            !$mobile && $this->ajaxReturn(0,'用户不存在！');
        }else{
            $mobile = I('post.mobile','','trim');
            !$mobile && $this->ajaxReturn(0,'请填手机号码！');
        }
        if(!fieldRegex($mobile,'mobile')) $this->ajaxReturn(0,'手机号错误！');
        $sms_type = I('post.sms_type','reg','trim');
        $rand=getmobilecode();
        switch ($sms_type) {
            case 'reg':
                $sendSms['tpl']='set_register';
                $sendSms['data']=array('rand'=>$rand.'','sitename'=>C('qscms_site_name'));
                break;
            case 'gsou_reg':
                $sendSms['tpl']='set_register';
                $sendSms['data']=array('rand'=>$rand.'','sitename'=>C('qscms_site_name'));
                break;
            case 'getpass':
                $sendSms['tpl']='set_retrieve_password';
                $sendSms['data']=array('rand'=>$rand.'','sitename'=>C('qscms_site_name'));
                break;
            case 'login':
                if(!$uid=M('Members')->where(array('mobile'=>$mobile))->getfield('uid')){
                    if(false === $sitegroup_user = $passport->uc('sitegroup')->get($smsVerify['mobile'], 'mobile')){
                        $this->ajaxReturn(0,'您输入的手机号未注册会员');
                    }
                }
                $sendSms['tpl']='set_login';
                $sendSms['data']=array('rand'=>$rand.'','sitename'=>C('qscms_site_name'));
                break;
        }
        $smsVerify = session($sms_type.'_smsVerify');
        if($smsVerify && $smsVerify['mobile']==$mobile && time()<$smsVerify['time']+180) $this->ajaxReturn(0,'180秒内仅能获取一次短信验证码,请稍后重试');
        $sendSms['mobile']=$mobile;
        if(true === $reg = D('Sms')->sendSms('captcha',$sendSms)){
            session($sms_type.'_smsVerify',array('rand'=>substr(md5($rand), 8,16),'time'=>time(),'mobile'=>$mobile));
            $this->ajaxReturn(1,'手机验证码发送成功！');
        }else{
            $this->ajaxReturn(0,$reg);
        }
    }
    /**
     * 用户退出
     */
    public function logout() {
        $this->visitor->logout();
        //同步退出
        $passport = $this->_user_server();
        $synlogout = $passport->synlogout();
        $this->redirect('members/login');
    }
    /**
     * [register 会员注册]
     */
    public function register(){
        if (C('qscms_mobile_closereg')){
            IS_AJAX && $this->ajaxReturn(0,'触屏端网站暂停会员注册，请稍后再次尝试！');
            $this->_404("触屏端网站暂停会员注册，请稍后再次尝试！");
        }
        if(IS_POST && IS_AJAX){
            $data['reg_type'] = I('post.reg_type',1,'intval');//注册方式(1:手机，2:邮箱，3:微信)
            $array = array(1 => 'mobile',2 => 'email');
            if(!$reg = $array[$data['reg_type']]) $this->ajaxReturn(0,'正确选择注册方式！');
            $data['utype'] = I('post.utype',0,'intval');
            if($data['utype'] != 1 && $data['utype'] != 2) $this->ajaxReturn(0,'请正确选择会员类型!');
            if($data['reg_type'] == 1){
                $data['mobile'] = I('post.mobile',0,'trim');
                $smsVerify = session('reg_smsVerify');
                if(!$smsVerify) $this->ajaxReturn(0,'手机验证码错误！');
                if($data['mobile'] != $smsVerify['mobile']) $this->ajaxReturn(0,'手机号不一致！',$smsVerify);//手机号不一致
                if(time()>$smsVerify['time']+600) $this->ajaxReturn(0,'验证码过期！');//验证码过期
                $vcode_sms = I('post.mobile_vcode',0,'intval');
                $mobile_rand=substr(md5($vcode_sms), 8,16);
                if($mobile_rand!=$smsVerify['rand']) $this->ajaxReturn(0,'手机验证码错误！');//验证码错误！
            }
            if(C('qscms_register_password_open')){
                $data['password'] = I('post.password','','trim');
                !$data['password'] && $this->ajaxReturn(0,'请输入密码!');
                $passwordVerify = I('post.passwordVerify','','trim');
                $data['password'] != $passwordVerify && $this->ajaxReturn(0,'两次密码输入不一致!');
            }
            if('bind' == $ucenter = I('post.ucenter','','trim')){
                $uc_user = cookie('members_uc_info');
                $data = array_merge($data,$uc_user);
                $passwordVerify = $data['password'];
            }
            if($data['utype']==1){
                $com_setarr['audit'] = 0;
                $com_setarr['companyname']=I('post.companyname','','trim,badword');
                $com_setarr['contact']=I('post.contact','','trim,badword');
                $com_setarr['telephone']=I('post.mobile','','trim,badword');
                $company_mod = D('CompanyProfile');
                if(false === $company_mod->create($com_setarr)) $this->ajaxReturn(0,$company_mod->getError());
                $data = array_merge($data,$com_setarr);
            }
            $us = $uc_user ? 'default' : '';
            $passport = $this->_user_server($us);
            // 若手机号重复且勾选了解绑原账号
            if($data['reg_type'] == 1 && $data['utype'] ==2){
                $data['unbind_mobile'] = I('post.unbind_mobile',0,'intval');
                if($data['unbind_mobile']){
                    $repeat = M('Members')->where(array('mobile'=>$data['mobile']))->select();
                    foreach ($repeat as $val){
                        if(false != D('UnbindMobile')->create($val)){
                            D('UnbindMobile')->add();
                        }
                        $update['mobile'] = '';
                        $update['mobile_audit'] = 0;
                        M('Members')->where(array('uid'=>$val['uid']))->save($update);
                    }
                }
            }
            if(false === $data = $passport->register($data)){
                if($user = $passport->get_status()) $this->ajaxReturn(1,'会员注册成功！',array('url'=>U('members/reg_email_activate',array('uid'=>$user['uid']))));
                $this->ajaxReturn(0,$passport->get_error());
            }
            // 添加企业信息
            if($data['utype']==1){
                $company_mod->uid=$data['uid'];
                $insert_company_id = $company_mod->add();
                if($insert_company_id){
                    switch($com_setarr['audit']){
                        case 1:
                            $audit_str = '认证通过';break;
                        case 2:
                            $audit_str = '认证中';break;
                        case 3:
                            $audit_str = '认证未通过';break;
                        default:
                            $audit_str = '';break;
                    }
                    if($audit_str){
                        $auditsqlarr['company_id']=$insert_company_id;
                        $auditsqlarr['reason']='自动设置';
                        $auditsqlarr['status']=$audit_str;
                        $auditsqlarr['addtime']=time();
                        $auditsqlarr['audit_man']='系统';
                        M('AuditReason')->data($auditsqlarr)->add();
                    }
                }
            }
            if('bind' == I('post.org','','trim') && cookie('members_bind_info')){
                $user_bind_info = object_to_array(cookie('members_bind_info'));
                $user_bind_info['uid'] = $data['uid'];
                $oauth = new \Common\qscmslib\oauth($user_bind_info['type']);
                $oauth->bindUser($user_bind_info);
                if($user_bind_info['type'] == 'weixin'){
                    $data['openid'] = $user_bind_info['openid']?:I('request.openid','','trim');
                }
                if($user_bind_info['keyavatar_big']) M('Members')->where(array('uid'=>$data['uid']))->setfield('avatars',$user_bind_info['keyavatar_big']);//临时头像转换
                cookie('members_bind_info', NULL);//清理绑定COOKIE
            }
            session('reg_smsVerify',null);
            D('Members')->user_register($data);
            $incode = I('post.incode','','trim');
            //如果是推荐注册，赠送积分
            $this->_incode($incode);
            $this->_correlation($data);
            $points_rule = D('Task')->get_task_cache(2,1);
            $result['url'] = $data['utype']==2 ? U('personal/resume_add',array('points'=>$points_rule['points'],'first'=>1)) : U('members/index');
            $this->ajaxReturn(1,'会员注册成功！',$result);
        }else{
            $utype = I('get.utype',0,'intval');
            $reg_type = I('get.regtype','mobile','trim');
            $utype == 0 && $type = 'reg';
            $utype == 1 && $type = 'reg_company';
            if($utype==2){
                if($reg_type=='mobile'){
                    $type = 'reg_personal';
                }else{
                    $type = 'reg_personal_email';
                }
            }
            //第三方登录
            if(false === $oauth_list = F('oauth_list')){
                $oauth_list = D('Oauth')->oauth_cache();
            }
            $this->assign('utype',$utype);//注册会员类型
            $this->assign('user_bind',$user_bind);
            $this->assign('oauth_list',$oauth_list);
            $this->assign('openid',I('get.openid','','trim'));
            $this->assign('company_repeat',C('qscms_company_repeat'));//企业注册名称是否可以重复
            $this->_config_seo(array('title'=>'会员注册 - '.C('qscms_site_name'),'header_title'=>'会员注册'));
            $this->display($type);
        }
    }
    /**
     * [reg_email_activate description]
     */
    public function reg_email_activate(){
        $uid = I('get.uid',0,'intval');
        !$uid && $this->redirect('members/register');
        $user = M('Members')->field('uid,email')->where(array('uid'=>$uid,'status'=>0))->find();
        !$user && $this->redirect('members/register');
        $this->assign('user',$user);
        $this->_config_seo(array('title'=>'会员注册 - '.C('qscms_site_name'),'header_title'=>'会员注册'));
        $this->display();
    }
    /**
     * [activate 邮箱注册激活]
     */
    public function activate(){
        parse_str(decrypt(I('get.key','','trim')),$data);
        !fieldRegex($data['e'],'email') && $this->_404('邮箱格式错误！',U('members/register'));
        $end_time=$data['t']+24*3600;
        if($end_time<time()) $this->_404('注册失败,链接过期!',U('members/register'));
        $key_str=substr(md5($data['e'].$data['t']),8,16);
        if($key_str!=$data['k']) $this->_404('注册失败,key错误',U('members/register'));
        $members_mod = D('Members');
        $user = $members_mod->field('uid,utype,email,status')->where(array('email'=>$data['e']))->find();
        !$user && $this->_404('账号不存在！',U('members/register'));
        $points_rule = D('Task')->get_task_cache(2,1);
        $urls = array('1'=>U('company/index',array('uid'=>$this->visitor->info['uid'])),'2'=>U('personal/resume_add',array('points'=>$points_rule['points'],'first'=>1)));
        if($user['status'] == 0){
            $d = array('username'=>$data['n'],'password'=>$data['p'],'status'=>1,'email_audit'=>1);
            $passport = $this->_user_server();
            if(false === $passport->edit($user['uid'],$d)) $this->_404('账号激活失败，请重新操作！',U('members/register'));
            $user['reg_type'] = 2;
            $user['password'] = $data['p'];
            $data['i'] && $this->_incode($data['i']);
            $this->_correlation($user);
            $url = $urls[$this->visitor->info['utype']];
            $status = 0;
        }else{
            $url = 'members/login';
            $status = 1;
        }
        $this->assign('type',$status);
        $this->assign('url',U($url));
        $this->_config_seo(array('title'=>'会员注册 - '.C('qscms_site_name'),'header_title'=>'会员注册'));
        $this->display();
    }
    /**
     * [_incode 注册赠送积分]
     */
    protected function _incode($incode){
        if($incode){
            if(preg_match('/^[a-zA-Z0-9]{8}$/',$incode)){  
                $inviter_info = M('Members')->where(array('invitation_code'=>$incode))->find();
                if($inviter_info){
                    $task_id = $inviter_info['utype']==1?31:14;
                    D('TaskLog')->do_task($inviter_info,$task_id);
                }
            }
        }
    }
    /**
     * 检测用户信息是否存在或合法
     */
    public function ajax_check() {
        $type = I('post.type', 'trim', 'email');
        $param = I('post.param','','trim');
        if(in_array($type,array('username','mobile','email'))){
            $type != 'username' && !fieldRegex($param,$type) && $this->ajaxReturn(0,L($type).'格式错误！');
            $where[$type] = $param;
            $reg = M('Members')->field('uid,status')->where($where)->find();
            if($reg['uid'] && $reg['status'] != 0){
                $this->ajaxReturn(0,L($type).'已经注册');
            }else{
                $passport = $this->_user_server();
                $name = 'check_'.$type;
                if(false === $passport->$name($param)){
                    $this->ajaxReturn(0,$passport->get_error());
                }
            }
            $this->ajaxReturn(1);
        }elseif($type == 'companyname'){
            if(C('qscms_company_repeat')==0){
                $reg = M('CompanyProfile')->where(array('companyname'=>$param))->getfield('id');
                $reg ? $this->ajaxReturn(0,'企业名称已经注册') : $this->ajaxReturn(1);
            }else{
                $this->ajaxReturn(1);
            }
        }
    }
    /**
     * [save_username 修改账户名]
     */
    public function save_username(){
        if(IS_POST){
            $user['username']=I('post.username','','trim,badword');
            $passport = $this->_user_server();
            if(false === $uid = $passport->edit(C('visitor.uid'),$user)) $this->ajaxReturn(0,$passport->get_error());
            $this->visitor->update();//刷新会话
            $this->ajaxReturn(1,'用户名修改成功！');
        }
    }
    /**
     * [save_password 修改密码]
     */
    public function save_password(){
        if(IS_POST){
            $oldpassword=I('post.oldpassword','','trim,badword');
            !$oldpassword && $this->ajaxReturn(0,'请输入原始密码!');
            $password=I('post.password','','trim,badword');
            !$password && $this->ajaxReturn(0,'请输入新密码！');
            if($password != I('post.password1','','trim,badword')) $this->ajaxReturn(0,'两次输入密码不相同，请重新输入！');
            $data['oldpassword'] = $oldpassword;
            $data['password'] = $password;
            $reg = D('Members')->save_password($data,C('visitor'));
            !$reg['state'] && $this->ajaxReturn(0,$reg['error']);
            $this->ajaxReturn(1,'密码修改成功！');
        }
    }
    /**
     * [send_code 验证邮箱_发送验证链接]
     */
    public function send_email_varify_url(){
        $email=I('post.email','','trim,badword');
        if(!fieldRegex($email,'email')) $this->ajaxReturn(0,'邮箱格式错误!');
        $user=M('Members')->field('uid,email,email_audit')->where(array('email'=>$email))->find();
        $user && $user['uid'] <> C('visitor.uid') && $this->ajaxReturn(0,'邮箱已经存在,请填写其他邮箱!');
        if($user['email'] && $user['email_audit'] == 1 && $user['email'] == $email) $this->ajaxReturn(0,"你的邮箱 {$email} 已经通过验证！");
        if(session('verify_email.time') && (time()-session('verify_email.time'))<60) $this->ajaxReturn(0,'请60秒后再进行验证！');
        $token = encrypt(C('visitor.uid')).'-'.encrypt($email).'-'.time();
        $send_mail['send_type']='set_auth';
        $send_mail['sendto_email']=$email;
        $send_mail['subject']='set_auth_title';
        $send_mail['body']='set_auth';
        $replac_mail['auth_url']=build_mobile_url(array('c'=>'Members','a'=>'varify_email','params'=>'token='.$token));
        if (true === $reg = D('Mailconfig')->send_mail($send_mail,$replac_mail)){
            $this->ajaxReturn(1,'验证邮件发送成功！');
        }else{
            $this->ajaxReturn(0,$reg);
        }
    }
    /**
     * 邮箱链接验证
     */
    public function varify_email(){
        $token = I('get.token','','trim');
        $return_url_arr = array('1'=>U('Company/index'),'2'=>U('Personal/index'));
        if($token){
            $token = str_replace(C('URL_HTML_SUFFIX'), '', $token);
            $verify = explode("-", $token);
            $uid = decrypt($verify[0]);
            $email = decrypt($verify[1]);
            $time = $verify[2];
            if($time+3600*24>=time()){//24小时内有效
                $userinfo = D('Members')->where(array('uid'=>$uid))->find();
                if(!$userinfo){
                    $this->_404('邮箱验证失败!',$return_url_arr[$userinfo['utype']]);
                }
                if($userinfo['email_audit'] && $userinfo['email'] == $email){
                    $this->email_varify_success($return_url_arr[$userinfo['utype']]);
                    return;
                }
                $setsqlarr['email']=$email;
                $setsqlarr['email_audit']=1;
                $passport = $this->_user_server();
                if(false === $passport->edit($uid,$setsqlarr)) $this->_404('邮箱验证失败!',$return_url_arr[$userinfo['utype']]);
                $user_visitor = new \Common\qscmslib\user_visitor;
                $user_visitor->logout();
                $user_visitor->assign_info($userinfo);
                D('Members')->update_user_info($setsqlarr,$userinfo);
                if ($userinfo['utype']=="1"){
                    $rule=D('Task')->get_task_cache($userinfo['utype'],23);
                    $r = D('TaskLog')->do_task($userinfo,23);
                }else{
                    $rule=D('Task')->get_task_cache($userinfo['utype'],16);
                    $r = D('TaskLog')->do_task($userinfo,16);
                }
                write_members_log($userinfo,'','邮箱验证通过（邮箱：'.$email.'）');
                if($r['data']){
                    $sub = '增加'.$r['data'].C('qscms_points_byname');
                }else{
                    $sub = ''; 
                }
                $this->email_varify_success($return_url_arr[$userinfo['utype']]);
            }else{
                $this->_404('该链接已过期',$return_url_arr[$userinfo['utype']]);
            }
        }
        else
        {
            $this->_404('链接无效',$return_url_arr[$userinfo['utype']]);
        }
    }
    public function email_varify_success($url){
        $this->assign('return_url',$url);
        $this->_config_seo(array('title'=>'邮箱认证 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'邮箱认证'));
        $this->display('email_varify_success');
    }
    /**
     * [send_mobile_code 发送手机验证码]
     */
    public function send_mobile_code(){
        $mobile=I('post.mobile','','trim,badword');
        if(!fieldRegex($mobile,'mobile')) $this->ajaxReturn(0,'手机格式错误!');
        $user=M('Members')->field('uid,mobile,mobile_audit')->where(array('mobile'=>$mobile))->find();
        $user['uid'] && $user['uid']<>C('visitor.uid') && $this->ajaxReturn(0,'手机号已经存在,请填写其他手机号!');
        if($user['mobile'] && $user['mobile_audit'] == 1 && $user['mobile'] == $mobile) $this->ajaxReturn(0,"你的手机号 {$mobile} 已经通过验证！");
        if(session('verify_mobile.time') && (time()-session('verify_mobile.time'))<180) $this->ajaxReturn(0,'请180秒后再进行验证！');
        $rand=getmobilecode();
        $sendSms = array('mobile'=>$mobile,'tpl'=>'set_mobile_verify','data'=>array('rand'=>$rand.'','sitename'=>C('qscms_site_name')));
        if (true === $reg = D('Sms')->sendSms('captcha',$sendSms)){
            session('verify_mobile',array('mobile'=>$mobile,'rand'=>$rand,'time'=>time()));
            $this->ajaxReturn(1,'验证码发送成功！');
        }else{
            $this->ajaxReturn(0,$reg);
        }
    }
    /**
     * [verify_mobile_code 验证手机验证码]
     */
    public function verify_mobile_code(){
        $verifycode=I('post.verifycode',0,'intval');
        $verify = session('verify_mobile');
        if (!$verifycode || !$verify['rand'] || $verifycode<>$verify['rand']) $this->ajaxReturn(0,'验证码错误!');
        $setsqlarr['mobile'] = $verify['mobile'];
        $setsqlarr['mobile_audit']=1;
        $uid=C('visitor.uid');
        $user = M('Members')->where(array('uid'=>$uid,'mobile'=>$verify['mobile'],'mobile_audit'=>1))->find();
        if($user) $this->ajaxReturn(0,"你的手机 {$verify['mobile']} 已经通过验证！");
        $passport = $this->_user_server();
        if(false === $passport->edit($uid,$setsqlarr)) $this->ajaxReturn(0,'手机验证失败!');
        D('Members')->update_user_info($setsqlarr,C('visitor'));
        if(C('visitor.utype')=='1'){
            $rule=D('Task')->get_task_cache(C('visitor.utype'),22);
            D('TaskLog')->do_task(C('visitor'),22);
        }else{
            $rule=D('Task')->get_task_cache(C('visitor.utype'),7);
            D('TaskLog')->do_task(C('visitor'),7);
        }
        write_members_log(C('visitor'),'','手机验证通过（手机号：'.$verify['mobile'].'）');
        session('verify_mobile',null);
        $this->ajaxReturn(1,'手机验证通过!',array('mobile'=>$verify['mobile'],'points'=>$rule['points']));
    }
    /**
     * [user_getpass 忘记密码]
     */
    public function user_getpass(){
        if(IS_POST){
            $type = I('post.type',0,'intval');
            $array = array(1 => 'mobile',2 => 'email');
            if(!$reg = $array[$type]) $this->ajaxReturn(0,'请正确选择找回密码方式！');
            $retrievePassword = session('retrievePassword');
            if($retrievePassword['token'] != I('post.token','','trim')) $this->ajaxReturn(0,'非法参数！');
            if($type == 1){
                $mobile = I('post.mobile',0,'trim');
                if(!$user = M('Members')->field('uid,username')->where(array('mobile'=>$mobile,'mobile_audit'=>1))->find()) $this->_404('该手机号没有绑定账号！');
                $smsVerify = session('getpass_smsVerify');
                if($mobile != $smsVerify['mobile']) $this->ajaxReturn(0,'手机号不一致！');//手机号不一致
                if(time()>$smsVerify['time']+600) $this->ajaxReturn(0,'验证码过期！');//验证码过期
                $vcode_sms = I('post.mobile_vcode',0,'intval');
                $mobile_rand=substr(md5($vcode_sms), 8,16);
                if($mobile_rand!=$smsVerify['rand']) $this->ajaxReturn(0,'验证码错误！');//验证码错误！
                session('smsVerify',null);
                $token=substr(md5(getmobilecode()), 8,16);
                session('retrievePassword',array('uid'=>$user['uid'],'token'=>$token));
                $this->ajaxReturn(1,'',array('url'=>U('Members/user_setpass',array('token'=>$token))));
            }else{
                $email = I('post.email',0,'trim');
                if(!$user = M('Members')->field('uid,username')->where(array('email'=>$email,'email_audit'=>1))->find()) $this->ajaxReturn(0,'该邮箱没有绑定账号！');
                $time=time();
                $key=substr(md5($email.$time),8,16);
                $str = encrypt(http_build_query(array('e'=>$email,'k'=>$key,'t'=>$time)),C('PWDHASH'));
                $email_str.="{$user['username']}您好：<br>";
                $email_str.="请在24小时内点击以下链接重新设置您的密码：<br>";
                $url = C('qscms_site_domain').U('members/user_setpass',array('key'=>$str),true,false,true);
                $email_str.="<a href='".$url."' target='_blank'>".$url."</a><br>";
                $email_str.="如果链接无法点击,请复制粘贴到浏览器访问！<br>";
                $email_str.="本邮件由系统发出,请勿回复！<br>";
                $email_str.="如有任何疑问请联系网站官方：".C('qscms_top_tel');
                $email_data = array('sendto_email'=>$email,'subject'=>C('qscms_site_name')." - 会员找回密码",'body'=>$email_str);
                if(true !== $reg = D('Mailconfig')->send_mail($email_data)) $this->ajaxReturn(0,$reg);
                $this->ajaxReturn(1,'',array('url'=>U('Members/user_retrieve_email',array('email'=>$email))));
            }
        }else{
            $type = I('get.type',1,'intval');
            if($type==1){
                $tpl = 'user_getpass';
            }else{
                $tpl = 'user_getpass_email';
            }
        }
        $token=substr(md5(getmobilecode()), 8,16);
        session('retrievePassword',array('uid'=>$user['uid'],'token'=>$token));
        $this->assign('token',$token);
        $this->_config_seo(array('title'=>'找回密码 - '.C('qscms_site_name'),'header_title'=>'找回密码'));
        $this->display($tpl);
    }
    /**
     * [find_pwd 重置密码]
     */
    public function user_setpass(){
        if(IS_POST){
            $retrievePassword = session('retrievePassword');
            if($retrievePassword['token'] != I('post.token','','trim')) $this->ajaxReturn(0,'非法参数！');
            $user['password']=I('post.password','','trim,badword');
            !$user['password'] && $this->ajaxReturn(0,'请输入新密码！');
            if($user['password'] != I('post.password1','','trim,badword')) $this->ajaxReturn(0,'两次输入密码不相同，请重新输入！');
            $passport = $this->_user_server();
            if(false === $passport->edit($retrievePassword['uid'],$user)) $this->ajaxReturn(0,$passport->get_error());
            session('retrievePassword',null);
            $this->ajaxReturn(1,'重置密码成功！',array('url'=>U('Members/user_setpass_success')));
        }else{
            $key = I('get.key','','trim');
            if($key){
                parse_str(decrypt($key,C('PWDHASH')),$data);
                !fieldRegex($data['e'],'email') && $this->_404('找回密码失败,邮箱格式错误！','user_getpass');
                $end_time=$data['t']+24*3600;
                if($end_time<time()) $this->_404('找回密码失败,链接过期!','user_getpass');
                $key_str=substr(md5($data['e'].$data['t']),8,16);
                if($key_str!=$data['k']) $this->_404('找回密码失败,key错误!','user_getpass');
                if(!$uid = M('Members')->where(array('email'=>$data['e']))->getfield('uid')) $this->_404('找回密码失败,账号不存在!','user_getpass');
                $token=substr(md5(getmobilecode()), 8,16);
                session('retrievePassword',array('uid'=>$uid,'token'=>$token));
                $this->assign('token',$token);
            }else{
                $token = I('get.token','','trim');
                $this->assign('token',$token);
            }
        }
        $this->_config_seo(array('title'=>'找回密码 - '.C('qscms_site_name'),'header_title'=>'找回密码'));
        $this->display($tpl);
    }
    /**
     * [user_retrieve_email description]
     */
    public function user_retrieve_email(){
        $email = I('get.email','','trim');
        $this->assign('email',$email);
        $this->_config_seo(array('title'=>'找回密码 - '.C('qscms_site_name'),'header_title'=>'找回密码'));
        $this->display();
    }
    /**
     * 重置密码成功
     */
    public function user_setpass_success(){
        $this->_config_seo(array('title'=>'找回密码 - '.C('qscms_site_name'),'header_title'=>'找回密码'));
        $this->display();
    }
    /**
     * 账号申诉
     */
    public function appeal_user(){
        $mod = D('MembersAppeal');
        if(IS_POST && IS_AJAX){
            if (false === $data = $mod->create()) {
                $this->ajaxReturn(0, $mod->getError());
            }
            if (false !== $mod->add($data)) {
                $this->ajaxReturn(1, L('operation_success'));
            } else {
                $this->ajaxReturn(0, L('operation_failure'));
            }
        }
        $this->_config_seo(array('title'=>'账号申诉 - '.C('qscms_site_name'),'header_title'=>'账号申诉'));
        $this->display();
    }
    /**
     * [sign_in 签到]
     */
    public function sign_in(){
        if(IS_AJAX){
            $reg = D('Members')->sign_in(C('visitor'));
            if($reg['state']){
                write_members_log(C('visitor'),'','成功签到');
                $this->ajaxReturn(1,'成功签到！',$reg['points']);
            }else{
                $this->ajaxReturn(0,$reg['error']);
            }
        }
    }
    
    /**
     * [binding 第三方绑定]
     */
    public function apilogin_binding(){
        $this->_bind_info();
        $this->_config_seo(array('title'=>'账号绑定 - '.C('qscms_site_name'),'header_title'=>'账号绑定'));
        $this->display();
    }
    public function bind_reg(){
        $this->_bind_info();
        $this->_config_seo(array('title'=>'个人账号注册 - '.C('qscms_site_name'),'header_title'=>'个人账号注册'));
        $this->display();
    }
    public function bind_reg_com(){
        $this->_bind_info();
        $this->_config_seo(array('title'=>'企业账号注册 - '.C('qscms_site_name'),'header_title'=>'企业账号注册'));
        $this->display();
    }
    protected function _bind_info(){
        if('qianfanyunapp' == I('get.qianfan','','trim')){
            $user_bind_info = object_to_array(cookie('members_bind_info'));
            if(!$this->visitor->is_login && !$user_bind_info) $this->redirect('index/index');
        }else if('magappx' == I('get.type','','trim')){
            $user_bind_info = object_to_array(cookie('members_bind_info'));
            if(!$this->visitor->is_login && !$user_bind_info) $this->redirect('index/index');
        }else if($openid = I('get.openid','','trim')){
            $reg = \Common\qscmslib\weixin::get_user_info($openid);
            $user_bind_info = $reg['data'];
        }else{
            $user_bind_info = object_to_array(cookie('members_bind_info'));
            if(!$this->visitor->is_login && !$user_bind_info) $this->redirect('members/login');
        }
        if(false === $oauth_list = F('oauth_list')){
            $oauth_list = D('Oauth')->oauth_cache();
        }
        if(C('apply.Magappx')){
            $oauth_list['magappx']['name'] = C('qscms_magappx_app_name')?:'马甲APP';
        }
        if(C('apply.Qianfanyunapp')){
            $oauth_list['qianfanyunapp']['name'] = C('qscms_qianfanyunapp_name')?:'千帆APP';
        }
        $this->assign('third_name',$oauth_list[$user_bind_info['type']]['name']);
        $this->assign('user_bind_info', $user_bind_info);
    }
    public function ucenter_bind_reg(){
        $user_bind_info = object_to_array(cookie('members_uc_info'));
        if(!$this->visitor->is_login && !$user_bind_info) $this->redirect('members/login');
        $user_bind_info['keyavatar_big'] = attach('no_photo_male.png','resource');
        $this->assign('third_name','Ucenter');
        $this->assign('user_bind_info', $user_bind_info);
        $this->_config_seo(array('title'=>'个人账号注册 - '.C('qscms_site_name'),'header_title'=>'个人账号注册'));
        $this->display();
    }
    public function ucenter_bind_reg_com(){
        $user_bind_info = object_to_array(cookie('members_uc_info'));
        if(!$this->visitor->is_login && !$user_bind_info) $this->redirect('members/login');
        $user_bind_info['keyavatar_big'] = attach('no_photo_male.png','resource');
        $this->assign('third_name','Ucenter');
        $this->assign('user_bind_info', $user_bind_info);
        $this->_config_seo(array('title'=>'企业账号注册 - '.C('qscms_site_name'),'header_title'=>'企业账号注册'));
        $this->display();
    }
    /**
     * [oauth_reg 第三方登录注册]
     */
    public function oauth_reg(){
        if (cookie('members_bind_info')) {
            $user_bind_info = object_to_array(cookie('members_bind_info'));
        }else{
            $this->_404('第三方授权失败，请重新操作！');
        }
        //第三方账号绑定
        $username = I('post.username','','trim');
        $password = I('post.password','','trim');
        $passport = $this->_user_server();
        if(false === $uid = $passport->uc('default')->auth($username, $password)){
            if($err == L('auth_null') && $passport->is_sitegroup() && false !== $passport->uc('sitegroup')->auth($username, $password)){
                $user = $passport->_user;
                if($user['utype']==1){
                    $company_mod = D('CompanyProfile');
                    $company_mod->create($user);
                }
                if(false === $user = $passport->uc('default')->register($user)){
                    if($user = $passport->get_status()){
                        $uid = $user['uid'];
                        $email_activate = true;
                    }else{
                        $this->_404($passport->get_error());
                    }
                }else{
                    $uid = $user['uid'];
                    // 添加企业信息
                    if($user['utype']==1){
                        $company_mod->uid=$user['uid'];
                        C('SUBSITE_VAL.s_id') && $company_mod->subsite_id = C('SUBSITE_VAL.s_id');
                        $insert_company_id = $company_mod->add();
                        if($insert_company_id){
                            switch($com_setarr['audit']){
                                case 1:
                                    $audit_str = '认证通过';break;
                                case 2:
                                    $audit_str = '认证中';break;
                                case 3:
                                    $audit_str = '认证未通过';break;
                                default:
                                    $audit_str = '';break;
                            }
                            if($audit_str){
                                $auditsqlarr['company_id']=$insert_company_id;
                                $auditsqlarr['reason']='自动设置';
                                $auditsqlarr['status']=$audit_str;
                                $auditsqlarr['addtime']=time();
                                $auditsqlarr['audit_man']='系统';
                                M('AuditReason')->data($auditsqlarr)->add();
                            }
                        }
                    }
                    D('Members')->user_register($user);
                    $this->_correlation($user);
                    $points_rule = D('Task')->get_task_cache(2,1);
                    $login = true;
                }
            }else{
                $this->_404($passport->get_error());
            }
        }else{
            $user = $passport->_user;
            if($user['utype'] == 1 && (!$user['sitegroup_uid'] || !$user['uc_uid'])){
                $company = M('CompanyProfile')->field('companyname,contact,landline_tel')->where(array('uid'=>$user['uid']))->find();
                $user = array_merge($user,$company);
            }
            if(!$user['sitegroup_uid'] && $passport->is_sitegroup()){
                $temp = $passport->uc('sitegroup')->register($user);
                $temp && M('Members')->where(array('uid'=>$user['uid']))->setfield('sitegroup_uid',$temp['sitegroup_uid']);
            }
            if(!$user['uc_uid'] && !$passport->is_sitegroup() && $passport->is_uc()){
                $temp = $passport->uc('ucenter')->register($user);
                $temp && M('Members')->where(array('uid'=>$user['uid']))->setfield('uc_uid',$temp['uc_uid']);
            }
        }
        if(!$email_activate && !$login){
            if(false === $this->visitor->login($uid)) $this->_404($this->visitor->getError());
            $passport->synlogin($uid);
        }
        if($user_bind_info['type'] == 'weixin'){
            $openid = $user_bind_info['openid']?:I('request.openid','','trim');
            $reg = \Common\qscmslib\weixin::bind($openid,$user);
            if(!$reg['state']) $this->_404($reg['tip']);
            if(!$this->visitor->get('avatars') && $user_bind_info['keyavatar_big']) $reg = M('Members')->where(array('uid'=>$uid))->setfield('avatars',$user_bind_info['keyavatar_big']);//临时头像转换
        }else{
            $oauth = new \Common\qscmslib\oauth($user_bind_info['type']);
            $bind_user = $oauth->_checkBind($user_bind_info['type'], $user_bind_info);
            if($bind_user['uid'] && $bind_user['uid'] != $uid) $this->_404('此账号已经绑定过本站！');
            $user_bind_info['uid'] = $uid;
            if(false === $oauth->bindUser($user_bind_info)) $this->_404('账号绑定失败，请重新操作！');
            if(!$this->visitor->get('avatars') && $user_bind_info['temp_avatar']) $this->_save_avatar($user_bind_info['temp_avatar'],$uid);//临时头像转换
        }
        cookie('members_bind_info', NULL);//清理绑定COOKIE
        $urls = array(1=>'company/index',2=>'personal/index',3=>'members/reg_email_activate');
        $type = $email_activate ? 3 : $this->visitor->info['utype'];
        $this->redirect($urls[$type],array('uid'=>$uid));
    }
    /**
     * [_save_avatar 第三方头像保存]
     */
    protected function _save_avatar($avatar,$uid){
        $path = C('qscms_attach_path').'avatar/temp/'.$avatar;
        $image = new \Common\ORG\ThinkImage();
        $date = date('ym/d/');
        $save_avatar=C('qscms_attach_path').'avatar/'.$date;//图片存储路径
        if(!is_dir($save_avatar)) mkdir($save_avatar,0777,true);
        $savePicName = md5($uid.time()).".jpg";
        $filename = $save_avatar.$savePicName;
        $size = explode(',',C('qscms_avatar_size'));
        copy($path, $filename);
        foreach ($size as $val) {
            $image->open($path)->thumb($val,$val,3)->save("{$filename}._{$val}x{$val}.jpg");
        }
        M('Members')->where(array('uid'=>$uid))->setfield('avatars',$date.$savePicName);
        @unlink($path);
    }
    /**
     * [get_weixin 获取微信]
     */
    public function get_weixin(){
        if(!C('qscms_weixin_apiopen') || !C('qscms_weixin_mpname')) $this->ajaxReturn(0,'未配置微信参数！');
        $this->ajaxReturn(1,'请微信关注  '.C('qscms_weixin_mpname').' 公众号进行账号绑定');
    }
}
?>