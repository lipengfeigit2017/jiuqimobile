<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
use Allowance\Model\AllowanceInfoModel;
class AjaxAllowanceController extends MobileController{
	public function _initialize() {
        parent::_initialize();
    }
    /**
     * 领取红包检测提示
     */
    public function apply_allowance(){
        $jid = I('request.jid',0,'intval');
        !$jid && $this->ajaxReturn(0,'请选择要投递的职位！');
        $check = D('Allowance/AllowanceRecord')->check_apply($jid,C('visitor'));
        if($check===false){
            $this->ajaxReturn(0,D('Allowance/AllowanceRecord')->getError());
        }else if($check=='-1'){
            $this->ajaxReturn(2,D('Allowance/AllowanceRecord')->getError());
        }
        $jobsinfo = $check['jobsinfo'];
        $resume = $check['resume'];
        $tip = D('Allowance/AllowanceInfo')->check_intention($jobsinfo['allowance_id'],$resume);
        $this->assign('tip',$tip);
        $allowance_info = D('Allowance/AllowanceInfo')->find($jobsinfo['allowance_id']);
        $this->assign('allowance_info',$allowance_info);
        if(false===$config=F('allowance_config')){
            $config = D('Allowance/AllowanceConfig')->config_cache();
        }
        $this->assign('mobile_address',$config['allow_signon_mobile_address']?$config['allow_signon_mobile_address']:'不限');
        $this->assign('tip_status',$tip['status']);
        $this->assign('jid',$jid);
        $data['html'] = $this->fetch('apply_allowance');
        $this->ajaxReturn(1,'回调成功！',$data);
    }
    /**
     * 领取红包
     */
    public function apply_allowance_save(){
        $jid = I('request.jid',0,'intval');
        !$jid && $this->ajaxReturn(0,'请选择要投递的职位！');
        if(false===$config=F('allowance_config')){
            $config = D('Allowance/AllowanceConfig')->config_cache();
        }
        if($config['apply_need_auth_mobile']==1){
            $check_mobile_auth = M('Members')->where(array('uid'=>C('visitor.uid')))->find();
            if($check_mobile_auth['mobile_audit']=="0"){
                $this->ajaxReturn(3,'请先验证手机号！');
            }
        }
        $need_check_bind = I('get.need_check_bind',0,'intval');
        if($need_check_bind){
            $check_bind = D('Allowance/AllowanceInfo')->check_binding_weixin(C('visitor.uid'));
            if(!$check_bind){
                if(!C('qscms_weixin_apiopen')) $this->ajaxReturn(0,'未配置微信参数！');
                $this->ajaxReturn(2,'请先绑定微信');
            }
        }
        $reg = D('Allowance/AllowanceRecord')->jobs_apply_add($jid,$this->visitor->info);
        !$reg && $this->ajaxReturn(0,D('Allowance/AllowanceRecord')->getError());
        $this->ajaxReturn(1,'投递成功！',$reg);
    }
    /**
     * 领取面试红包、入职红包、在职红包后的弹框提示
     */
    public function apply_allowance_okremind(){
        $type = I('get.type','','trim');
        switch($type){
            case 'interview':
                $msg = '<div style="text-align:left;">您好，您当前领取的是 【面试红包】，系统已帮您向该企业发起面试申请，请注意查收面试通知并及时参加面试，参加面试后点击申请“已参加面试”，企业确认后红包会自动到账！</div>';
                break;
            case 'entry':
                $msg = '<div style="text-align:left;">您好，您当前领取的是【入职红包】，请在入职后及时申请“我已入职”，企业确认后红包会自动到账。请如实发起申请，若虚假申请将永久失去本平台领取红包的特权！</div>';
                break;
            default:
                $msg = '<div style="text-align:left;">您好，您当前领取的是 【面试红包】，系统已帮您向该企业发起面试申请，请注意查收面试通知并及时参加面试，参加面试后点击申请“已参加面试”，企业确认后红包会自动到账！</div>';
                break;
        }
        $this->ajaxReturn(1,'回调成功！',$msg);
    }
    /**
     * 同意面试
     */
    public function agree_interview(){
        $record_id = I('get.record_id',0,'intval');
        if(!$record_id){
            $this->ajaxReturn(0,'参数错误！');
        }
        D('Allowance/AllowanceRecord')->agree_interview($record_id);
        $this->ajaxReturn(1,'操作成功！');
    }
    /**
     * 拒绝面试
     */
    public function refuse_interview(){
        $record_id = I('get.record_id',0,'intval');
        if(!$record_id){
            $this->ajaxReturn(0,'参数错误！');
        }
        D('Allowance/AllowanceRecord')->refuse_interview($record_id);
        $this->ajaxReturn(1,'操作成功！');
    }
    /**
     * 确认个人缺席面试
     */
    public function absent_interview(){
        $record_id = I('request.record_id',0,'intval');
        if(!$record_id){
            $this->ajaxReturn(0,'参数错误！');
        }
        if(IS_POST){
            D('Allowance/AllowanceRecord')->absent_interview($record_id);
            $this->ajaxReturn(1,'操作成功！');
        }else{
            $html = '您当前判定个人缺席面试，判定完成后将结束本条流程。请如实判定，若虚假处理将被加入黑名单，永久失去本平台发布红包职位特权！';
            $this->ajaxReturn(1,'回调成功！',$html);
        }
    }
    /**
     * 确认入职
     */
    public function confirm_entry(){
        $record_id = I('get.record_id',0,'intval');
        if(!$record_id){
            $this->ajaxReturn(0,'参数错误！');
        }
        $r = D('Allowance/AllowanceRecord')->confirm_entry($record_id);
        if($r){
            $this->ajaxReturn(1,'操作成功！');
        }else{
            $this->ajaxReturn(0,D('Allowance/AllowanceRecord')->getError());
        }
    }
    /**
     * 确认个人缺席入职
     */
    public function absent_entry(){
        $record_id = I('request.record_id',0,'intval');
        if(!$record_id){
            $this->ajaxReturn(0,'参数错误！');
        }
        if(IS_POST){
            D('Allowance/AllowanceRecord')->absent_entry($record_id);
            $this->ajaxReturn(1,'操作成功！');
        }else{
            $html = '您当前判定个人缺席面试，判定完成后将结束本条流程。请如实判定，若虚假处理将被加入黑名单，永久失去本平台发布红包职位特权！';
            $this->ajaxReturn(1,'回调成功！',$html);
        }
    }
}
?>