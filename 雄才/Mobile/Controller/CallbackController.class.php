<?php
// +----------------------------------------------------------------------
// | 74CMS [ WE CAN DO IT JUST THINK IT ]
// +----------------------------------------------------------------------
// | Copyright (c) 2009 http://www.74cms.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed ( http://www.apache.org/licenses/LICENSE-2.0 )
// +----------------------------------------------------------------------
// | Author: 
// +----------------------------------------------------------------------
// | ActionName: 系统第三方回调
// +----------------------------------------------------------------------
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class CallbackController extends MobileController{
	/**
     * 第三方账号登录和绑定
     * @mod qq sina taobao  weixin
     * @type 操作类型 login bind unbind
     */
    public function index() {
    	$mod = I('get.mod','','trim');
    	$type = I('get.type', 'login','trim');
    	!$mod && $this->_error('请选择正确的第三方服务！');
        if ('unbind' == $type) {
            !$this->visitor->is_login && $this->redirect('members/login');
            if($mod == 'weixin'){
                if(false === M('MembersBind')->where(array('uid'=>C('visitor.uid'), 'type'=>$mod))->save(array('uid'=>0,'is_bind'=>0,'bindingtime'=>0))) $this->_404('解除绑定失败，请重新操作！');
            }else{
                if(false === M('MembersBind')->where(array('uid'=>C('visitor.uid'), 'type'=>$mod))->delete()) $this->_404('解除绑定失败，请重新操作！');
            }
            $urls = array('1'=>'company/com_binding','2'=>'personal/per_binding');
            $this->redirect($urls[C('visitor.utype')]);
        }
        $oauth = new \Common\qscmslib\oauth($mod);
        cookie('callback_type', $type);
        return $oauth->authorize();
    }
	/**
	 * 第三方登录和绑定回调
	 */
	public function oauth(){
        if(I('get.error_uri','','trim') || I('get.error','','trim') || I('get.error_code','','trim')){
            $this->redirect('members/index');
        }
		$mod = I('get.mod','','trim');
        !$mod && $this->_404('请选择正确的第三方服务！');
        $callback_type = cookie('callback_type');
        $oauth = new \Common\qscmslib\oauth($mod);
        $rk = $oauth->NeedRequest();
        $request_args = array();
        foreach ($rk as $v) {
            $request_args[$v] =I('get.'.$v);
        }
        switch ($callback_type) {
            case 'login':
                $url = $oauth->callbackLogin($request_args);
                break;
            case 'bind':
                $url = $oauth->callbackbind($request_args);
                break;
            default:
                $url = __ROOT__;
                break;
        }
        cookie('callback_type', null);
        if(false === $url) $this->_404($oauth->getError(),__ROOT__);
        redirect($url);
	}

    /**
     * 检测微信h5支付是否成功
     */
    public function check_h5pay_status(){
        $order_id = I('get.order_id',0,'intval');
        $order = D('Order')->find($order_id);
        if($order['is_paid']==2){
            $this->ajaxReturn(1);
        }else{
            $this->ajaxReturn(0);
        }
    }
}
?>