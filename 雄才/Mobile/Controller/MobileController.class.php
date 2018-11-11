<?php
/**
 * 触屏控制器基类
 *
 * @author andery
 */
namespace Mobile\Controller;
use Common\Controller\FrontendController;
class MobileController extends FrontendController {
    protected $visitor = null;
    protected $openid = '';
    protected $is_weixin;
    public function _initialize() {
        parent::_initialize();
        //网站状态
        if (C('qscms_mobile_isclose')) {
            header('Content-Type:text/html; charset=utf-8');
            exit('触屏端网站关闭:'.C('qscms_mobile_close_reason'));
        }
        if(!IS_AJAX && C('apply.Magappx') && !$this->visitor->is_login && C('qscms_magappx_secret') && CONTROLLER_NAME != 'Captcha') $this->magappx();
        if(!IS_AJAX && C('apply.Qianfanyunapp') && !$this->visitor->is_login && C('qscms_qianfanyunapp_secret') && CONTROLLER_NAME != 'Captcha') $this->qianfanyunapp();
        if($this->visitor->is_login){
            $reg = D('ImUser')->get_user_info($uid);
            if($reg['state']){
                $this->assign('ronguser',$reg['user']);
            }
        }
        $this->is_weixin = $this->is_weixin();
        $this->assign('is_weixin',$this->is_weixin);
    }
    public function is_weixin(){
        if(strpos($_SERVER['HTTP_USER_AGENT'], 'MicroMessenger') !== false) {  
            return true;  
        }
        return false;
    } 
    /**
     * 检测是否是微信，如果是微信，有openid，并且绑定，自动登录
     */
    public function check_weixin_login($openid=''){
        $openid = $openid?:I('request.openid','','trim');
        $this->openid = $openid;
        if(strpos($_SERVER['HTTP_USER_AGENT'], 'MicroMessenger') !== false && $openid) {
            $this->assign('openid',$openid);
            $userbind = D('MembersBind')->where(array('type'=>'weixin','openid'=>$openid))->find();
            if(!$userbind){
                $reg = \Common\qscmslib\weixin::get_user_info($openid);
                if($reg['state'] && $reg['data']['unionid']){
                    $reg['data']['info'] = $reg['data']['bind_info'];
                    $userbind = M('MembersBind')->where(array('type'=>'weixin','unionid'=>$reg['data']['unionid']))->find();
                    if(!$userbind){
                        M('MembersBind')->add($reg['data']);
                    }else{
                        $reg['data']['info'] = serialize(array_merge(unserialize($userbind['info']),unserialize($reg['data']['info'])));
                        $userbind['uid'] && $reg['data']['is_bind'] = 1;
                        M('MembersBind')->where(array('type'=>'weixin','unionid'=>$reg['data']['unionid']))->save($reg['data']);
                        $userbind = array_merge($userbind,$reg['data']);
                    }
                }
            }elseif(!$userbind['is_focus'] || !$userbind['is_bind']){
                if($userbind['uid'] && !$userbind['is_bind']) $userbind['is_bind'] = $data['is_bind'] = 1;
                $userbind['is_focus'] = $data['is_focus'] = 1;
                M('MembersBind')->where(array('type'=>'weixin','openid'=>$openid))->save($data);
            }
            if(!$userbind['uid'] || !$userbind['is_bind'] || !$userbind['is_focus'] || !$userbind['openid']) $userbind = '';
            if(!$this->visitor->is_login){
                if($userbind){
                    $this->visitor->login($userbind['uid']);
                    C('visitor',$this->visitor->info);
                    $this->_init_visitor();
                    return 0;
                }
                return 1;
            }else{
                if($userbind){
                    if($userbind['uid'] != $this->visitor->info['uid']){
                        $this->visitor->login($userbind['uid']);
                        C('visitor',$this->visitor->info);
                        $this->_init_visitor();
                        return 0;
                    }
                }else{
                    $this->visitor->logout();
                    //同步退出
                    $passport = $this->_user_server();
                    $synlogout = $passport->synlogout();
                    return 1;
                }
            }
        }
        return 0;
    }
    /**
     * 获取openid
     */
    public function get_weixin_openid($code)
    {
        if($code && C('qscms_weixin_appid') && C('qscms_weixin_appsecret')){
            $url ="https://api.weixin.qq.com/sns/oauth2/access_token?appid=".C('qscms_weixin_appid')."&secret=".C('qscms_weixin_appsecret')."&code=".$code."&grant_type=authorization_code";
            $data = https_request($url);
            $data = json_decode($data,true);
            $openid = $data['openid'];
            if($openid){
                return $this->check_weixin_login($openid);
            }
        }
    }
    /**
     * 微信分享
     */
    public function wx_share(){
        $access_token = \Common\qscmslib\weixin::get_access_token();
        $jssdk = new \Common\qscmslib\Jssdk(C('qscms_weixin_appid'), C('qscms_weixin_appsecret'),$access_token);
        $signPackage = $jssdk->GetSignPackage();
        $this->assign("signPackage",$signPackage);
    }
    protected function magappx(){
        $userAgent = $_SERVER['HTTP_USER_AGENT'];
        $info = strstr($userAgent, "MAGAPPX");
        $info=explode('|',$info);
        $agent=array(
            'name'=>$info[0],
            'version'=>$info[1],//客户端版本信息 4.0.0
            'client'=>$info[2],//客户端信息可以获取区分android或ios 可以获取具体机型
            'site'=>$info[3],  //站点名称  如lxh
            'device'=>$info[4],//设备号    客户端唯一设备号
            'sign'=>$info[5],  //签名     可以验证请求是否伪造 签名算法见下面详细说明
            'signCloud'=>$info[6],//云端签名 这个签名马甲内部使用
            'token'=>$info[7]//用户token 用户的token信息
        );
        if($agent['name'] == 'MAGAPPX'){
            !$agent['token'] && $agent['token'] = I('request.magappx_token','','trim');
            !$agent['token'] && $agent['token'] = I('request.magappx','','trim');
            $this->assign('magappx',$agent);
            if($agent['token'] && !I('request.magappx','','trim')){
                $user = \Magappx\qscmslib\magappx::get_user_info($agent);
                if(false !== $user){
                    $userbind = M('MembersBind')->where(array('type'=>'magappx','keyid'=>$user['keyid']))->find();
                    if($userbind){
                        $this->visitor->login($userbind['uid']);
                        C('visitor',$this->visitor->info);
                        $this->_init_visitor();
                    }else{
                        $this->redirect('members/apilogin_binding',array('magappx'=>$agent['token']));
                    }
                }
            }
        }
    }
    protected function qianfanyunapp(){
        if(cookie('wap_token') || cookie('qianfan_deviceid') || cookie('qianfan_appversion') || cookie('qianfan_appcode')){
            $s = \Qianfanyunapp\qscmslib\qianfanyunapp::is_login();
            if(false !== $s){
                $this->assign('qianfan','qianfanyunapp');
                if($s && ACTION_NAME != 'apilogin_binding' && 'qianfanyunapp' != I('request.qianfan','','trim')){
                    $user = \Qianfanyunapp\qscmslib\qianfanyunapp::get_user_info($s);
                    if(false !== $user){
                        $userbind = M('MembersBind')->where(array('type'=>'qianfanyunapp','keyid'=>$user['keyid']))->find();
                        if($userbind){
                            $this->visitor->login($userbind['uid']);
                            C('visitor',$this->visitor->info);
                            $this->_init_visitor();
                        }else{
                            $this->redirect('members/apilogin_binding',array('qianfan'=>'qianfanyunapp'));
                        }
                    }
                }
            }
            $this->assign('qianfanyunapp_is_login',$s?0:1);
        }
    }
}