<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class CompanyController extends MobileController{
    public function _initialize(){
        parent::_initialize();
        //访问者控制
        if(I('get.code','','trim') && !in_array(ACTION_NAME,array('order_detail'))){
            $reg = $this->get_weixin_openid(I('get.code','','trim'));
            $reg && $this->redirect('members/apilogin_binding');
        }
        if (!$this->visitor->is_login) {
            IS_AJAX && $this->ajaxReturn(0, L('login_please'),'',1);
            //非ajax的跳转页面
            $this->redirect('members/login');
        }
        if(!IS_AJAX){
            $this->_global_variable();
        }else{
            $this->_cominfo_flge();
        }
    }
    protected function _global_variable() {
        C('visitor.utype') !=1 && $this->redirect('members/login');
        // 顾问信息
        if(C('visitor.consultant')>0){
            $consultant = M('Consultant')->where(array('id'=>C('visitor.consultant')))->find();
            $this->assign('consultant',$consultant);
        }
        // 账号状态 为暂停
        if (C('visitor.status') == 2 && !in_array(ACTION_NAME, array('index'))){
            $this->_404('您的账号处于暂停状态，请联系管理员设为正常后进行操作！',U('Company/index'));
        }
        // 短信必须验证
        if (C('qscms_sms_open')==1 && C('qscms_login_com_audit_mobile')==1 && C('visitor.mobile_audit') == 0 && !in_array(ACTION_NAME, array('com_security_tel','com_auth','com_info'))){
            $this->_404('您的手机未认证，认证后才能进行其他操作！',U('Company/com_security_tel'));
        }
        $this->_cominfo_flge();
        // 强制认证营业执照
        if (C('qscms_login_com_audit_certificate')==1 && $this->company_profile['audit'] !=1 && !in_array(ACTION_NAME, array('com_auth','com_info','com_security_tel'))){
            $this->_404('您的营业执照未认证，认证后才能进行其他操作！',U('Company/com_auth'));
        }
        $this->assign('company_profile',$this->company_profile);
        $this->assign('cominfo_flge',$this->cominfo_flge);
        // 第一次登录
        if(!S('personal_login_first_'.C('visitor.uid'))){
            S('personal_login_first_'.C('visitor.uid'),1,86400-(time()-strtotime("today")));
            //快到期提醒
            $my_setmeal = D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
            if($my_setmeal['endtime']>0){
                if(C('qscms_meal_min_remind')==0){
                    $confirm_setmeal = 0;
                }else{
                    if($my_setmeal['endtime'] - time()>C('qscms_meal_min_remind')){
                        $confirm_setmeal = 0;
                    }else{
                        $confirm_setmeal = 1;
                    }
                }
                $this->assign('confirm_setmeal',$confirm_setmeal);
            }
        }
        $this->assign('company_nav',ACTION_NAME);
    }
    protected function _cominfo_flge(){
        //当前用户的企业信息 
        $this->company_profile = M('CompanyProfile')->where(array('uid'=>C('visitor.uid')))->find();
        if($this->company_profile)
        {
            // 判断是否需要完善信息
            $this->cominfo_flge=true;
            $array=array("companyname","nature","trade","district","scale","address","contact","email","contents");
            foreach ($this->company_profile as $key => $value){
                if(in_array($key,$array) && empty($value))
                {
                    $this->cominfo_flge=false;
                    break;
                }
            }
        }
        else
        {   
            $this->cominfo_flge=false;
        }
        $this->assign('cominfo_flge',$this->cominfo_flge);
    }
    /**
     * 检测当前发布职位数是否足够
     */
    public function check_jobs_num(){
        $upper_limit = 0;
        $setmeal = D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
        $jids = M('Jobs')->where(array('uid'=>C('visitor.uid')))->getField('id',true);
        $jids_tmp = M('JobsTmp')->where(array('uid'=>C('visitor.uid'),'display'=>1))->getField('id',true);
        if(count($jids)+count($jids_tmp)>=$setmeal['jobs_meanwhile']) $upper_limit = 1;
        $this->ajaxReturn($upper_limit,'',$setmeal['is_free']);
    }
    public function index(){
        $setmeal = D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
        $is_apply = M('PersonalJobsApply')->where(array('company_uid'=>C('visitor.uid'),'personal_look'=>1))->find();
        $upper_limit = 0;
        $jids = M('Jobs')->where(array('uid'=>C('visitor.uid')))->getField('id',true);
        $jids_tmp = M('JobsTmp')->where(array('uid'=>C('visitor.uid'),'display'=>1))->getField('id',true);
        if(count($jids)+count($jids_tmp)>=$setmeal['jobs_meanwhile']) $upper_limit = 1;
        $this->assign('is_apply',$is_apply ? 1 : 0);
        $this->assign('setmeal',$setmeal);
        $this->assign('upper_limit',$upper_limit);
        $this->assign('points',D('MembersPoints')->get_user_points(C('visitor.uid')));//当前用户积分数
        $this->_config_seo(array('title'=>'企业会员中心 - '.C('qscms_site_name'),'header_title'=>'企业会员中心'));
        $this->display();
    }
	public function com(){
        $this->_config_seo(array('title'=>'企业管理 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'企业管理'));
        $this->display();
    }
    public function com_info(){
        $company_profile = $this->company_profile;
        if(!IS_POST){
            //对座机进行分隔
            $telarray = explode('-',$company_profile['landline_tel']);
            if(intval($telarray[0]) > 0)
            {
                $company_profile['landline_tel_first'] = $telarray[0];
            }
            if(intval($telarray[1]) > 0)
            {
                $company_profile['landline_tel_next'] = $telarray[1];
            }
            if(intval($telarray[2]) > 0)
            {
                $company_profile['landline_tel_last'] = $telarray[2];
            }
            $comtag = $this->company_profile['tag']?explode(",",$this->company_profile['tag']):array();
            $tagArr = array('id'=>array(),'cn'=>array());
            if(!empty($comtag)){
                foreach ($comtag as $key => $value) {
                    $arr = explode("|",$value);
                    $tagArr['id'][] = $arr[0];
                    $tagArr['cn'][] = $arr[1];
                }
            }
            $tagStr = array('id'=>'','cn'=>'');
            if(!empty($tagArr['id']) && !empty($tagArr['cn'])){
                $tagStr['id'] = implode(",",$tagArr['id']);
                $tagStr['cn'] = implode(",",$tagArr['cn']);
            }
            $this->assign('tagArr',$tagArr);
            $this->assign('tagStr',$tagStr);
            /* 分类*/
            $category = D('Category')->get_category_cache();
            $tag_arr = array_chunk($category['QS_jobtag'],12,true);
            $company_profile['landline_tel'] = ltrim($company_profile['landline_tel'],'-');
            $this->assign('tag_arr',$tag_arr);
            $this->assign('category',$category);
            $this->assign('company_profile',$company_profile);
            $this->_config_seo(array('title'=>'企业资料 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'企业资料'));
            $this->display();
        }else{//保存企业信息
            $setsqlarr['id']=I('post.id',0,'intval');
            $setsqlarr['uid']=C('visitor.uid');
            $setsqlarr['companyname']=$company_profile['audit']==1?$company_profile['companyname']:I('post.companyname',0,'trim,badword');
            $setsqlarr['short_name']=$company_profile['audit']==1?$company_profile['short_name']:I('post.short_name',0,'trim,badword');
            // 判断企业名称是否重复
            if (C('qscms_company_repeat')=="0")
            {
                $info = M('CompanyProfile')->where(array('uid'=>array('neq',C('visitor.uid')),'companyname'=>$setsqlarr['companyname']))->getField('uid');
                if($info) $this->ajaxReturn(0,"{$setsqlarr['companyname']}已经存在，同公司信息不能重复注册");
            }

            $data = array('nature','trade','scale');
            foreach ($data as $val) {
                $setsqlarr[$val] = I('post.'.$val,0,'intval');
            }
            $setsqlarr['district'] = I('post.district',0,'intval');
            $city = get_city_info($setsqlarr['district']);
            $setsqlarr['district'] = $city['district'];
            $setsqlarr['district_cn'] = $city['district_cn_all'];

            // 分类缓存
            $category = D('Category')->get_category_cache();
            $setsqlarr['nature_cn']=$category['QS_company_type'][$setsqlarr['nature']];
            $setsqlarr['trade_cn']=$category['QS_trade'][$setsqlarr['trade']];
            // $setsqlarr['street_cn']=$category['QS_street'][$setsqlarr['street']];
            $setsqlarr['scale_cn']=$category['QS_scale'][$setsqlarr['scale']];
            // 字符串字段
            $setsqlarr['registered']=I('post.registered','','trim,badword');
            $setsqlarr['currency']=I('post.currency','','trim,badword'); 
            $setsqlarr['address']=I('post.address','','trim,badword'); 
            $setsqlarr['contact']=I('post.contact','','trim,badword');
            $setsqlarr['telephone'] = C('visitor.mobile_audit') ? C('visitor.mobile') : I('post.telephone','','trim,badword');
            $setsqlarr['email'] = C('visitor.email_audit') ? C('visitor.email') : I('post.email','','trim,badword');
            $setsqlarr['website']=I('post.website','','trim,badword');
            $setsqlarr['contents']=I('post.contents','','trim,badword');
            $setsqlarr['contact_show']=I('post.contact_show',1,'intval');
            $setsqlarr['telephone_show']=I('post.telephone_show',1,'intval');
            $setsqlarr['landline_tel_show']=I('post.landline_tel_show',1,'intval');
            $setsqlarr['email_show']=I('post.email_show',1,'intval');
            $setsqlarr['qq']=I('post.qq',0,'intval');
            $setsqlarr['map_x']=I('post.map_x',0,'trim,badword');
            !$setsqlarr['map_x'] && $setsqlarr['map_x'] = 0;
            $setsqlarr['map_y']=I('post.map_y',0,'trim,badword');
            !$setsqlarr['map_y'] && $setsqlarr['map_y'] = 0;
            $setsqlarr['map_zoom']=I('post.map_zoom',0,'intval');
            
            //座机
            $landline_tel_first=I('post.landline_tel_first',0,'trim,badword');
            $landline_tel_next=I('post.landline_tel_next',0,'trim,badword');
            $landline_tel_last=I('post.landline_tel_last',0,'trim,badword');
            $setsqlarr['landline_tel']=$landline_tel_first.'-'.$landline_tel_next.($landline_tel_last?('-'.$landline_tel_last):'');
            $posttag = I('post.tag','','trim,badword');

            if($posttag){
                $tagArr = explode(",",$posttag);
                $r_arr = array();
                foreach ($tagArr as $key => $value) {
                    $r_arr[] = $value.'|'.$category['QS_jobtag'][$value];
                }
                if(!empty($r_arr)){
                    $setsqlarr['tag'] = implode(",",$r_arr);
                }else{
                    $setsqlarr['tag'] = '';
                }
            }

            if($company_profile['contents']) 
            {
                $setsqlarr['id']=$company_profile['id'];
                C('qscms_audit_edit_com')<>"-1"?$setsqlarr['audit']=C('qscms_audit_edit_com'):$setsqlarr['audit']=$company_profile['audit'];
            }
            else
            {
                $setsqlarr['audit']=0;
            }
            $setsqlarr['sync'] = I('post.sync',0,'intval');
            // 插入数据
            $rst = D('CompanyProfile')->add_company_profile($setsqlarr,C('visitor'));
            $rst['state']==0 && $this->ajaxReturn(0,$rst['error']);
            $r = D('TaskLog')->do_task(C('visitor'),27);
            if($setsqlarr['map_x'] && $setsqlarr['map_y'] && $setsqlarr['map_zoom']){
                D('TaskLog')->do_task(C('visitor'),29);
            }
            if($setsqlarr['audit']!=$company_profile['audit']){
                switch($setsqlarr['audit']){
                    case 0:
                        $audit_str = '未认证';break;
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
                    $auditsqlarr['company_id']=$company_profile['id'];
                    $auditsqlarr['reason']='自动设置';
                    $auditsqlarr['status']=$audit_str;
                    $auditsqlarr['addtime']=time();
                    $auditsqlarr['audit_man']='系统';
                    M('AuditReason')->data($auditsqlarr)->add();
                }
            }
            
            if($rst['id']) {$success ="添加成功！";}else{$success ="保存成功！";}
            $this->ajaxReturn(1,$success,$r['points']);
        }
    }
    public function com_img(){
        $company_img = M('CompanyImg')->where(array('uid'=>C('visitor.uid')))->select();
        $this->assign('company_img',$company_img);
        $this->_config_seo(array('title'=>'企业风采 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'企业风采'));
        $this->display();
    }
    // 删除企业风采
    public function del_company_img()
    {
        $id = I('request.id',0,'intval');
        !$id && $this->ajaxReturn(0, '图片Id不能为空！');
        $img =  M('CompanyImg')->find($id);
        !$img && $this->ajaxReturn(0, '图片Id不存在！');
        @unlink(C('qscms_attach_path').'company_img/'.$img['img']);
        if(C('qscms_qiniu_open')==1){
            $qiniu = new \Common\ORG\qiniu;
            $qiniu->delete($img['img']);
        }
        $num = M('CompanyImg')->where(array('uid'=>C('visitor.uid'),'id'=>$id))->delete();
        $num===false && $this->ajaxReturn(0, '删除失败！');
        //写入会员日志
        write_members_log(C('visitor'),'','删除企业风采（id：'.$id.'）');
        $this->ajaxReturn(1, '删除成功！');
    }
    /**
     * 企业认证
     */
    public function com_auth(){
        !$this->cominfo_flge && $this->_404('为了达到更好的招聘效果，请先完善您的企业资料！',U('company/com_info'));
        if(IS_POST && $this->company_profile['audit']!=1){
            $data['certificate_img'] = I('post.certificate_img_up','','trim,badword');
            $data['audit'] = 2;
            $rst = D('CompanyProfile')->add_certificate_img($data,C('visitor'));
            if(IS_AJAX){
                !$rst['state'] && $this->ajaxReturn(0,$rst['error']);
                $this->ajaxReturn(1,'更新营业执照成功！',$data);
            } else {
                !$rst['state'] && $this->_404($rst['error']);
                $this->assign('company_profile',array_merge($this->company_profile,$data));
            }
        }
        // 认证不通过原因提示
        if($this->company_profile['audit'] == 3){
            $map['company_id'] = $this->company_profile['id'];
            $map['status'] = '认证未通过';
            $reason = M('AuditReason')->where($map)->order('id DESC')->getField('reason');
            $this->assign('reason',$reason);
        }
        $this->_config_seo(array('title'=>'企业认证 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'企业认证'));
        $this->display();
    }
    public function com_binding(){
        $uid=C('visitor.uid');
        $user_bind = M('MembersBind')->where(array('uid'=>$uid))->limit('10')->getfield('type,keyid,info');
        foreach($user_bind as $key=>$val){
            $user_bind[$key] = unserialize($val['info']);
        }
        if(false === $oauth_list = F('oauth_list')){
            $oauth_list = D('Oauth')->oauth_cache();
        }
        $this->assign('user_bind',$user_bind);
        $this->assign('oauth_list',$oauth_list);
        $this->_config_seo(array('title'=>'账号绑定 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'账号绑定'));
        $this->display();
    }
    public function com_msg(){
        $this->_config_seo(array('title'=>'消息提醒 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'消息提醒'));
        $this->display();
    }
    public function com_info_introduction(){
        $this->display();
    }
    public function com_info_tel(){
        $this->display();
    }
    public function com_info_adder(){
        $this->display();
    }
    public function com_security(){
        $this->_config_seo(array('title'=>'账号安全 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'账号安全'));
        $this->display();
    }
    public function com_security_tel(){
        $this->_config_seo(array('title'=>'手机认证 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'手机认证'));
        $this->display();
    }
    public function com_security_email(){
        $this->_config_seo(array('title'=>'邮箱认证 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'邮箱认证'));
        $this->display();
    }
    public function com_security_user(){
        $this->_config_seo(array('title'=>'修改用户名 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'修改用户名'));
        $this->display();
    }
    public function com_security_pwd(){
        $this->_config_seo(array('title'=>'修改密码 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'修改密码'));
        $this->display();
    }
    public function com_security_log(){
        $where = array('log_uid'=>C('visitor.uid'),'log_type'=>1001);
        $loginlog = D('MembersLog')->get_members_log($where,15);
        if($loginlog['list']){
            foreach ($loginlog['list'] as $key => $val) {
                $t = strtotime(date('Y-m-d',$val['log_addtime']));
                $list[$t][] = $val;
            }
            $loginlog['list'] = $list;
        }
        $this->assign('loginlog',$loginlog);
        $this->_config_seo(array('title'=>'会员登录日志 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'会员登录日志'));
        $this->display();
    }
    public function com_msg_consult(){
        $this->display();
    }
    public function com_msg_sys(){
        $this->display();
    }
    public function jobs_add(){
        if(!$this->cominfo_flge){
            if(IS_AJAX){
                $this->ajaxReturn(0,'为了达到更好的招聘效果，请先完善您的企业资料！');
            }else{
                $this->_404('为了达到更好的招聘效果，请先完善您的企业资料！',U('company/com_info'));
            }
        }
        //对座机进行分隔
        $telarray = explode('-',$this->company_profile['landline_tel']);
        $setmeal=D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
        // 统计有效职位数
        $jobs_num = D('Jobs')->where(array('uid'=>C('visitor.uid')))->count();
        $jobs_num_tmp = D('JobsTmp')->where(array('uid'=>C('visitor.uid'),'display'=>1))->count();
        if($jobs_num+$jobs_num_tmp>=$setmeal['jobs_meanwhile'] && I('request.id',0,'intval')==0){
            if(IS_AJAX){
                $this->ajaxReturn(0,'当前显示的职位已经达到最大限制，请升级服务套餐!');
            }else{
                $this->_404('当前显示的职位已经达到最大限制，请升级服务套餐!',U('CompanyService/index'));
            }
        }else{
            IS_AJAX && IS_GET && $this->ajaxReturn(1);
        }
        if(IS_POST && IS_AJAX){
            // 保存 POST 数据
            // 插入职位信息
            $jobs_info = '';
            if($id = I('post.id',0,'intval')){
                $setsqlarr['id'] = $id;
                $jobs_info = D('Jobs')->find($id);
                if(!$jobs_info){
                    $jobs_info = D('JobsTmp')->find($id);
                }
                $name = 'edit_jobs';
            }else{
                $name = 'add_jobs';
            }
            $setsqlarr['setmeal_deadline']=$setmeal['endtime'];
            $setsqlarr['deadline']=$setsqlarr['setmeal_deadline'];
            $setsqlarr['setmeal_id']=$setmeal['setmeal_id'];
            $setsqlarr['setmeal_name']=$setmeal['setmeal_name'];
            $setsqlarr['uid']=C('visitor.uid');
            $setsqlarr['company_id']=$this->company_profile['id'];
            $setsqlarr['company_addtime']=$this->company_profile['addtime'];
            $setsqlarr['company_audit']=$this->company_profile['audit'];
            C('apply.Sincerity') && $setsqlarr['famous']=$this->company_profile['famous'];
            if(!$id){
                $setsqlarr['audit']= 2;
            }else{
                if($this->company_profile['audit']==1){
                    $setsqlarr['audit'] = C('qscms_audit_verifycom_editjob')=='-1'?$jobs_info['audit']:C('qscms_audit_verifycom_editjob');
                }else{
                    $setsqlarr['audit'] = C('qscms_audit_unexaminedcom_editjob')=='-1'?$jobs_info['audit']:C('qscms_audit_unexaminedcom_editjob');
                }
            }
            
            $array = array('companyname','trade','trade_cn','scale','scale_cn','tpl','map_x','map_y','map_zoom');
            if(I('post.basis_contact',0,'intval')){//与企业联系方式同步
                $array = array_merge($array,array('contact','telephone','landline_tel','address','email','contact_show','email_show','telephone_show','landline_tel_show'));
            }else{
                $setsqlarr['contact']=I('post.contact','','trim,badword');
                $setsqlarr['telephone']=I('post.telephone','','trim,badword');
                $setsqlarr['landline_tel']=I('post.landline_tel','','trim,badword');
                $setsqlarr['address']=I('post.address','','trim,badword');
                $setsqlarr['email']=I('post.email','','trim,badword');
                $setsqlarr['contact_show']=I('post.contact_show',1,'intval');
                $setsqlarr['email_show']=I('post.email_show',1,'intval');
                $setsqlarr['telephone_show']=I('post.telephone_show',1,'intval');
                $setsqlarr['landline_tel_show']=I('post.landline_tel_show',1,'intval');
            }
            foreach($array as $val){
                $setsqlarr[$val] = $this->company_profile[$val];
            }
            $array = array('nature','topclass','category','subclass','amount','district','minwage','maxwage','negotiable','sex','education','experience','graduate','minage','maxage','notify','notify_mobile');
            foreach ($array as $val) {
                $setsqlarr[$val] = I('post.'.$val,0,'intval');
            }
            $jobcategory = I('post.jobcategory');
            $jobcategory_arr = explode(".", $jobcategory);
            $setsqlarr['topclass']= $jobcategory_arr[0];
            $setsqlarr['category']= $jobcategory_arr[1];
            $setsqlarr['subclass']= $jobcategory_arr[2];
            $setsqlarr['jobs_name']= I('post.jobs_name','','trim,badword');
            $setsqlarr['tag']=I('post.tag','','trim,badword');// 标签
            $setsqlarr['contents']=I('post.contents','','trim,badword');
            $setsqlarr['department'] = I('post.department','','trim,badword');
            
            $rst = D('Jobs')->$name($setsqlarr,C('visitor'));
            if($rst['state']==0){
                if(IS_AJAX){
                    $this->ajaxReturn(0,$rst['error']);
                }else{
                    $this->_404($rst['error']);
                }
            }
            if($jobs_info){
                if($setsqlarr['audit']!=$jobs_info['audit']){
                    switch($setsqlarr['audit']){
                        case 1:
                            $audit_str = '审核通过';break;
                        case 2:
                            $audit_str = '审核中';break;
                        case 3:
                            $audit_str = '审核未通过';break;
                        default:
                            $audit_str = '';break;
                    }
                    if($audit_str){
                        $auditsqlarr['jobs_id']=$jobs_info['id'];
                        $auditsqlarr['reason']='自动设置';
                        $auditsqlarr['status']=$audit_str;
                        $auditsqlarr['addtime']=time();
                        $auditsqlarr['audit_man']='系统';
                        M('AuditReason')->data($auditsqlarr)->add();
                    }
                }
            }else{
                switch($setsqlarr['audit']){
                    case 1:
                        $audit_str = '审核通过';break;
                    case 2:
                        $audit_str = '';break;
                    case 3:
                        $audit_str = '审核未通过';break;
                    default:
                        $audit_str = '';break;
                }
                if($audit_str){
                    $auditsqlarr['jobs_id']=$rst['id'];
                    $auditsqlarr['reason']='自动设置';
                    $auditsqlarr['status']=$audit_str;
                    $auditsqlarr['addtime']=time();
                    $auditsqlarr['audit_man']='系统';
                    M('AuditReason')->data($auditsqlarr)->add();
                }
            }
            if(C('qscms_jobs_display') == 2){
            baidu_submiturl(url_rewrite('QS_jobsshow',array('id'=>$rst['id'])),'addjob');
            }
            if(IS_AJAX){
                $this->ajaxReturn(1,$id?'修改成功！':'添加成功！',array('url'=>U('company/jobs_list'),'insertid'=>$rst['id']));
            }else{
                $this->redirect('company/jobs_list');
            }
        }else{
            $comtag = $this->company_profile['tag']?explode(",",$this->company_profile['tag']):array();
            $tagArr = array('id'=>array(),'cn'=>array());
            if(!empty($comtag)){
                foreach ($comtag as $key => $value) {
                    $arr = explode("|",$value);
                    $tagArr['id'][] = $arr[0];
                    $tagArr['cn'][] = $arr[1];
                }
            }
            $tagStr = array('id'=>'','cn'=>'');
            if(!empty($tagArr['id']) && !empty($tagArr['cn'])){
                $tagStr['id'] = implode(",",$tagArr['id']);
                $tagStr['cn'] = implode(",",$tagArr['cn']);
            }
            $this->assign('tagStr',$tagStr);
            $this->assign('jobs',$jobs);
            /* 分类*/
            $category = D('Category')->get_category_cache();
            $this->assign('category',$category);
            $this->assign('telarray',$telarray);
            $total=$setmeal['jobs_meanwhile'] - $jobs_num - $jobs_num_tmp;
            $this->assign('total',$total);
            $this->assign('setmeal',$setmeal);
            $tag_arr = array_chunk($category['QS_jobtag'],12,true);
            $this->assign('tag_arr',$tag_arr);
            //检测是否被屏蔽领取红包功能
            if(C('apply.Allowance')){
                $could_allowance = 1;
                $in_blacklist = D('Allowance/AllowanceBlacklist')->where(array('uid'=>C('visitor.uid')))->find();
                if($in_blacklist){
                    if($in_blacklist['deadline']==0 || $in_blacklist['deadline']>time()){
                        $could_allowance = 0;
                    }else{
                        D('Allowance/AllowanceBlacklist')->where(array('uid'=>$user['uid']))->delete();
                    }
                }
            }else{
                $could_allowance = 0;
            }
            $this->assign('could_allowance',$could_allowance);
            $this->_config_seo(array('title'=>'发布职位 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'发布职位'));
            $this->display();
        }
    }
    /**
     * 修改职位
     */
    public function jobs_edit(){
        $id = I('get.id',0,'intval');
        $jobs = D('Jobs')->get_jobs_one(array('id'=>$id,'uid'=>C('visitor.uid')));
        $jobs['age'] = explode('-',$jobs['age']);
        $telarray = explode('-',$jobs['contact']['landline_tel']);
        $category = D('Category')->get_category_cache();
        $this->assign('category',$category);
        $this->assign('jobs',$jobs);
        $this->assign('company_profile',$jobs['contact']);
        $this->assign('telarray',$telarray);
        $tag_arr = array_chunk($category['QS_jobtag'],12,true);
        $this->assign('tag_arr',$tag_arr);
        $this->assign('tag_val',explode(',',$jobs['tag']));
        // 统计有效职位数
        $setmeal=D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
        $jobs_num = D('Jobs')->where(array('uid'=>C('visitor.uid')))->count();
        $jobs_num_tmp = D('JobsTmp')->where(array('uid'=>C('visitor.uid'),'display'=>1))->count();
        $total=$setmeal['jobs_meanwhile']-$jobs_num-$jobs_num_tmp;
        $total = $total<0?0:$total;
        $this->assign('total',$total);
        //检测是否被屏蔽领取红包功能
        if(C('apply.Allowance')){
            $could_allowance = 1;
            $in_blacklist = D('Allowance/AllowanceBlacklist')->where(array('uid'=>C('visitor.uid')))->find();
            if($in_blacklist){
                if($in_blacklist['deadline']==0 || $in_blacklist['deadline']>time()){
                    $could_allowance = 0;
                }else{
                    D('Allowance/AllowanceBlacklist')->where(array('uid'=>$user['uid']))->delete();
                }
            }
        }else{
            $could_allowance = 0;
        }
        $this->assign('could_allowance',$could_allowance);
        $this->_config_seo(array('title'=>'修改职位 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'修改职位'));
        $this->display('jobs_add');
    }
    /**
     * 职位管理
     */
    public function jobs_list(){
        $this->check_params();
        $type = I('get.type',0,'intval');
        switch ($type) {
            case '1':
                $tabletype="jobs";
                break;
            case '2':
                $tabletype = "jobs_tmp";
                break;
            default:
                $tabletype = "all";
                break;
        }
        $jobs_list = D('Jobs')->get_jobs(array('uid'=>C('visitor.uid')),'display asc,audit asc,refreshtime desc',$tabletype,10,true,true,true,true);

        if(!APP_SPELL){
            $city_cate = D('CategoryDistrict')->city_cate_cache();
            foreach ($jobs_list as $key => $val) {
                $jobs_list[$key]['jobcategory'] = $city_cate[$val['subclass'] || $val['category'] || $val['topclass']];
            }
        }
        $upper_limit = 0;
        $my_setmeal = D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
        $jids = M('Jobs')->where(array('uid'=>C('visitor.uid')))->getField('id',true);
        $jids_tmp = M('JobsTmp')->where(array('uid'=>C('visitor.uid'),'display'=>1))->getField('id',true);
        if(count($jids)+count($jids_tmp)>=$my_setmeal['jobs_meanwhile']) $upper_limit = 1;
        $this->assign('jobs_list',$jobs_list);
        $this->assign('total',$total);
        $this->assign('my_setmeal',$my_setmeal);
        $this->assign('promotion',$promotion);
        $this->assign('upper_limit',$upper_limit);
        $this->_config_seo(array('title'=>'管理职位 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'管理职位'));
        $this->display();
    }
    public function jobs_display(){
        $yid =  I('request.id');
        if(!$yid){
            $this->_404('请选择职位！');
        }
        $upper_limit = 0;
        $my_setmeal = D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
        $jids = M('Jobs')->where(array('uid'=>C('visitor.uid')))->getField('id',true);
        $jids_tmp = M('JobsTmp')->where(array('uid'=>C('visitor.uid'),'display'=>1))->getField('id',true);
        if(count($jids)+count($jids_tmp)>=$my_setmeal['jobs_meanwhile']) $upper_limit = 1;
        if($upper_limit){
            $this->ajaxReturn(2);
        }
        $perform_type = 'display';
        $rst = D('Jobs')->jobs_perform(array('yid'=>$yid,'perform_type'=>$perform_type,'user'=>C('visitor')));
        $return_url = I('request.list_type',0,'intval')==0?U('jobs_list'):U('jobs_list',array('type'=>1));
        if($rst['state']==1)
        {
            $this->ajaxReturn(1,'操作成功！',$return_url);
        }
        else
        {
            $this->ajaxReturn(0,$rst['error'],$return_url);
        }
    }
    public function jobs_close(){
        $yid =  I('request.id');
        if(!$yid){
            $this->_404('请选择职位！');
        }
        $perform_type = 'close';
        $rst = D('Jobs')->jobs_perform(array('yid'=>$yid,'perform_type'=>$perform_type,'user'=>C('visitor')));
        $return_url = I('request.list_type',0,'intval')==0?U('jobs_list'):U('jobs_list',array('type'=>1));
        if($rst['state']==1)
        {
            $this->ajaxReturn(1,'操作成功！',$return_url);
        }
        else
        {
            $this->ajaxReturn(0,$rst['error'],$return_url);
        }
    }
    public function jobs_delete(){
        $yid =  I('request.id');
        if(!$yid){
            $this->_404('请选择职位！');
        }
        $perform_type = 'delete';
        $rst = D('Jobs')->jobs_perform(array('yid'=>$yid,'perform_type'=>$perform_type,'user'=>C('visitor')));
        $return_url = I('request.list_type',0,'intval')==0?U('jobs_list'):U('jobs_list',array('type'=>1));
        if($rst['state']==1){
            $this->ajaxReturn(1,'操作成功！',$return_url);
        }else{
            $this->ajaxReturn(0,$rst['error'],$return_url);
        }
    }
    /**
     * 职位刷新
     */
    public function jobs_refresh(){
        $yid = I('request.yid');
        if(!$yid){
            $this->ajaxReturn(0,'请选择职位！');
        }
        /*$yid = is_array($yid)?$yid:explode(",", $yid);
        $jobs_num =count($yid);
        $user_jobs=D('Jobs')->count_auditjobs_num(C('visitor.uid'));
        if($user_jobs==0){
            $this->ajaxReturn(0,'没有可刷新的职位！');
        }*/
        $r = D('Jobs')->jobs_refresh(array('yid'=>$yid,'user'=>C('visitor')));
        if($r['state']==1){
            $this->ajaxReturn(1,'刷新成功！');
        }else{
            $this->ajaxReturn(0,$r['error']);
        }
    }
    /**
     * 职位刷新确认
     */
    public function jobs_refresh_confirm(){
        $yid = I('request.yid');
        if(!$yid){
            $this->ajaxReturn(0,'请选择职位！');
        }
        $job = D('Jobs')->get_jobs_one(array('id'=>$yid,'uid'=>C('visitor.uid')));
        if($job['audit'] == 3){
            $this->ajaxReturn(0,'审核未通过职位不可刷新！');
        } elseif ($job['audit'] == 2 && C('qscms_jobs_display') == 1){
            $this->ajaxReturn(0,'审核中职位不可刷新！');
        } elseif ($job['display'] == 2 || ($job['deadline']>0 && $job['deadline']<time())){
            $this->ajaxReturn(0,'已关闭职位不可刷新！');
        }
        $my_setmeal = D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
        $my_points = D('MembersPoints')->get_user_points(C('visitor.uid'));
        $tip = '';
        $discount = D('Setmeal')->get_max_discount('auto_refresh_jobs');
        $refresh_time = D('RefreshLog')->get_today_refresh_times(array('uid'=>C('visitor.uid'),'type'=>1001,'mode'=>2));
        if($refresh_time>=($my_setmeal['refresh_jobs_free']+C('qscms_refresh_jobs_more'))){//免费刷新次数已到
            if($my_points>=C('qscms_refresh_jobs_price')*C('qscms_payment_rate') && C('qscms_refresh_jobs_by_points')==1){
                $tip = '<div class="dialog_notice">今天免费刷新次数已用完，本次刷新需要扣除 <span class="font_yellow">'.intval(C('qscms_payment_rate')*C('qscms_refresh_jobs_price')).'</span> '.C('qscms_points_byname').'，是否确定刷新？<br /><div class="dialog_tip font10 font_gray9">您当前拥有 <span class="font_yellow">'.$my_points.'</span> '.C('qscms_points_byname').'</div></div>';
                $mode = 'points';
            }else{
                $this->assign('discount',$discount);
                $this->assign('jobs_id',$yid);
                $tip = $this->fetch('Ajax_tpl/guide_pay_refresh_jobs');
                $mode = 'mix';
            }
        }else{
            $tip = '<div class="dialog_notice">你今天还可免费刷新 <span>'.($my_setmeal['refresh_jobs_free']+C('qscms_refresh_jobs_more')-$refresh_time).'</span> 次，本次刷新免费，是否立即刷新？</div>';
            $mode = 'setmeal';
        }
        $this->ajaxReturn(1,$tip,$mode);
    }
    /**
     * [jobs_refresh_all 批量刷新]
     */
    public function jobs_refresh_all(){
        $user_jobs=D('Jobs')->count_auditjobs_num(C('visitor.uid'));
        if($user_jobs==0){
            $this->ajaxReturn(0,'没有可刷新的职位！');
        }
        $my_setmeal = D('MembersSetmeal')->get_user_setmeal(C('visitor.uid'));
        $refresh_time = D('RefreshLog')->get_today_refresh_times(array('uid'=>C('visitor.uid'),'type'=>1001,'mode'=>2));
        if($refresh_time>=($my_setmeal['refresh_jobs_free']+C('qscms_refresh_jobs_more'))){
            $this->ajaxReturn(2,'您当前共有 <span class="font_yellow">'.$user_jobs.'</span> 条在招职位，今天免费刷新次数已用完。请单条刷新。',1);
        }elseif($user_jobs+$refresh_time>($my_setmeal['refresh_jobs_free']+C('qscms_refresh_jobs_more'))){
            $surplus = $my_setmeal['refresh_jobs_free'] + C('qscms_refresh_jobs_more') - $refresh_time;
            $this->ajaxReturn(2,'您当前共有 <span class="font_yellow">'.$user_jobs.'</span> 条在招职位，今天免费刷新次数剩余 <span class="font_yellow">'.$surplus.'</span> 次。请单条刷新。',1);
        }else{
            $condition['uid'] = C('visitor.uid');
            C('qscms_jobs_display') == 1 && $condition['audit'] = 1 ;
            $jobsid_arr = D('Jobs')->where($condition)->field('id')->select();
            $yid = array();
            foreach ($jobsid_arr as $key => $value) {
                $yid[] = $value['id'];
            }
            $r = D('Jobs')->jobs_refresh(array('yid'=>$yid,'user'=>C('visitor')));
            $this->ajaxReturn($r['state'],$r['error']);
        }
    }
    /**
     * 收到的简历
     */
    public function jobs_apply(){
        $this->check_params();
        $where['company_uid']=C('visitor.uid');
        $stop =I('get.stop',0,'intval');
        $settr =I('get.settr',0,'intval');
        $settr && $where['apply_addtime']=array('gt',strtotime("-{$settr} day")); //筛选简历
        $jobs_id =I('get.jobs_id',0,'intval');
        $jobs_id && $where['jobs_id']=$jobs_id; //筛选简历
        // 筛选项 -> 标签 (0->全部  1->合适  2->不合适  3->待定  4->未接通)
        $state=I('get.state',0,'intval');
        // 筛选项 -> 来源 (0->全部  1->委托投递  2->主动投递 )
        $is_apply=I('get.is_apply',0,'intval');
        $is_apply && $where['is_apply']=$is_apply; 
        $personal_apply_mod = D('PersonalJobsApply');
        $condition['uid'] = C('visitor.uid');
        (!$stop && C('qscms_jobs_display') == 1) && $condition['audit'] = 1;
        $jobs_list = M('Jobs')->where($condition)->getField('id,jobs_name');
        if($stop){
            $jobs_list_tmp = M('JobsTmp')->where(array('uid'=>C('visitor.uid')))->getField('id,jobs_name');
            if($jobs_list_tmp){
                $jobs_list = $jobs_list?($jobs_list+$jobs_list_tmp):$jobs_list_tmp;
            }
        }
        $jobsid_arr = array();
        foreach ($jobs_list as $key => $value) {
            $jobsid_arr[] = $key;
        }
        if(!empty($jobsid_arr) && !$where['jobs_id']){
            $where['jobs_id'] = array('in',$jobsid_arr);
        }
        $apply_list = $personal_apply_mod->get_apply_jobs($where,1,$state);
        $this->assign('jobs_id',$jobs_id);
        $this->assign('jobs_list',$jobs_list);
        $this->assign('apply_list',$apply_list);
        $this->assign('is_reply',$is_reply);
        $this->assign('state',$state);
        $this->assign('state_arr',$personal_apply_mod->state_arr);
        $this->_config_seo(array('title'=>'收到的简历 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'收到的简历'));
        $this->display();
    }
    // 删除
    public function jobs_apply_del(){
        $yid= I('request.y_id');
        !$yid && $this->ajaxReturn(0,"你没有选择项目！");
        $reg=D('PersonalJobsApply')->del_jobs_apply($yid,C('visitor'));
        if($reg['state']==1){
            $this->ajaxReturn(1,"删除成功！");
        }else{
            $this->ajaxReturn(0,"删除失败！");
        }
    }
    /**
     * 面试邀请
     */
    public function jobs_interview(){
        $this->check_params();
        $where['company_uid']= C('visitor.uid');
        $stop =I('get.stop',0,'intval');
        $settr =I('get.settr',0,'intval');
        $settr && $where['interview_addtime']=array('gt',strtotime("-{$settr} day")); //筛选简历
        $look=I('get.look',0,'intval');
        $look>0 && $where['personal_look'] =$look;
        $jobs_id = I('get.jobs_id',0,'intval');
        $condition['uid'] = C('visitor.uid');
        (!$stop && C('qscms_jobs_display') == 1) && $condition['audit'] = 1;
        $jobs_list1 = M('Jobs')->where($condition)->getField('id,jobs_name');
        $jobs_list1 = $jobs_list1?$jobs_list1:array();
        if($stop==1){
            $jobs_list2 = M('JobsTmp')->where(array('uid'=>C('visitor.uid')))->getField('id,jobs_name');
            $jobs_list2 = $jobs_list2?$jobs_list2:array();
            $jobs_list = $jobs_list1 + $jobs_list2;
        }else{
            $jobs_list = $jobs_list1;
        }
        $jobs_id_arr = array();
        foreach ($jobs_list as $key => $value) {
            $jobs_id_arr[] = $key;
        }
        if($jobs_id>0){
            $where['jobs_id'] =$jobs_id;
        }
        else if(!empty($jobs_id_arr))
        {
            $where['jobs_id'] = array('in',$jobs_id_arr);
        }else{
            $where['jobs_id'] = 0;
        }
        $company_interview_mod = D('CompanyInterview');
        $interview = $company_interview_mod->get_invitation_pre($where,1);
        
        $this->assign('jobs_list',$jobs_list);
        $this->assign('interview',$interview);
        $this->assign('jobs_id',$jobs_id);
        $this->_config_seo(array('title'=>'面试邀请 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'面试邀请'));
        $this->display();
    }
    // 删除面试邀请
    public function jobs_interview_del(){
        $yid= I('request.y_id','','trim');
        !$yid && $this->ajaxReturn(0,"你没有选择项目！");
        $rst = D('CompanyInterview')->del_interview($yid,C('visitor'));
        if($rst['state']){
            $this->ajaxReturn(1,"删除成功！");
        }else{
            $this->ajaxReturn(0,"删除失败！");
        }
    }
    /**
     * 已下载简历
     */
    public function resume_down(){
        $this->check_params();
        $where['company_uid']=C('visitor.uid');
        $settr=I('get.settr',0,'intval');
        $settr && $where['down_addtime']=array('gt',strtotime("-".$settr." day")); //筛选 下载时间
        $state = I('get.state',''); // 简历标记状态
        $down_list = D('CompanyDownResume')->get_down_resume($where,$state);
        $this->assign('down_list',$down_list);
        $this->assign('state',$state);
        $this->assign('state_arr',D('CompanyDownResume')->state_arr);
        $this->_config_seo(array('title'=>'已下载的简历 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'已下载的简历'));
        $this->display();
    }
    /**
     * 收藏的简历
     */
    public function resume_favorites(){
        $this->check_params();
        $where['company_uid']=C('visitor.uid');
        $settr=I('get.settr',0,'intval');
        $education=I('get.education',0,'intval');
        $experience=I('get.experience',0,'intval');
        $education && $where['r.education'] = $education;
        $experience && $where['r.experience'] = $experience;
        $settr && $where['favorites_addtime']=array('gt',strtotime("-".$settr." day")); //筛选 收藏时间
        $favorites = D('CompanyFavorites')->get_favorites($where);
        $category = D('Category')->get_category_cache();
        $this->assign('education',$category['QS_education']);
        $this->assign('experience',$category['QS_experience']);
        $this->assign('favorites',$favorites);
        $this->_config_seo(array('title'=>'已收藏的简历 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'已收藏的简历'));
        $this->display();
    }
    /**
     * [resume_favorites_del 删除收藏的简历]
     */
    public function resume_favorites_del(){
        $yid= I('request.y_id');
        !$yid && $this->ajaxReturn(0,"你没有选择项目！");
        $n=D('CompanyFavorites')->del_favorites($yid,C('visitor'));
        if($n['state']==1){
            $this->ajaxReturn(1,"删除成功！");
        }else{
            $this->ajaxReturn(0,"删除失败！");
        }
    }
	/**
     * 意见反馈
     */
	public function feedback(){
        if(IS_POST){
            $data = I('post.');
            $r = D('Feedback')->addeedback($data);
            $this->ajaxReturn($r['state'],$r['msg']);
        }
        if (C('qscms_wap_captcha_config.varify_suggest')==1 && C('qscms_mobile_captcha_open')==1){
            $varify_suggest = 1;
        }else{
            $varify_suggest = 0;
        }
        $this->assign('varify_suggest',$varify_suggest);
        $this->_config_seo(array('title'=>'意见反馈 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'意见反馈'));
        $this->display();
	}
    /**
     * 消息提醒
     */
    public function pms_list(){
        if(I('get.type',0,'intval')){
            $msg_list = D('Msg')->msg_list(C('visitor'),false);
            $this->assign('msg_list',$msg_list);
            $this->_config_seo(array('title'=>'求职者咨询 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'求职者咨询'));
        }else{
            $settr = I('get.settr',0,'intval');
            $new = I('get.new',0,'intval');
            $map = array();
            if($settr>0){
                $tmp_addtime = strtotime('-'.$settr.' day');
                $map['dateline'] = array('egt',$tmp_addtime);
            }
            if($new>0){
                $map['new'] = array('eq',$new);
            }
            $msg = D('Pms')->update_pms_read(C('visitor'),10,$map);
            $this->assign('msg_list',$msg);
            $this->_config_seo(array('title'=>'消息提醒 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'消息提醒'));
        }
        $this->display();
    }
	 /**
     * 消息详细
     */
    public function pms_show(){
        $ids = I('request.id','','trim');
        $reg = D('Pms')->msg_check($ids,C('visitor'));
        if($reg['state']){
            $this->assign('msg',$reg['data']);
        }else{
            $this->_404($reg['error']);
        }
        $this->_config_seo(array('title'=>'系统信息 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'系统信息'));
        $this->display();
    }
	 /**
     * 资讯详细
     */
    public function pms_consult_show(){
        $id = I('get.id',0,'intval');
        !$id && $this->_404('请选择求职咨询');
        $msg_list = D('Msg')->smsg_list($id,C('visitor'));
        $this->assign('msg_list',$msg_list);
        $this->_config_seo(array('title'=>'求职者咨询 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'求职者咨询'));
        $this->display();
    }
    /**
     * [msg_feedback_send 发送咨询反馈]
     */
    public function pms_feedback_send(){
        if(IS_AJAX){
            $data['pid'] = I('post.pid',0,'intval');
            $data['touid'] = I('post.touid',0,'intval');
            $data['message'] = I('post.message','','trim');
            $reg = D('Msg')->msg_send($data,C('visitor'));
            if($reg['state']) $this->ajaxReturn(1,'消息发送成功！',$reg['data']);
            $this->ajaxReturn(0,$reg['error']);
        }
    }
    /**
     * [jobfair 招聘会]
     */
    public function jobfair_list(){
        if(!isset($this->apply['Jobfair'])) $this->_empty();
        $where = array('显示数目' => 5,'分页显示' => 1,'列表页'=>'jobfair_list');
        $jobfair_mod = new \Common\qscmstag\jobfair_listTag($where);
        $jobfair = $jobfair_mod->run();
        $this->assign('jobfair_list',$jobfair);
        $this->_config_seo(array('title'=>'近期招聘会 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'近期招聘会'));
        $this->display();
    }
    /**
     * [my_jobfair 我预定的展会记录]
     */
    public function jobfair_enact(){
        if(!isset($this->apply['Jobfair'])) $this->_empty();
        $jobfair = D('Jobfair/JobfairExhibitors')->get_jobfair_exhibitors(C('visitor'));
        $this->assign('jobfair',$jobfair);
        $this->_config_seo(array('title'=>'我预定的展位 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'我预定的展位'));
        $this->display();
    }
    /**
     * 设置红包职位
     */
    public function set_allowance_job(){
        $jobsid = I('get.jobsid',0,'intval');
        if(!$jobsid){
            $this->ajaxReturn(0,'参数错误！');
        }
        //检测是否被屏蔽领取红包功能
        $in_blacklist = D('Allowance/AllowanceBlacklist')->where(array('uid'=>C('visitor.uid')))->find();
        if($in_blacklist){
            if($in_blacklist['deadline']==0 || $in_blacklist['deadline']>time()){
                exit('你的发布红包职位权限被冻结，暂时无法发布红包职位，如有疑问请联系网站管理员');
            }else{
                D('Allowance/AllowanceBlacklist')->where(array('uid'=>$user['uid']))->delete();
            }
        }
        $category = D('Category')->get_category_cache();
        $this->assign('category',$category);
        if(false === $config = F('allowance_config')){
            $config = D('Allowance/AllowanceConfig')->config_cache();
        }
        $this->assign('range',$config['amount_range']);
        $this->assign('jobsid',$jobsid);
        $jobs_info = D('Jobs')->find($jobsid);
        if(!$jobs_info){
            $jobs_info = D('JobsTmp')->find($jobsid);
        }
        $this->assign('jobs_info',$jobs_info);
        $this->_config_seo(array('title'=>'发布红包职位 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'发布红包职位'));
    	$this->display();
    }
    /**
     * 职位红包保存
     */
    public function set_allowance_job_save(){
        $setsqlarr['payment'] = I('request.payment','wxpay','trim');
        if(!in_array($setsqlarr['payment'], array('wxpay','alipay'))){
            $this->ajaxReturn(0,'支付方式错误！');
        }
        $setsqlarr['jobsid'] = I('request.jobsid',0,'intval');
        !$setsqlarr['jobsid'] && $this->ajaxReturn(0,'职位id错误！');
        $model = D('Allowance/AllowanceInfo');
        $setsqlarr['type_alias'] = I('request.type_alias');
        if(!array_key_exists($setsqlarr['type_alias'], $model->type_alias_cn)){
            $this->ajaxReturn(0,'红包类型错误！');
        }
        if(false === $config = F('allowance_config')){
            $config = D('Allowance/AllowanceConfig')->config_cache();
        }
        $setsqlarr['per_amount'] = I('request.per_amount',0,'intval');
        !$setsqlarr['per_amount'] && $this->ajaxReturn(0,'请填写红包金额！');
        if($setsqlarr['per_amount']<$config['amount_range'][0]) $this->ajaxReturn(0,'红包金额不能小于'.$config['amount_range'][0].'元');
        if($setsqlarr['per_amount']>$config['amount_range'][1]) $this->ajaxReturn(0,'红包金额不能大于'.$config['amount_range'][1].'元');
        $setsqlarr['total_num'] = I('request.total_num',0,'intval');
        !$setsqlarr['total_num'] && $this->ajaxReturn(0,'请填写红包总数量！');
        $setsqlarr['complete_percent'] = I('request.complete_percent',0,'intval');
        $setsqlarr['education'] = I('request.education','','trim');
        $setsqlarr['experience'] = I('request.experience','','trim');
        $setsqlarr['intention_jobs'] = I('request.intention_jobs','','trim');
        $setsqlarr['uid'] = C('visitor.uid');
        $insertid = $model->add_allowance_job($setsqlarr);
        if($insertid){
            // $info = $model->find($insertid);
            // $paysetarr['pay_resource'] = 'allowance';
            // $paysetarr['ordtotal_fee']=$info['pay_amount'];
            // $paysetarr['oid']=$info['oid'];
            // $paysetarr['payFrom'] = 'wap';
            // $paysetarr['type'] = $setsqlarr['payment'];
            // $paysetarr['ordsubject'] = $model->type_alias_cn[$info['type_alias']];
            // $paysetarr['ordbody'] = $model->type_alias_cn[$info['type_alias']];
            // $r = D('Payment')->pay($paysetarr);
            if($setsqlarr['payment']=='wxpay')
            {
                $this->ajaxReturn(1,'保存成功！',U('Company/allowance_wxpay',array('info_id'=>$insertid)));
            }else{
                $this->ajaxReturn(1,'保存成功！',U('Company/allowance_alipay',array('info_id'=>$insertid)));
            }
        }else{
            $this->ajaxReturn(0,'保存失败！');
        }
    }
    public function allowance_wxpay($info_id){
        $model = D('Allowance/AllowanceInfo');
        $info = $model->find($info_id);
        $paysetarr['pay_resource'] = 'allowance';
        $paysetarr['ordtotal_fee']=$info['pay_amount'];
        $paysetarr['oid']=$info['oid'];
        $paysetarr['payFrom'] = 'wap';
        $paysetarr['type'] = $info['payment'];
        $paysetarr['ordsubject'] = $model->type_alias_cn[$info['type_alias']];
        $paysetarr['ordbody'] = $model->type_alias_cn[$info['type_alias']];
        $paysetarr['openid'] = D('MembersBind')->where(array('uid'=>$info['uid'],'type'=>'weixin'))->getField('openid');
        $r = D('Payment')->pay($paysetarr);
        $this->assign('jsApiParameters',$r['jsApiParameters']);
        $this->display();
    }
    public function allowance_alipay($info_id){
        $model = D('Allowance/AllowanceInfo');
        $info = $model->find($info_id);
        $paysetarr['ordtotal_fee']=$info['pay_amount'];
        $paysetarr['oid']=$info['oid'];
        $paysetarr['payFrom'] = 'wap';
        $paysetarr['type'] = $info['payment'];
        $paysetarr['ordsubject'] = $model->type_alias_cn[$info['type_alias']];
        $paysetarr['ordbody'] = $model->type_alias_cn[$info['type_alias']];
        D('Payment')->pay($paysetarr);
    }
    /**
     * 我的打赏
     */
    public function allowance(){
        $this->check_params();
    	if(!C('apply.Allowance')) $this->_empty();
        $jobs_id = I('get.jobs_id',0,'intval');
        if($jobs_id>0){
            $map_info['jobsid'] = array('eq',$jobs_id);
        }
        $type = I('get.type','','trim');
        $type && $map[$type] = array('eq',1);
        $status = I('get.status','','trim');
        $status!='' && $map['status'] = array('eq',$status);
        $member_turn = I('get.member_turn',0,'intval');
        $member_turn>0 && $map['member_turn'] = array('eq',$member_turn);
        $map_info['uid'] = array('eq',C('visitor.uid'));
        $infoid_arr = array();
        $info_list = D('Allowance/AllowanceInfo')->where($map_info)->index('id')->select();
        foreach ($info_list as $key => $value) {
            $infoid_arr[] = $key;
        }
        if(!empty($infoid_arr)){
            $map['info_id'] = array('in',$infoid_arr);
        }else{
            $map['info_id'] = array('eq',0);
        }
        $data_count = D('Allowance/AllowanceRecord')->where($map)->count();
        $pagesize = 10;
        $pager = pager($data_count, $pagesize);
        $page = $pager->fshow();
        $list = D('Allowance/AllowanceRecord')->where($map)->order('id desc')->limit($pager->firstRow . ',' . $pager->listRows)->select();
        foreach ($list as $key => $value) {
            $list[$key]['info'] = $info_list[$value['info_id']];
            $list[$key]['info']['type_cn'] = D('Allowance/AllowanceInfo')->get_alias_cn($info_list[$value['info_id']]['type_alias']);
            $list[$key]['status_cn'] = D('Allowance/AllowanceRecord')->get_status_cn($value['status']);
            $resumeid_arr[] = $value['resumeid'];
            $recordid_arr[] = $value['id'];
        }
        if(!empty($resumeid_arr)){
            $resumelist = D('Resume')->where(array('id'=>array('in',$resumeid_arr)))->index('id')->select();
            foreach ($resumelist as $key => $value) {
                $resumelist[$key]['age'] = date('Y') - $value['birthdate'];
            }
        }else{
            $resumelist = array();
        }
        if(!empty($recordid_arr)){
            $log = D('Allowance/AllowanceRecordLog')->where(array('record_id'=>array('in',$recordid_arr)))->select();
            foreach ($list as $key => $value) {
                foreach ($log as $k => $v) {
                    if($value['id']==$v['record_id']){
                        $list[$key]['log'][$v['step']] = $v;
                    }
                }
            }
        }
        $record['list'] = $list;
        $this->assign("page", $page);
        $this->assign('jobs_id',$jobs_id);
        $this->assign('type',$type);
        $this->assign('status',$status);
        $this->assign('member_turn',$member_turn);
        $this->assign('resumelist',$resumelist);
        $this->assign('record',$record);
        $this->assign('jobslist',D('Jobs')->where(array('uid'=>C('visitor.uid')))->getField('id,jobs_name'));
        $this->assign('status_list',D('Allowance/AllowanceRecord')->status_cn);
        $this->assign('type_list',D('Allowance/AllowanceRecord')->type_cn);
        $this->_config_seo(array('title'=>'我的打赏 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'我的打赏'));
        $this->display();
    }
    /**
     * 打赏详情
     */
    public function allowance_detail($id){
        $record_info = D('Allowance/AllowanceRecord')->where(array('company_uid'=>C('visitor.uid'),'id'=>$id))->find();
        if(!$record_info){
            $this->_404('参数错误！');
        }
        $record_info['status_cn'] = D('Allowance/AllowanceRecord')->status_cn[$record_info['status']];
        $log = D('Allowance/AllowanceRecordLog')->where(array('record_id'=>array('eq',$id)))->select();
        foreach ($log as $k => $v) {
            $record_info['log'][$v['step']] = $v;
        }
        $base_info = D('Allowance/AllowanceInfo')->find($record_info['info_id']);
        $resume_info = D('Resume')->find($record_info['resumeid']);
        $resume_info['age'] = date('Y') - $resume_info['birthdate'];
        $this->assign('record_info',$record_info);
        $this->assign('base_info',$base_info);
        $this->assign('resume_info',$resume_info);
        $this->_config_seo(array('title'=>'打赏详情 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'打赏详情'));
    	$this->display();
    }
    /**
     * 确认已面试
     */
    public function confirm_interview($record_id){
        if(IS_POST){
            $be_score = I('post.score',0,'intval');
            if($be_score){
                $score['uid'] = C('visitor.uid');
                $score['score_experience'] = I('post.score_experience',0,'intval');
                $score['score_education'] = I('post.score_education',0,'intval');
            }else{
                $score = array();
            }
            $r = D('Allowance/AllowanceRecord')->confirm_interview($record_id,$score);
            if($r){
                $this->ajaxReturn(1,'操作成功！');
            }else{
                $this->ajaxReturn(0,D('Allowance/AllowanceRecord')->getError());
            }
        }else{
            $this->assign('record_id',$record_id);
            $this->_config_seo(array('title'=>'确认面试 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'确认面试'));
            $this->display('allowance_confirm_interview');
        }
    }
    /**
     * 面试邀请
     */
    public function allowance_interview_add(){
        if(IS_POST){
            $data['jobs_name'] = I('post.jobs_name','','trim');
            $data['fullname'] = I('post.fullname','','trim');
            $data['company_name'] = I('post.company_name','','trim');
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
            if(!$data['address']){
                $this->ajaxReturn(0,'请填写面试地点');
            }
            $data['contact'] = I('post.contact','','trim');
            if(!$data['contact']){
                $this->ajaxReturn(0,'请填写联系人');
            }
            $data['telephone'] = I('post.telephone','','trim');
            if(!$data['telephone']){
                $this->ajaxReturn(0,'请填写联系电话');
            }
            $data['notes'] = I('post.notes','','trim');
            $data['sms_notice'] = I('post.sms_notice',0,'intval');
            $data['record_id'] = I('post.record_id',0,'intval');
            $reg = D('Allowance/AllowanceInterview')->add($data);
            $this->ajaxReturn(1,'操作成功！');
        }else{
            $record_id = I('get.record_id',0,'intval');
            $resume_id = I('get.resume_id',0,'intval');
            !$resume_id && $this->_404('请选择简历！');
            $jobsid = I('get.jobsid',0,'intval'); 
            $company = M('CompanyProfile')->field('companyname,district_cn,contact,telephone,landline_tel,address')->where(array('uid'=>C('visitor.uid')))->find();
            $resumeinfo = M('Resume')->find($resume_id);
            $jobs = D('Jobs')->find($jobsid);
            !$jobs && $jobs = D('JobsTmp')->find($jobsid);

            // //统计
            // $count_map['personal_uid'] = $resumeinfo['uid'];
            // $count_map['interview'] = 1;
            // $count_map['addtime'] = array('egt',strtotime('-7day'));
            // $count_record = D('AllowanceRecord')->where($count_map)->count();
            // //评价
            // $score = D('AllowanceEvaluateByCompany')->where(array('personal_uid'=>$resumeinfo['uid']))->select();
            // $show_score = false;
            // if($score){
            //     $show_score = true;
            // }
            // if($show_score){
            //     $avg['education'] = D('AllowanceEvaluateByCompany')->where(array('personal_uid'=>$resumeinfo['uid']))->avg('score_education');
            //     $avg['experience'] = D('AllowanceEvaluateByCompany')->where(array('personal_uid'=>$resumeinfo['uid']))->avg('score_experience');
            // }else{
            //     $avg = array();
            // }

            // $this->assign('count_record',$count_record);
            // $this->assign('avg',$avg);
            $this->assign('record_id',$record_id);
            $this->assign('resume_id',$resume_id);
            $this->assign('jobs',$jobs);
            $this->assign('company',$company);
            $this->assign('fullname',$resumeinfo['fullname']);
            $this->_config_seo(array('title'=>'面试邀请 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'面试邀请'));
            $this->display('allowance_interview_add');
        }
    }
    /**
     * 面试详情
     */
    public function allowance_interview_detail($record_id){
        $info = D('Allowance/AllowanceInterview')->where(array('record_id'=>$record_id))->find();
        $this->assign('info',$info);
        $this->_config_seo(array('title'=>'面试详情 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'面试详情'));
        $this->display();
    }
    /**
     * 打赏详情
     */
    public function allowance_jobs_detail($info_id){
        $info = D('Allowance/AllowanceInfo')->where(array('id'=>$info_id))->find();
        $info['type_alias_cn'] = D('Allowance/AllowanceInfo')->type_alias_cn[$info['type_alias']];
        $this->assign('info',$info);
        if(false===$config=F('allowance_config')){
            $config = D('Allowance/AllowanceConfig')->config_cache();
        }
        $this->assign('mobile_address',$config['allow_signon_mobile_address']?$config['allow_signon_mobile_address']:'不限');
        $this->_config_seo(array('title'=>'打赏详情 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'打赏详情'));
        $this->display();
    }
}
?>