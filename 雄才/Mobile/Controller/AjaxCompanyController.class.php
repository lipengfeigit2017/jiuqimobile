<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class AjaxCompanyController extends MobileController{
	public function _initialize() {
        parent::_initialize();
        //访问者控制
        if (!$this->visitor->is_login) $this->ajaxReturn(0, L('login_please'),'',1);
        if(C('visitor.utype') !=1) $this->ajaxReturn(0,'请登录企业账号！');
    }
    /**
     * 标记简历
     */
    public function resume_label(){
        $resume_id = I('get.resume_id',0,'intval');
        $label = I('get.label',0,'intval');
        $label_type = I('get.label_type',0,'intval');
        $jobs_id = I('get.jobs_id',0,'intval');
        $where = array('resume_id'=>$resume_id,'company_uid'=>C('visitor.uid'));
        if($label_type==1){
            $model_name = 'CompanyDownResume';
        }else{
            $where['jobs_id'] = array('eq',$jobs_id);
            $model_name = 'PersonalJobsApply';
        }
        $data = D($model_name)->where($where)->find();
        if($data){
            $r = D('Resume')->company_label_resume($data['did'],$model_name,C('visitor.uid'),$label);
            $this->ajaxReturn($r['state'],$r['msg']);
        }else{
            $this->ajaxReturn(0,'参数错误！');
        }
    }
    /**
     * 下载简历
     */
    public function resume_down(){
        $rid = I('request.rid',0,'intval');
        $addarr['rid'] = array($rid);
        $r = D('CompanyDownResume')->add_down_resume($addarr,C('visitor'),true);
        $this->ajaxReturn($r['state'],$r['msg'],$r['data']);
    }
    /**
     * 下载简历确认
     */
    public function resume_down_confirm(){
        $rid = I('request.rid',0,'intval');
        if(!$rid){
            $this->ajaxReturn(0,'请选择简历！');
        }
        $my_setmeal = D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
        $my_points = D('MembersPoints')->get_user_points(C('visitor.uid'));
        $tip = '';
        $mode = '';
        $discount = D('Setmeal')->get_max_discount('download_resume');
        $downwhere['down_addtime'] = array('between',strtotime('today').','.strtotime('tomorrow'));
        $downwhere['company_uid'] = C('visitor.uid');
        $downnum = D('CompanyDownResume')->where($downwhere)->count();
        if($my_setmeal['download_resume_max']>0)
        {
            if($downnum>=$my_setmeal['download_resume_max']){
                $tip = '<div class="dialog_notice">您今天已下载 <span class="font_yellow">'.$downnum.'</span> 份简历，已达到每日下载上限，请先收藏该简历，明天继续下载。</div>';
                $mode = 'no';
                $this->ajaxReturn(1,$tip,$mode);
            }
        }
        if ($my_setmeal['download_resume']<=0)// 套餐内简历下载数已用完
        {
            if(C('qscms_resume_download_quick')==1){
				if($my_points>=C('qscms_download_resume_price')*C('qscms_payment_rate') && C('qscms_down_resume_by_points')==1){
					$tip = '<div class="dialog_notice">您的套餐内简历下载数已用完，下载该简历需要扣除 <span class="font_yellow">'.intval(C('qscms_payment_rate')*C('qscms_download_resume_price')).'</span> '.C('qscms_points_byname').'，是否继续下载？<br /><div class="dialog_tip font10 font_gray9">您当前拥有 <span class="font_yellow">'.$my_points.'</span> '.C('qscms_points_byname').'</div></div>';
					$mode = 'points';
				}else{
					$this->assign('resume_id',$rid);
					$this->assign('discount',$discount);
					$tip = $this->fetch('Ajax_tpl/guide_pay_resume');
					$mode = 'mix';
				}
			}else{
				
				$this->ajaxReturn(0,'您套餐中剩余的下载简历数量不足，请升级套餐后继续下载');
			}
        }else{
            $tip = '<div class="dialog_notice">您还可免费下载 <span>'.$my_setmeal['download_resume'].'</span> 份简历，是否继续下载？</div>';
            $mode = 'setmeal';
        }
        $this->ajaxReturn(1,$tip,$mode);
    }
    /**
     * 收藏简历
     */
    public function resume_favor(){
        $rid = I('request.rid');
        !$rid && $this->ajaxReturn(0,'请选择简历！');
        $has = $this->_check_favor($rid,C('visitor.uid'));
        if($has){
            D('CompanyFavorites')->where(array('resume_id'=>$rid,'company_uid'=>C('visitor.uid')))->delete();
            $this->ajaxReturn(1,'取消收藏成功！','has');
        }else{
        	$r = D('CompanyFavorites')->add_favorites($rid,C('visitor'));
        	$this->ajaxReturn($r['state'],$r['error']);
        }
    }
    //检测是否已收藏
    protected function _check_favor($resume_id,$uid){
        $r = D('CompanyFavorites')->where(array('resume_id'=>$resume_id,'company_uid'=>$uid))->find();
        if($r){
            return 1;
        }else{
            return 0;
        }
    }
    public function jobs_interview_add(){
        if(IS_POST){
            $data['jobs_id'] = I('post.jobs_id',0,'intval');
            $data['resume_id'] = I('post.resume_id',0,'intval');
            $date = I('post.date','','trim');
            if(!$date){
                $this->ajaxReturn(0,'请选择面试日期');
            }
            $ap = I('post.ap',1,'intval')== 1? 'AM' : 'PM';
            $time = I('post.time',0,'intval');
            if(!$time){
                $this->ajaxReturn(0,'请选择面试时间');
            }
            $data['interview_time'] = strtotime($date.' '.$time.':00:00 '.$ap);
            if($data['interview_time']<time()){
                $this->ajaxReturn(0,'面试时间不能早于当前时间');
            }
            $data['address'] = I('post.address','','trim');
            $data['contact'] = I('post.contact','','trim');
            $data['telephone'] = I('post.telephone','','trim');
            $data['notes'] = I('post.notes','','trim');
            $data['sms_notice'] = I('post.sms_notice',0,'intval');
            $reg = D('CompanyInterview')->add_interview($data,C('visitor'));
            $this->ajaxReturn($reg['state'],$reg['msg']);
        }else{
            $id = I('get.id',0,'intval');
            !$id && $this->_404('请选择简历！');
            $is_apply = 0;
            $apply = M('PersonalJobsApply')->where(array('resume_id'=>$id,'company_uid'=>C('visitor.uid')))->find();
            if($apply) $is_apply = 1;
            if(C('qscms_showresumecontact_wap') == 2){
                !$apply && $apply = M('CompanyDownResume')->where(array('resume_id'=>$id,'company_uid'=>C('visitor.uid')))->find();
                !$apply && $this->_404('请先下载简历！');
            }else{
                $apply = M('Resume')->field('id as resume_id,uid as resume_uid,fullname as resume_name')->find($id);
            }
            $company = M('CompanyProfile')->field('district_cn,contact,telephone,landline_tel,address')->where(array('uid'=>C('visitor.uid')))->find();
            $apply = array_merge($apply,$company);
            $apply['fullname'] = M('Resume')->where(array('id'=>$apply['resume_id']))->getfield('fullname');
            $jobs_map['uid'] = C('visitor.uid');
            if(C('qscms_jobs_display')==1){
                $jobs_map['audit'] = 1;
            }
            $jobs = D('Jobs')->get_jobs($jobs_map,'refreshtime desc','jobs');
            if($jobs['list']){
               $temp = current($jobs['list']);
               $default_jobs['jobs_id'] = $temp['id'];
               $default_jobs['jobs_name'] = $temp['jobs_name'];
            }else{
                $this->_404('您还没有发布职位，请先发布职位！');
            }
            $this->assign('default_jobs',$default_jobs);
            $this->assign('is_apply',$is_apply);
            $this->assign('apply',$apply);
            $this->assign('jobs',$jobs['list']);
            $this->_config_seo(array('title'=>'发送面试邀请 - '.C('qscms_site_name'),'header_title'=>'发送面试邀请'));
            $this->display('AjaxCommon/jobs_interview_add');
        }
    }
}
?>