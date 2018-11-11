<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class ImController extends MobileController{
	// 初始化函数
	public function _initialize(){
		parent::_initialize();
		if(!$this->visitor->is_login) {
            IS_AJAX && $this->ajaxReturn(0, L('login_please'),'',1);
            //非ajax的跳转页面
            $this->redirect('members/login');
        }
	}
	/**
	 * [add_message 新增消息]
	 */
	public function add_message(){
		if(IS_AJAX && IS_POST){
			$reg = D('ImMessage')->add_message();
			$this->ajaxReturn($reg['state'],$reg['error'],$reg['message']);
		}
	}
	/**
	 * [user_list 历史会话列表]
	 */
	public function user_list(){
		$this->assign('userList',D('ImUser')->get_user_list());
		$this->_config_seo(array('title'=>'在线咨询 - '.C('qscms_site_name'),'header_title'=>'在线咨询'));
		$this->display();
	}
	/**
	 * [read_message 标记当剪卡会话为已读消息]
	 */
	public function read_message(){
		if(IS_AJAX && IS_POST){
			$uid = I('post.uid',0,'intval');
			$reg = D('ImUser')->read_message($uid);
			$this->ajaxReturn($reg['state'],$reg['error']);
		}
	}
	/**
	 * [unread_message 读取未读消息数]
	 */
	public function unread_message(){
		if(IS_AJAX && IS_GET){
			$reg = D('ImUser')->unread_message();
			$this->ajaxReturn(1,'',$reg);
		}
	}
	/**
	 * [del_dialog 删除会话]
	 */
	public function del_dialog(){
		if(IS_AJAX && IS_GET){
			$uid = I('get.uid',0,'intval');
			$reg = D('ImUser')->del_dialog($uid);
			$this->ajaxReturn($reg['state'],$reg['error']);
		}
	}
	/**
	 * [message 会话]
	 */
	public function message(){
        $name = C('visitor.utype') == 1 ? 'message_personal' : 'message_company';
        $this->$name();
		$this->display($name);
	}
	protected function message_personal(){
		$id = I('get.id',0,'intval');
		$uid = I('get.uid',0,'intval');
		if(!$id && !$uid)$this->_404('请选择用户！');
		if($uid) $where['uid'] = $uid;
		if($id){
			$where['id'] = $id;
		}else{
			$order = 'def desc,id asc';
		}
		$resume = M('Resume')->where($where)->order($order)->find();
		if($resume){
			$down_resume = D('CompanyDownResume')->check_down_resume($resume['id'],C('visitor.uid'));
            $jobs_apply = D('PersonalJobsApply')->check_jobs_apply($resume['id'],C('visitor.uid'));
            $resume['show_contact'] = $this->_get_show_contact($resume,$down_resume,$jobs_apply);
			if($resume['photo_img']){
				$resume['photo_img'] = attach($resume['photo_img'],'avatar');
			}else{
				$avatar_default = $resume['sex']==1?'no_photo_male.png':'no_photo_female.png';
				$resume['photo_img'] = attach($avatar_default,'resource');
			}
			$sendUser = array('uid'=>$resume['uid'],'username'=>$resume['fullname'],'avatars'=>$resume['photo_img']);
		}else{
			$sendUser = M('Members')->field('uid,username,avatars')->where(array('uid'=>$uid))->find();
			if($sendUser['avatars']){
                $sendUser['avatars'] = attach($sendUser['avatars'],'avatar');
            }else{
                $sex = M('Resume')->where(array('uid'=>$uid,'def'=>1))->limit(1)->getfield('sex');
                $avatar_default = $sex==1?'no_photo_male.png':'no_photo_female.png';
                $sendUser['avatars'] = attach($avatar_default,'resource');
            }
		}
		$uid = $uid?:$resume['uid'];
		$reg = D('ImUser')->get_user_info($uid);
		if(!$reg['state']) $this->_404($reg['error']);
        $this->assign('ronguser',$reg['user']);
		$this->assign('resume',$resume);
		$this->assign('sendUser',$sendUser);
		$message = D('ImMessage')->get_message($uid);
        if($message['state']){
			$this->assign('message',$message['data']);
        }
		$this->_config_seo(array('title'=>'在线咨询 - '.C('qscms_site_name'),'header_title'=>$sendUser['username']));
	}
	protected function message_company(){
		$id = I('get.id',0,'intval');
		$uid = I('get.uid',0,'intval');
		if(!$id && !$uid)$this->_404('请选择用户！');
		if($uid) $where['uid'] = $uid;
		if($id) $where['id'] = $id;
		//当前用户的企业信息 
        $company_profile = M('CompanyProfile')->where($where)->find();
        $uid = $uid?:$company_profile['uid'];
        $reg = D('ImUser')->get_user_info($uid);
		if(!$reg['state']) $this->_404($reg['error']);
        // 顾问信息
        if(C('visitor.consultant')>0){
            $consultant = M('Consultant')->where(array('id'=>$reg['user']['consultant']))->find();
            $this->assign('consultant',$consultant);
        }
        $company_profile['logo'] = $company_profile['logo'] ? attach($company_profile['logo'],'company_logo') : attach('no_logo.png','resource');
        $message = D('ImMessage')->get_message($uid);
        if($message['state']){
			$this->assign('message',$message['data']);
        }
        $this->assign('ronguser',$reg['user']);
        $this->assign('company_profile',$company_profile);
        $this->_config_seo(array('title'=>'在线咨询 - '.C('qscms_site_name'),'header_title'=>$company_profile['companyname']));
	}
	/**
     * 是否显示联系方式
     */
    protected function _get_show_contact($val,$down,$apply){
        $show_contact = 0;
        //情景1：游客访问
        if(!C('visitor')){
			if(MODULE_NAME == 'Home' && C('qscms_showresumecontact')==0){
                $show_contact = 1;
            }
			if(MODULE_NAME == 'Mobile' && C('qscms_showresumecontact_wap')==0){
                $show_contact = 1;
            }
        }
        //情景2：个人会员访问并且是该简历发布者
        else if(C('visitor.utype')==2 && C('visitor.uid')==$val['uid'])
        {
            $show_contact = 1;
        }
        //情景3：企业会员访问
        else if(C('visitor.utype')==1)
        {
            //情景3-1：其他企业会员
            if(MODULE_NAME == 'Home' && C('qscms_showresumecontact')==1){
                $show_contact = 1;
            }
            if(MODULE_NAME == 'Mobile' && C('qscms_showresumecontact_wap')==1){
                $show_contact = 1;
            }
            //情景3-2：下载过该简历
            if($down){
                $show_contact = 1;
            }
            //情景3-3：该简历申请过当前企业发布的职位
            $setmeal=D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
            if($apply && $setmeal['show_apply_contact']=='1'){
                $show_contact = 1;
            }
        }
        return $show_contact;
    }
	/**
	 * [get_message AJAX获取聊天内容]
	 */
	public function get_message(){
		if(IS_AJAX && IS_GET){
			$uid = I('get.uid',0,'intval');
			if(C('visitor.utype') == 1){
				$avatars = M('Members')->where(array('uid'=>$uid))->getfield('avatars');
				if($avatars){
					$sendUser['avatars'] = attach($avatars,'avatar');
				}else{
					$sex = M('Resume')->where(array('uid'=>$uid,'def'=>1))->limit(1)->getfield('sex');
	                $avatar_default = $sex==1?'no_photo_male.png':'no_photo_female.png';
	                $sendUser['avatars'] = attach($avatar_default,'resource');
				}
			}else{
				$avatars = M('CompanyProfile')->where(array('uid'=>$uid))->getfield('logo');
				$sendUser['avatars'] = $avatars ? attach($avatars,'company_logo') : attach('no_logo.png','resource');
			}
			$message = D('ImMessage')->get_message($uid);
			if($message['state']){
				$this->assign('sendUser',$sendUser);
				$this->assign('message',$message['data']);
				$message['data']['html']=$this->fetch('ajax_message');
	        }
	        unset($message['data']['list']);
			$this->ajaxReturn($message['state'],$message['error'],$message['data']);
		}
	}
}
?>