<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class PersonalController extends MobileController{
	public function _initialize() {
        parent::_initialize();
        //访问者控制
        if(I('get.code','','trim') && !in_array(ACTION_NAME,array('order_detail'))){
            $reg = $this->get_weixin_openid(I('get.code','','trim'));
            $reg && $this->redirect('members/apilogin_binding',array('openid'=>$this->openid));
        }
        if (!$this->visitor->is_login) {
            IS_AJAX && $this->ajaxReturn(0, L('login_please'),'',1);
            //非ajax的跳转页面
            $this->redirect('members/login');
        }
        if(C('visitor.utype') !=2){
            IS_AJAX && $this->ajaxReturn(0,'请登录个人账号！');
            $this->redirect('members/index');
        }
        !IS_AJAX && $this->_global_variable();
    }
    protected function _global_variable() {
        // 账号状态 为暂停
        if (C('visitor.status') == 2 && !in_array(ACTION_NAME, array('index'))){
            $this->_404('您的账号处于暂停状态，请联系管理员设为正常后进行操作！',U('Personal/index'));
        }
        // 短信必须验证
        if (C('qscms_sms_open')==1 && (!C('visitor.mobile') || C('visitor.mobile_audit') == 0) && !in_array(ACTION_NAME, array('per_security_tel','resume_add'))){
            $this->_404('您的手机未认证，认证后才能进行其他操作！',U('Personal/per_security_tel'));
        }
        $resume_count = D('Resume')->count_resume(array('uid'=>C('visitor.uid')));//当前用户简历份数
    	if(!$resume_count && !in_array(ACTION_NAME,array('resume_add'))){
            $this->redirect('personal/resume_add');
        }elseif($resume_count && in_array(ACTION_NAME,array('resume_add'))){
            $this->redirect('personal/index');
        }
        if(!S('personal_login_first_'.C('visitor.uid'))){
            S('personal_login_first_'.C('visitor.uid'),1,86400-(time()-strtotime("today")));
            if($resume_count>0){
                $resume = M('Resume')->where(array('uid'=>C('visitor.uid')))->order('def desc')->limit(1)->find();//当前用户默认简历内容
                $this->assign('resume',$resume);//当前用户简历内容
            }
        }
        $this->assign('personal_nav',ACTION_NAME);
    }
    /**
     * [_is_resume 检测简历是否存在]
     * @return boolean [false || 简历信息(按需要添加字段)]
     */
    protected function _is_resume($pid){
        !$pid && $pid = I('request.pid',0,'intval');
        if(!$pid){
            IS_AJAX && $this->ajaxReturn(0,'请正确选择简历！');
            $this->_404('请正确选择简历！');
        }
        //$field = 'id,uid,title,fullname,sex,nature,nature_cn,trade,trade_cn,birthdate,residence,height,marriage_cn,experience_cn,district_cn,wage_cn,householdaddress,education_cn,major_cn,tag,tag_cn,telephone,email,intention_jobs,photo_img,complete_percent,current,current_cn,word_resume';
        if(!$reg = M('Resume')->field()->where(array('id'=>$pid,'uid'=>C('visitor.uid')))->find()) return false;
        $reg['height'] = $reg['height']==0?'':$reg['height'];
        $this->assign('resume',$reg);
        return $reg;
    }
	/*
	*	个人会员中心首页
	*/
	public function index(){
		session('error_login_count',0);
		$uid=C('visitor.uid');
		$visitor=C('visitor');
        $resume_list = D('Resume')->get_resume_list(array('where'=>array('uid'=>$uid),'limit'=>1,'order'=>'def desc','countinterview'=>true,'countdown'=>true,'countapply'=>true,'views'=>true,'stick'=>true,'feedback'=>true));
        $this->assign('points',D('MembersPoints')->get_user_points($uid));//当前用户积分数
		$this->assign('visitor',$visitor);//当前用户积分数
        $this->assign('resume_info',$resume_list[0]);
        $this->_config_seo(array('title'=>'个人会员中心 - '.C('qscms_site_name'),'header_title'=>'个人会员中心'));
		$this->display();
	}
	/*
    **创建简历-基本信息
    */
    public function resume_add(){
        $uid=C('visitor.uid');
        if(IS_POST && IS_AJAX){
            if(C('qscms_sms_open')==1 && (!C('visitor.mobile') || !C('visitor.mobile_audit'))){
                $smsVerify = session('verify_mobile');
                if(!$smsVerify) $this->ajaxReturn(0,'验证码错误！');
                $telephone = I('post.telephone','','trim');
                if($telephone != $smsVerify['mobile']) $this->ajaxReturn(0,'手机号不一致！');//手机号不一致
                if(time()>$smsVerify['time']+600) $this->ajaxReturn(0,'验证码过期！');//验证码过期
                $mobile_rand = I('post.mobile_vcode',0,'intval');
                if($mobile_rand!=$smsVerify['rand']) $this->ajaxReturn(0,'验证码错误！');//验证码错误！
                $setsqlarr['mobile'] = $telephone;
                $setsqlarr['mobile_audit']=1;
                if(false === $reg = M('Members')->where(array('uid'=>C('visitor.uid')))->save($setsqlarr)) $this->ajaxReturn(0,'手机验证失败!');
                !$reg && $this->ajaxReturn(0,"你的手机 {$smsVerify['mobile']} 已经通过验证！");
                D('Members')->update_user_info($setsqlarr,C('visitor'));
                if(C('visitor.utype')=='1'){
                    $rule=D('Task')->get_task_cache(C('visitor.utype'),22);
                    D('TaskLog')->do_task(C('visitor'),22);
                }else{
                    $rule=D('Task')->get_task_cache(C('visitor.utype'),7);
                    D('TaskLog')->do_task(C('visitor'),7);
                }
                write_members_log(C('visitor'),'','手机验证通过（手机号：'.$telephone.'）');
                session('verify_mobile',null);
                $user = C('visitor');
                $user['mobile_audit'] = 1;
                $user['mobile'] = $telephone;
                C('visitor',$user);
            }
            $ints = array('district','sex','birthdate','education','experience','nature','current','wage');
            $trims = array('telephone','fullname','email','intention_jobs_id','trade');
            foreach ($ints as $val) {
                $setsqlarr[$val] = I('post.'.$val,0,'intval');
            }
            foreach ($trims as $val) {
                $setsqlarr[$val] = I('post.'.$val,'','trim,badword');
            }
            foreach(array('major','marriage','householdaddress','residence','qq','weixin') as $val){
            	C('visitor.'.$val) && $setsqlarr[$val] = C('visitor.'.$val);
            }
            $resume_count == 0 && $setsqlarr['def'] = 1;
            $setsqlarr['display_name'] = C('qscms_default_display_name');
            $rst=D('Resume')->add_resume($setsqlarr,C('visitor'));
            if(!$rst['state']) $this->ajaxReturn(0,$rst['error']);
            //$this->ajaxReturn(1,'简历创建成功！',array('url'=>U('personal/resume_add_success',array('pid'=>$rst['id']))));
            $this->ajaxReturn(1,'简历创建成功！',array('url'=>U('personal/resume_guidance',array('pid'=>$rst['id']))));
        }else{
            $category = D('Category')->get_category_cache();
            $birthdate_arr = range(date('Y')-16,date('Y')-65);
            $this->assign('education',$category['QS_education']);//最高学历
            $this->assign('experience',$category['QS_experience']);//工作经验
            $this->assign('current',$category['QS_current']);//目前状态
            $this->assign('jobs_nature',$category['QS_jobs_nature']);//工作性质
            $this->assign('wage',$category['QS_wage']);//期望薪资
            $this->assign('birthdate_arr',$birthdate_arr);
            $this->_config_seo(array('title'=>'创建简历 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'创建简历'));
            $this->display();
        }
    }
	/*
    *	简历-创建成功
    */
	public function resume_add_success(){
        if(false === $resume = $this->_is_resume()) $this->_404('简历不存在或已经删除!');
        $where = array(
            '显示数目' => '12',
            '分页显示' => 1,
            '职位分类' => $resume['intention_jobs_id'],
            '排序' => 'stickrtime'
        );
        $jobs_mod = new \Common\qscmstag\jobs_listTag($where);
        $jobs_list = $jobs_mod->run();
        $this->assign('jobs_list',$jobs_list['list']);
        $this->_config_seo(array('title'=>'创建简历 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'创建简历'));
        $this->display();
    }
	/*
    *	简历-修改
    */
	public function resume_edit(){
        $resume = $this->_is_resume();
        $get_resume_img=M('ResumeImg')->where(array('resume_id'=>$resume['id']))->select();//获取简历附件图片
        $resume['tag_key'] = $resume['tag']?explode(',',$resume['tag']):array();
        $resume['tag_cn'] = $resume['tag_cn']?explode(',',$resume['tag_cn']):array();
        $tags=D('Category')->get_category_cache('QS_resumetag');
        $this->assign('resume',$resume);
        $this->assign('resume_img',$get_resume_img);//获取简历附件图片
        $this->assign('tags',$tags);
        $this->_config_seo(array('title'=>'编辑简历 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'编辑简历'));
        $this->display();
    }
    /**
    * [_ajax_list ajax获取简历信息列表]
    * @param  [type] $type  [要查的数据表名]
    * @param  [type] $field [要附加的字段名称]
    */
    protected function _ajax_list($type,$fields){
        $pid = I('get.pid',0,'intval');
        !$pid && $this->ajaxReturn(0,'请选择简历！');
        $uid=C('visitor.uid');
        $field=$fields ? 'id,pid,'.$fields : 'id,pid';
        if($dataInfo=M($type)->field($field)->where(array('pid'=>$pid,'uid'=>$uid))->select()){
            $this->assign('list',$dataInfo);
            $this->assign('count',count($dataInfo));
            $this->assign('pid',$pid);
        }
    }
    //获取教育经历列表
    public function ajax_get_education_list(){
        $this->_ajax_list('resume_education','startyear,startmonth,endyear,endmonth,school,speciality,education_cn,todate');
        $this->display('Personal/ajax_tpl/'.strtolower(ACTION_NAME));
    }
    //工作经历
    public function ajax_get_work_list(){
        $this->_ajax_list('resume_work','companyname,jobs,achievements,startyear,startmonth,endyear,endmonth,todate');
        $this->display('Personal/ajax_tpl/'.strtolower(ACTION_NAME));
    }
    //培训经历
    public function ajax_get_training_list(){
        $this->_ajax_list('resume_training','startyear,startmonth,endyear,endmonth,agency,course,description,todate');
        $this->display('Personal/ajax_tpl/'.strtolower(ACTION_NAME));
    }
    //语言能力
    public function ajax_get_language_list(){
        $this->_ajax_list('resume_language','language_cn,level_cn');
        $this->display('Personal/ajax_tpl/'.strtolower(ACTION_NAME));
    }
    //获得证书
    public function ajax_get_credent_list(){
        $this->_ajax_list('resume_credent','name,year,month');
        $this->display('Personal/ajax_tpl/'.strtolower(ACTION_NAME));
    }
    /**
     * [_del_data 删除简历信息]
     */
    protected function _del_data($type){
        $id = I('request.id',0,'intval');
        $pid = I('request.pid',0,'intval');
        if(!$pid || !$id) $this->ajaxReturn(0,'请求缺少参数！');
        if(IS_POST){
            $uid = C('visitor.uid');
            if (M($type)->where(array('id'=>$id,'uid'=>$uid,'pid'=>$pid))->delete()){
                switch($type){
                    case 'ResumeEducation':
                        write_members_log($user,'resume','删除简历教育经历（简历id：'.$pid.'）',false,array('resume_id'=>$pid));break;
                    case 'ResumeWork':
                        write_members_log($user,'resume','删除简历工作经历（简历id：'.$pid.'）',false,array('resume_id'=>$pid));break;
                    case 'ResumeTraining':
                        write_members_log($user,'resume','删除简历培训经历（简历id：'.$pid.'）',false,array('resume_id'=>$pid));break;
                    case 'ResumeLanguage':
                        write_members_log($user,'resume','删除简历语言能力（简历id：'.$pid.'）',false,array('resume_id'=>$pid));break;
                    case 'ResumeCredent':
                        write_members_log($user,'resume','删除简历证书（简历id：'.$pid.'）');break;
                }
                $resume_mod = D('Resume');
                $resume_mod->check_resume($uid,$pid);//更新简历完成状态
                $this->ajaxReturn(1,'删除成功！');
            }else{
                $this->ajaxReturn(0,'删除失败！');
            }
        }
    }
    //删除教育经历
    public function del_education(){
        $this->_del_data('ResumeEducation');
    }
    //删除工作经历
    public function del_work(){
        $this->_del_data('ResumeWork');
    }
    //删除培训经历
    public function del_training(){
        $this->_del_data('ResumeTraining');
    }
    //删除语言能力
    public function del_language(){
        $this->_del_data('ResumeLanguage');
    }
    //删除证书
    public function del_credent(){
        $this->_del_data('ResumeCredent'); 
    }
    //删除简历附件
    public function del_img(){
        if(IS_POST){
            $img_id = I('request.id',0,'intval');
            !$img_id && $this->ajaxReturn(0,'请选择要删除的图片！');
            $uid = C('visitor.uid');
            $img_mod = M('ResumeImg');
            $row=$img_mod->where(array('id'=>$img_id,'uid'=>$uid))->field('img,resume_id')->find();
            $size = explode(',',C('qscms_resume_img_size'));
            @unlink(C('qscms_attach_path')."photo/".$row['img']);
            if(C('qscms_qiniu_open')==1){
                $qiniu = new \Common\ORG\qiniu;
                $qiniu->delete($row['img']);
            }
            foreach ($size as $val) {
                @unlink(C('qscms_attach_path')."photo/{$row['img']}_{$val}x{$val}.jpg");
                if(C('qscms_qiniu_open')==1){
                    $thumb_name = $qiniu->getThumbName($row['img'],$val,$val);
                    $qiniu->delete($thumb_name);
                }
            }
            if(false === $img_mod->where(array('id'=>$img_id,'uid'=>$uid))->delete()) $this->ajaxReturn(0,'删除失败！');
            //写入会员日志
            write_members_log(C('visitor'),'resume','删除简历附件（简历id：'.intval($row['resume_id']).'）',false,array('resume_id'=>intval($row['resume_id'])));
            D('Resume')->check_resume(C('visitor.uid'),intval($row['resume_id']));//更新简历完成状态
            $this->ajaxReturn(1,'删除成功！');
        }
    }
    /**
     * [resume_edit_title 修改简历标题]
     */
    public function resume_edit_title(){
        if(false === $resume = $this->_is_resume()) $this->ajaxReturn(0,'简历不存在或已经删除!');
        $title = I('post.title','','trim,badword');
        $rst=D('Resume')->save_resume(array('title'=>$title),$resume['id'],C('visitor'));
        if($rst['state']) $this->ajaxReturn(1,'数据保存成功！');
        $this->ajaxReturn(0,$rst['error']);
    }
    /*
    **隐私设置更新数据库
    */
    public function save_resume_privacy(){
        $pid = I('post.pid',0,'intval');
        !$pid && $this->ajaxReturn(0,'请选择简历!');
        $setsqlarr['display']=I('post.display',0,'intval');
        // $setsqlarr['display_name']=I('post.display_name',0,'intval');
        // $setsqlarr['photo_display']=I('post.photo_display',0,'intval');
        $uid=C('visitor.uid');
        $where = array('id'=>$pid,'uid'=>$uid);
        if(false !== M('Resume')->where($where)->save($setsqlarr)){
            $reg = D('Resume')->resume_index($pid);
            if(!$reg['state']) $this->ajaxReturn(0,$reg['error']);
            //写入会员日志
            write_members_log(C('visitor'),'resume','保存显示/隐藏设置（简历id：'.$pid.'）',false,array('resume_id'=>$pid));
            $this->ajaxReturn(1,'显示/隐藏设置成功!');
        }else{
            $this->ajaxReturn(0,'显示/隐藏设置失败，请重新操作!');
        }
    }
	/*
    *	简历-修改 - -基本信息
    */
	public function resume_edit_basis(){
        $resume = $this->_is_resume();
        if(IS_AJAX){
            $ints = array('sex','birthdate','education','major','experience','email_notify','height','marriage');
            $trims = array('telephone','fullname','residence','email','householdaddress','qq','weixin');
            foreach ($ints as $val) {
                $setsqlarr[$val] = I('post.'.$val,0,'intval');
            }
            foreach ($trims as $val) {
                $setsqlarr[$val] = I('post.'.$val,'','trim,badword');
            }
            $uid=C('visitor.uid');
            if(C('qscms_audit_edit_resume')!="-1") D('ResumeEntrust')->set_resume_entrust($resume['id'],$uid);//添加简历自动投递功能
            $rst=D('Resume')->save_resume($setsqlarr,$resume['id'],C('visitor'));
            $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
            if($rst['state']) $this->ajaxReturn(1,'数据保存成功！',$data);
            $this->ajaxReturn(0,$rst['error']);
        }
        $category = D('Category')->get_category_cache();
        $birthdate_arr = range(date('Y')-16,date('Y')-65);
        $this->assign('education',$category['QS_education']);//最高学历
        $this->assign('experience',$category['QS_experience']);//工作经验
        $this->assign('birthdate_arr',$birthdate_arr);
        $this->_config_seo(array('title'=>'基本信息 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'基本信息'));
        $this->display();
    }
	/*
    *	简历-修改 - -求职意向
    */
	public function resume_edit_intent(){
        $resume = $this->_is_resume();
        if(IS_AJAX){
            $uid=C('visitor.uid');
            $pid = I('post.pid',0,'intval');
            !$pid && $this->ajaxReturn(0,'请选择简历！');
            $setsqlarr['intention_jobs_id']=I('post.intention_jobs_id','','trim,badword');
            $setsqlarr['trade']=I('post.trade','','trim,badword');//期望行业
            $setsqlarr['district']=I('post.district','','trim,badword');//工作地区
            $setsqlarr['nature']=I('post.nature',0,'intval');//工作性质
            $setsqlarr['current']=I('post.current',0,'intval');
            $setsqlarr['wage']=I('post.wage',0,'intval');//期望薪资
            if(C('qscms_audit_edit_resume')!="-1") D('ResumeEntrust')->set_resume_entrust($resume['id'],$uid);//添加简历自动投递功能
            $rst=D('Resume')->save_resume($setsqlarr,$resume['id'],C('visitor'));
            $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
            $data['attach'] = $rst['attach'];
            if($rst['state']) $this->ajaxReturn(1,'求职意向修改成功！',$data);
            $this->ajaxReturn(0,$rst['error']);
        }
        $category = D('Category')->get_category_cache();
        $this->assign('current',$category['QS_current']);//目前状态
        $this->assign('jobs_nature',$category['QS_jobs_nature']);//工作性质
        $this->assign('wage',$category['QS_wage']);//期望薪资
        $this->_config_seo(array('title'=>'求职意向 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'求职意向'));
        $this->display();
    }
	/*
    *	简历-修改 - -自我描述
    */
	public function resume_edit_description(){
        $resume = $this->_is_resume();
        if(IS_AJAX){
            $specialty = I('post.specialty','','trim,badword');
            !$specialty && $this->ajaxReturn(0,'请输入自我描述!');
            $rst=D('Resume')->save_resume(array('specialty'=>$specialty),$resume['id'],C('visitor'));
            if(!$rst['state']) $this->ajaxReturn(0,$rst['error']);
            write_members_log(C('visitor'),'resume','保存简历自我描述（简历id：'.$resume['id'].'）',false,array('resume_id'=>$resume['id']));
            $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
            $this->ajaxReturn(1,'简历自我描述修改成功',$data);
        }
        $this->_config_seo(array('title'=>'自我描述 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'自我描述'));
        $this->display();
    }
	/*
    *	简历-修改 - -教育经历
    */
	public function resume_edit_edu(){
        $resume = $this->_is_resume();
        $id = I('request.id',0,'intval');
        if(IS_AJAX){
            $setsqlarr['uid'] = C('visitor.uid');
            $setsqlarr['school'] = I('post.school','','trim,badword');
            $setsqlarr['speciality'] = I('post.speciality','','trim,badword');
            $setsqlarr['education'] = I('post.education',0,'intval');
            $setsqlarr['startyear'] = I('post.startyear',0,'intval');
            $setsqlarr['startmonth'] = I('post.startmonth',0,'intval');
            $setsqlarr['endyear'] = I('post.endyear',0,'intval');
            $setsqlarr['endmonth'] = I('post.endmonth',0,'intval');
            $setsqlarr['todate'] = I('post.todate',0,'intval'); // 至今
            // 选择至今就不判断结束时间了
            if ($setsqlarr['todate'] == 1) {
                if(!$setsqlarr['startyear'] || !$setsqlarr['startmonth']) $this->ajaxReturn(0,'请选择就读时间！');
                if($setsqlarr['startyear'] > intval(date('Y'))) $this->ajaxReturn(0,'就读开始时间不允许大于毕业时间！');
                if($setsqlarr['startyear'] == intval(date('Y')) && $setsqlarr['startmonth'] >= intval(date('m'))) $this->ajaxReturn(0,'就读开始时间需小于毕业时间！');
            } else {
                if(!$setsqlarr['startyear'] || !$setsqlarr['startmonth'] || !$setsqlarr['endyear'] || !$setsqlarr['endmonth']) $this->ajaxReturn(0,'请选择就读时间！');

                if($setsqlarr['startyear'] > intval(date('Y'))) $this->ajaxReturn(0,'就读开始时间不允许大于当前时间！');
                if($setsqlarr['startyear'] == intval(date('Y')) && $setsqlarr['startmonth'] >= intval(date('m'))) $this->ajaxReturn(0,'就读开始时间需小于当前时间！');
                if($setsqlarr['endyear'] > intval(date('Y'))) $this->ajaxReturn(0,'就读结束时间不允许大于当前时间！');
                if($setsqlarr['endyear'] == intval(date('Y')) && $setsqlarr['endmonth'] > intval(date('m'))) $this->ajaxReturn(0,'就读结束时间不允许大于当前时间！');

                if($setsqlarr['startyear'] > $setsqlarr['endyear']) $this->ajaxReturn(0,'就读开始时间不允许大于毕业时间！');
                if($setsqlarr['startyear'] == $setsqlarr['endyear'] && $setsqlarr['startmonth'] >= $setsqlarr['endmonth']) $this->ajaxReturn(0,'就读开始时间需小于毕业时间！');
            }
            $education=D('Category')->get_category_cache('QS_education');
            $setsqlarr['education_cn'] = $education[$setsqlarr['education']];
            if(false === $resume = $this->_is_resume()) $this->ajaxReturn(0,'请先填写简历基本信息！');
            $setsqlarr['pid'] = $resume['id'];
            $education=M('ResumeEducation')->where(array('pid'=>$setsqlarr['pid'],'uid'=>$setsqlarr['uid']))->count();//获取教育经历数量
            if (count($education)>=6) $this->ajaxReturn(0,'教育经历不能超过6条！');
            if($id){
                $setsqlarr['id'] = $id;
                $name = 'save_resume_education';
            }else{
                $name = 'add_resume_education';
            }
            $reg = D('ResumeEducation')->$name($setsqlarr,C('visitor'));
            if($reg['state']) {
                $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
                if(!$id){
                    $setsqlarr['id'] = $reg['id'];
                    $this->assign('list',array($setsqlarr));
                }else{
                    $this->assign('list',array($reg['data']));
                }
                $data['html'] = $this->fetch('Personal/ajax_tpl/ajax_get_education_list');
                $this->ajaxReturn(1,'教育经历保存成功！',$data);
            }else{
                $this->ajaxReturn(0,$reg['error']);
            }
        }
        $info=M('ResumeEducation')->where(array('id'=>$id,'pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $category = D('Category')->get_category_cache();
        $this->assign('info',$info);
        $this->assign('education',$category['QS_education']);//最高学历
        $this->_config_seo(array('title'=>'教育经历 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'教育经历'));
        $this->display();
    }
	/*
    *	简历-修改 - -工作经历
    */
	public function resume_edit_work(){
        $resume = $this->_is_resume();
        $id = I('request.id',0,'intval');
        if(IS_AJAX){
            $setsqlarr['uid'] = C('visitor.uid');
            $setsqlarr['companyname'] = I('post.companyname','','trim,badword');
            $setsqlarr['achievements'] = I('post.achievements','','trim,badword');
            $setsqlarr['jobs'] = I('post.jobs','','trim,badword');
            $setsqlarr['startyear'] = I('post.startyear',0,'intval');
            $setsqlarr['startmonth'] = I('post.startmonth',0,'intval');
            $setsqlarr['endyear'] = I('post.endyear',0,'intval');
            $setsqlarr['endmonth'] = I('post.endmonth',0,'intval');
            $setsqlarr['todate'] = I('post.todate',0,'intval'); // 至今
            // 选择至今就不判断结束时间了
            if ($setsqlarr['todate'] == 1) {
                if(!$setsqlarr['startyear'] || !$setsqlarr['startmonth']) $this->ajaxReturn(0,'请选择工作时间！');
                if($setsqlarr['startyear'] > intval(date('Y'))) $this->ajaxReturn(0,'工作开始时间不允许大于当前时间！');
                if($setsqlarr['startyear'] == intval(date('Y')) && $setsqlarr['startmonth'] >= intval(date('m'))) $this->ajaxReturn(0,'工作开始时间需小于当前时间！');
            } else {
                if(!$setsqlarr['startyear'] || !$setsqlarr['startmonth'] || !$setsqlarr['endyear'] || !$setsqlarr['endmonth']) $this->ajaxReturn(0,'请选择工作时间！');

                if($setsqlarr['startyear'] > intval(date('Y'))) $this->ajaxReturn(0,'工作开始时间不允许大于当前时间！');
                if($setsqlarr['startyear'] == intval(date('Y')) && $setsqlarr['startmonth'] >= intval(date('m'))) $this->ajaxReturn(0,'工作开始时间需小于当前时间！');
                if($setsqlarr['endyear'] > intval(date('Y'))) $this->ajaxReturn(0,'工作结束时间不允许大于当前时间！');
                if($setsqlarr['endyear'] == intval(date('Y')) && $setsqlarr['endmonth'] > intval(date('m'))) $this->ajaxReturn(0,'工作结束时间不允许大于当前时间！');
                
                if($setsqlarr['startyear'] > $setsqlarr['endyear']) $this->ajaxReturn(0,'工作开始时间不允许大于结束时间！');
                if($setsqlarr['startyear'] == $setsqlarr['endyear'] && $setsqlarr['startmonth'] >= $setsqlarr['endmonth']) $this->ajaxReturn(0,'工作开始时间需小于结束时间！');
            }
            if(false === $resume = $this->_is_resume()) $this->ajaxReturn(0,'请先填写简历基本信息！');
            $setsqlarr['pid'] = $resume['id'];
            $work=M('ResumeWork')->where(array('pid'=>$setsqlarr['pid'],'uid'=>$setsqlarr['uid']))->count();//获取教育经历数量
            if(count($work)>=6) $this->ajaxReturn(0,'工作经历不能超过6条！');
            if($id){
                $setsqlarr['id'] = $id;
                $name = 'save_resume_work';
            }else{
                $name = 'add_resume_work';
            }
            $reg=D('ResumeWork')->$name($setsqlarr,C('visitor'));
            if($reg['state']) {
                $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
                if(!$id){
                    $setsqlarr['id'] = $reg['id'];
                    $this->assign('list',array($setsqlarr));
                }else{
                    $this->assign('list',array($reg['data']));
                }
                $data['html'] = $this->fetch('Personal/ajax_tpl/ajax_get_work_list');
                $this->ajaxReturn(1,'工作经历保存成功！',$data);
            }else{
                $this->ajaxReturn(0,$reg['error']);
            }
        }
        $info=M('ResumeWork')->where(array('id'=>$id,'pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $this->assign('info',$info);
        $this->_config_seo(array('title'=>'工作经历 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'工作经历'));
        $this->display();
    }
	/*
    *	简历-修改 - -培训
    */
	public function resume_edit_train(){
        $resume = $this->_is_resume();
        $id = I('request.id',0,'intval');
        if(IS_AJAX){
            $setsqlarr['uid'] = C('visitor.uid');
            $setsqlarr['agency'] = I('post.agency','','trim,badword');
            $setsqlarr['course'] = I('post.course','','trim,badword');
            $setsqlarr['description'] = I('post.description','','trim,badword');
            $setsqlarr['startyear'] = I('post.startyear',0,'intval');
            $setsqlarr['startmonth'] = I('post.startmonth',0,'intval');
            $setsqlarr['endyear'] = I('post.endyear',0,'intval');
            $setsqlarr['endmonth'] = I('post.endmonth',0,'intval');
            $setsqlarr['todate'] = I('post.todate',0,'intval'); // 至今
            // 选择至今就不判断结束时间了
            if ($setsqlarr['todate'] == 1) {
                if(!$setsqlarr['startyear'] || !$setsqlarr['startmonth']) $this->ajaxReturn(0,'请选择培训时间！');
                if($setsqlarr['startyear'] > intval(date('Y'))) $this->ajaxReturn(0,'培训开始时间不允许大于毕业时间！');
                if($setsqlarr['startyear'] == intval(date('Y')) && $setsqlarr['startmonth'] >= intval(date('m'))) $this->ajaxReturn(0,'培训开始时间需小于毕业时间！');
            } else {
                if(!$setsqlarr['startyear'] || !$setsqlarr['startmonth'] || !$setsqlarr['endyear'] || !$setsqlarr['endmonth']) $this->ajaxReturn(0,'请选择培训时间！');
                if($setsqlarr['startyear'] > intval(date('Y'))) $this->ajaxReturn(0,'培训开始时间不允许大于当前时间！');
                if($setsqlarr['startyear'] == intval(date('Y')) && $setsqlarr['startmonth'] >= intval(date('m'))) $this->ajaxReturn(0,'培训开始时间需小于当前时间！');
                if($setsqlarr['endyear'] > intval(date('Y'))) $this->ajaxReturn(0,'培训结束时间不允许大于当前时间！');
                if($setsqlarr['endyear'] == intval(date('Y')) && $setsqlarr['endmonth'] > intval(date('m'))) $this->ajaxReturn(0,'培训结束时间不允许大于当前时间！');
                if($setsqlarr['startyear'] > $setsqlarr['endyear']) $this->ajaxReturn(0,'培训开始时间不允许大于毕业时间！');
                if($setsqlarr['startyear'] == $setsqlarr['endyear'] && $setsqlarr['startmonth'] >= $setsqlarr['endmonth']) $this->ajaxReturn(0,'培训开始时间需小于毕业时间！');
            }
            if(false === $resume = $this->_is_resume()) $this->ajaxReturn(0,'请先填写简历基本信息！');
            $setsqlarr['pid'] = $resume['id'];
            $training=M('ResumeTraining')->where(array('pid'=>$setsqlarr['pid'],'uid'=>$setsqlarr['uid']))->count();//获取教育经历数量
            if(count($training)>=6) $this->ajaxReturn(0,'培训经历不能超过6条！');
            if($id){
                $setsqlarr['id'] = $id;
                $name = 'save_resume_training';
            }else{
                $name = 'add_resume_training';
            }
            $reg=D('ResumeTraining')->$name($setsqlarr,C('visitor'));
            if($reg['state']) {
                $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
                $this->ajaxReturn(1,'培训经历保存成功！',$data);
            }else{
                $this->ajaxReturn(0,$reg['error']);
            }
        }
        $info=M('ResumeTraining')->where(array('id'=>$id,'pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $this->assign('info',$info);
        $this->_config_seo(array('title'=>'培训经历 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'培训经历'));
        $this->display();
    }
	/*
    *	简历-修改 - -证书
    */
	public function resume_edit_certificate(){
        $resume = $this->_is_resume();
        $id = I('request.id',0,'intval');
        if(IS_AJAX){
            $setsqlarr['uid'] = C('visitor.uid');
            $setsqlarr['name'] = I('post.name','','trim,badword');
            $setsqlarr['year'] = I('post.year','','trim,badword');
            $setsqlarr['month'] = I('post.month','','trim,badword');
            if(false === $resume = $this->_is_resume()) $this->ajaxReturn(0,'请先填写简历基本信息！');

            if(!$setsqlarr['year'] || !$setsqlarr['month']) $this->ajaxReturn(0,'请选择获得证书时间！');
            if($setsqlarr['year'] > intval(date('Y'))) $this->ajaxReturn(0,'获得证书时间不能大于当前时间！');
            if($setsqlarr['year'] == intval(date('Y')) && $setsqlarr['month'] > intval(date('m'))) $this->ajaxReturn(0,'获得证书时间不能大于当前时间！');
           
            $setsqlarr['pid'] = $resume['id'];
            $credent=M('ResumeCredent')->where(array('pid'=>$setsqlarr['pid'],'uid'=>$setsqlarr['uid']))->count();//获取证书数量
            if(count($credent)>=6) $this->ajaxReturn(0,'证书不能超过6条！');
            if($id){
                $setsqlarr['id'] = $id;
                $name = 'save_resume_credent';
            }else{
                $name = 'add_resume_credent';
            }
            $reg=D('ResumeCredent')->$name($setsqlarr,C('visitor'));
            if($reg['state']) {
                $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
                $this->ajaxReturn(1,'证书保存成功！',$data);
            }else{
                $this->ajaxReturn(0,$reg['error']);
            }
        }
        $info=M('ResumeCredent')->where(array('id'=>$id,'pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $this->assign('info',$info);
        $this->_config_seo(array('title'=>'获得证书 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'获得证书'));
        $this->display();
    }
	/*
    *	简历-修改 - -语言
    */
	public function resume_edit_lang(){
        $resume = $this->_is_resume();
        $id = I('request.id',0,'intval');
        $category = D('Category')->get_category_cache();
        if(IS_AJAX){
            $count = M('ResumeLanguage')->where(array('pid'=>$resume['id'],'uid'=>C('visitor.uid')))->count('id');
            if ($count>6) $this->ajaxReturn(0,'语言能力不能超过6条！');
            $language['uid'] = C('visitor.uid');
            $language['pid'] = $resume['id'];
            $language['language'] = I('post.language',0,'intval');
            $language['level'] = I('post.level',0,'intval');
            $language['language_cn'] = $category['QS_language'][$language['language']];
            $language['level_cn'] = $category['QS_language_level'][$language['level']];
            $is = M('ResumeLanguage')->where(array('pid'=>$resume['id'],'uid'=>C('visitor.uid'),'language'=>$language['language']))->find();
            $is && $this->ajaxReturn(0,'语言能力不能重复添加！');
            if($id){
                $language['id'] = $id;
                $name = 'save_resume_language';
            }else{
                $name = 'add_resume_language';
            }
            $reg=D('ResumeLanguage')->$name($language,C('visitor'));
            if($reg['state']) {
                $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
                $this->ajaxReturn(1,'语言能力保存成功！',$data);
            }else{
                $this->ajaxReturn(0,$reg['error']);
            }
        }
        $info=M('ResumeLanguage')->where(array('id'=>$id,'pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $this->assign('info',$info);
        $this->assign('language',$category['QS_language']);//最高学历
        $this->assign('level',$category['QS_language_level']);
        $this->_config_seo(array('title'=>'语言能力 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'语言能力'));
        $this->display();
    }
	/*
    *	简历-修改 - -特长标签
    */
	public function resume_edit_speciality(){
        $resume = $this->_is_resume();
        $tags=D('Category')->get_category_cache('QS_resumetag');
        if(IS_AJAX){
            $uid=C('visitor.uid');
            $tag_cn = I('post.tag_cn','','badword');
            $setarr['tag_cn']=$tag_cn?implode(",", $tag_cn):'';
            $tag=I('post.tag','','badword');
            $setarr['tag']=$tag?implode(",", $tag):'';
            foreach ($tag as $key => $val) {
                $setarr['tag_cn'].=",{$tags[$val]}";
            }
            $setarr['tag_cn'] = ltrim($setarr['tag_cn'],',');
            if(!$setarr['tag_cn']) $s = 2;
            $resume_mod = D('Resume');
            if(false !== $resume_mod->where(array('id'=>$resume['id'],'uid'=>$uid))->save($setarr)){
                $resume_mod->check_resume($uid,$resume['id']);//更新简历完成状态
                //写入会员日志
                write_members_log(C('visitor'),'resume','保存简历特长标签（简历id：'.$resume['id'].'）',false,array('resume_id'=>$resume['id']));
                $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
                $this->ajaxReturn(1,'简历特长标签修改成功！',$data);
            } 
            $this->ajaxReturn(0,'保存失败！');
        }
        $resume['tag_key'] = $resume['tag']?explode(',',$resume['tag']):array();
        $resume['tag_cn'] = $resume['tag_cn']?explode(',',$resume['tag_cn']):array();
        $tag_arr = array_chunk($tags,12,true);
        $this->assign('tags',$tags);
        $this->assign('resume',$resume);
        $this->assign('tag_arr',$tag_arr);
        $this->_config_seo(array('title'=>'特长标签 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'特长标签'));
        $this->display();
    }
	/*
    *	简历-修改 - -照片作品
    */
	public function resume_edit_img(){
        $resume = $this->_is_resume();
        $id = I('request.id',0,'intval');
        $img_mod = D('ResumeImg');
        if(IS_AJAX){
            $count = $img_mod->where(array('resume_id'=>$resume['id'],'uid'=>C('visitor.uid')))->count('id');
            if($count >= 6) $this->ajaxReturn(0,'简历附件最多只可上传6张！');
            $data['resume_id'] = $resume['id'];
            $data['uid'] = C('visitor.uid');
            $data['title'] = I('post.title','','trim,badword');
            $data['img'] = I('post.img','','trim,badword');
            $data['id'] = I('post.id',0,'intval');
            $reg = $img_mod->save_resume_img($data);
            if($reg['state'])
            {
                D('Resume')->check_resume(C('visitor.uid'),intval($data['resume_id']));//更新简历完成状态
                $data['url'] = I('request.news',0,'intval') ? U('personal/resume_replenish',array('pid'=>$resume['id'],'news'=>1)) : U('personal/resume_edit',array('pid'=>$resume['id']));
                $this->ajaxReturn(1,'附件添加成功！',$data);
            }
            $this->ajaxReturn(0,$reg['error']);
        }
        $info = $img_mod->where(array('id'=>$id,'resume_id'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $this->assign('info',$info);
        $this->_config_seo(array('title'=>'照片作品 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'照片作品'));
        $this->display();
    }
	/*
    *	简历-修改-简要的列表 表现方式 完善简历
    */
	public function resume_replenish(){
        $resume = $this->_is_resume();
        $info=M('ResumeEducation')->where(array('pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $resume['education'] = $info ? 1 : 0;
        $info=M('ResumeWork')->where(array('pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $info && $resume['work'] = 1;
        $info=M('ResumeTraining')->where(array('pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $info && $resume['train'] = 1;
        $info=M('ResumeCredent')->where(array('pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $info && $resume['certificate'] = 1;
        $info=M('ResumeLanguage')->where(array('pid'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $info && $resume['lang'] = 1;
        $info = M('ResumeImg')->where(array('resume_id'=>$resume['id'],'uid'=>C('visitor.uid')))->find();
        $info && $resume['img'] = 1;
        $this->assign('resume',$resume);
        $this->_config_seo(array('title'=>'完善简历 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'完善简历'));
        $this->display();
    }
    /**
     * [resume_guidance 简历引导]
     */
    public function resume_guidance(){
        $resume = $this->_is_resume();
        $category = D('Category')->get_category_cache();
        $this->assign('education',$category['QS_education']);//最高学历
        $this->_config_seo(array('title'=>'完善简历 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'完善简历'));
        $this->display();
    }
    /**
     * [set_default 默认简历设置]
     */
    public function set_default(){
        if(false === $resume = $this->_is_resume()) $this->ajaxReturn(0,'简历不存在！');
        if(!$resume['def']){
            $reg = M('Resume')->where(array('uid'=>C('visitor.uid'),'def'=>1))->setfield('def',0);
            false === $reg && $this->ajaxReturn(0,'默认简历设置失败，请重新操作！');
            M('Resume')->where(array('id'=>$resume['id']))->setfield('def',1);
            //写入会员日志
            write_members_log(C('visitor'),'resume','设置默认简历（简历id：'.$resume['id'].'）',false,array('resume_id'=>$resume['id']));
        }
        $this->ajaxReturn(1,'默认简历设置成功！');
    }
    /*
    **委托简历更新数据库
    */
    public function set_entrust(){
        $uid=C('visitor.uid');
        $pid = I('post.pid',0,'intval');
        !$pid && $this->ajaxReturn(0,'您没有选择简历!');
        $setsqlarr['entrust']=I('post.entrust',0,'intval');
        $setsqlarr['entrust_start']=time();
        switch ($setsqlarr['entrust']) {
             case '3':
                $setsqlarr['entrust_end']=strtotime("+3 day");
                break;
            case '7':
                $setsqlarr['entrust_end']=strtotime("+7 day");
                break;
            case '14':
                $setsqlarr['entrust_end']=strtotime("+14 day");
                break;
            case '30':
                $setsqlarr['entrust_end']=strtotime("+30 day");
                break;
            default:
                $this->ajaxReturn(0,'请正确选择委托时间!');
        }
        //设置简历委托
        if(D('ResumeEntrust')->set_resume_entrust($pid,$uid,$setsqlarr)){
            M('Resume')->where(array('id'=>$pid,'uid'=>$uid))->setfield('entrust',$setsqlarr['entrust']);
            //写入会员日志
            write_members_log(C('visitor'),'resume','设置简历委托（简历id：'.$pid.'）',false,array('resume_id'=>$pid));
            $this->ajaxReturn(1,'委托成功!');
        }else{
            $this->ajaxReturn(0,'委托失败!');
        }
    }
    /*
    **设置隐私
    */
    public function set_privacy(){
        $uid=C('visitor.uid');
        $pid = I('post.pid',0,'intval');
        !$pid && $this->ajaxReturn(0,'您没有选择简历!');
        $setsqlarr['display']=I('post.display',0,'intval');
        $where = array('id'=>$pid,'uid'=>$uid);
        if(false !== M('Resume')->where($where)->save($setsqlarr)){
            $reg = D('Resume')->resume_index($pid);
            if(!$reg['state']) $this->ajaxReturn(0,$reg['error']);
            //写入会员日志
            write_members_log(C('visitor'),'resume','保存隐私设置（简历id：'.$pid.'）',false,array('resume_id'=>$pid));
            $this->ajaxReturn(1,'隐私设置成功!');
        }else{
            $this->ajaxReturn(0,'隐私设置失败，请重新操作!');
        }
    }
    /*
    **取消委托简历更新数据库
    */
    public function set_entrust_del(){
        $pid = I('post.pid',0,'intval');
        !$pid && $this->ajaxReturn(0,'您没有选择简历！!');
        $uid = C('visitor.uid');
        if(false !== M('ResumeEntrust')->where(array('resume_id'=>$pid,'uid'=>$uid))->delete()){
            M('Resume')->where(array('id'=>$pid,'uid'=>$uid))->setfield('entrust',0);
            //写入会员日志
            write_members_log(C('visitor'),'resume','取消简历委托（简历id：'.$pid.'）',false,array('resume_id'=>$pid));
            $this->ajaxReturn(1,'取消委托成功！');
        }else{
            $this->ajaxReturn(0,'取消委托失败！');
        }
    }
    /*
    **删除简历更新数据库
    */
    public function set_del_resume(){
        $id = I('request.id',0,'intval');
        !$id && $this->ajaxReturn(0,'您没有选择简历！');
        $resume_num = D('Resume')->count_resume(array('uid'=>C('visitor.uid')));
        if(IS_POST){
            if($resume_num==1){
                $this->ajaxReturn(0,'删除失败，您至少要保留一份简历！');
            }
            $current = D('Resume')->get_resume_one($id);
            if(true === $reg = D('Resume')->del_resume(C('visitor'),$id)){
                if($current['def']==1){
                    D('Resume')->where(array('uid'=>C('visitor.uid')))->order('complete_percent desc')->limit(1)->setField('def',1);
                }
                $this->ajaxReturn(1,'简历删除成功！');
            }else{
                $this->ajaxReturn(0,'删除失败！');
            }
        }
    }
    /**
     * [refresh_resume 刷新简历]
     */
    public function refresh_resume(){
        if(IS_AJAX){
            $pid = I('post.pid',0,'intval');
            !$pid && $pid = M('Resume')->where(array('uid'=>C('visitor.uid')))->order('def desc')->limit(1)->getField('id');
            !$pid && $this->ajaxReturn(0,'请选择简历！');
            $uid = C('visitor.uid');
            $r = D('Resume')->get_resume_list(array('where'=>array('uid'=>$uid,'id'=>$pid),'field'=>'id,title,audit,display'));
            !$r && $this->ajaxReturn(0,"选择的简历不存在！");
            $r[0]['_audit'] != 1 && $this->ajaxReturn(0,"审核中或未通过的简历无法刷新！");
            $r[0]['display'] != 1 && $this->ajaxReturn(0,"简历已关闭，无法刷新！");
            $refresh_log = M('RefreshLog');
            $refrestime = $refresh_log->where(array('uid'=>$uid,'type'=>2001))->order('addtime desc')->getfield('addtime');
            $duringtime=time()-$refrestime;
            $space = C('qscms_per_refresh_resume_space')*60;
            $today = strtotime(date('Y-m-d'));
            $tomorrow = $today+3600*24;
            $count = $refresh_log->where(array('uid'=>$uid,'type'=>2001,'addtime'=>array('BETWEEN',array($today,$tomorrow))))->count();
            if(C('qscms_per_refresh_resume_time')!=0&&($count>=C('qscms_per_refresh_resume_time'))){
                $this->ajaxReturn(0,"每天最多可刷新 ".C('qscms_per_refresh_resume_time')." 次，您今天已达到最大刷新次数！");
            }elseif($duringtime<=$space && $space!=0){
                $this->ajaxReturn(0,C('qscms_per_refresh_resume_space')." 分钟内不允许重复刷新简历！");
            }else{
                //修改目前状态
                $resume = D('Resume');
                if($current = I('post.current',0,'intval')){
                    $data['current'] = $current;
                }
                if($mobile = I('post.mobile','','trime')){
                    $data['telephone'] = $mobile;
                    if(C('visitor.mobile_audit')){
                        $verifycode = I('post.verifycode','','trim');
                        $verify = session('verify_mobile');
                        if (!$verifycode || !$verify['rand'] || $verifycode<>$verify['rand']) $this->ajaxReturn(0,'验证码错误!');
                    }
                }
                if($data){
                    if(true !== $reg = D('Members')->update_user_info($data,C('visitor'),array('id'=>$pid))) $this->ajaxReturn(0,$reg);
                }
                $r = $resume->refresh_resume($pid,C('visitor'));
                $this->ajaxReturn(1,'刷新简历成功！',$r['data']);
            }
        }
    }
	/**
     * 手机招聘
     */
    public function mobile_recruit(){
        
		$this->display('Personal/mobile_recruit');
    }
    /**
     * 面试邀请
     */
    public function jobs_interview(){
        $this->check_params();
        $where['resume_uid']= C('visitor.uid');
        $look=I('get.look',0,'intval');
        $look && $where['personal_look'] = $look;
        $resume_id = I('get.resume_id',0,'intval');
        $resume_id && $where['resume_id'] =$resume_id;
        $settr = I('get.settr',0,'intval');
        if($settr > 0)
        {
            $settr_val=strtotime("-{$settr} day");
            $where['interview_addtime']=array('EGT',$settr_val);
        }
        $company_interview_mod = D('CompanyInterview');
        $interview = $company_interview_mod->get_invitation_pre($where);
        // 最近三天收到的面试邀请数
        $count_three_day = $company_interview_mod->where(array('resume_uid'=>C('visitor.uid'),'interview_addtime'=>array('egt',strtotime('-3 day'))))->count();

        $this->assign('count_three_day',$count_three_day);
        $this->assign('interview',$interview);
        $this->_config_seo(array('title'=>'面试邀请 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'面试邀请'));
        $this->display();
    }
    // 删除面试邀请
    public function interview_del()
    {
        $yid= I('request.y_id','','trim,badword');
        !$yid && $this->ajaxReturn(0,"你没有选择项目！");
        $rst = D('CompanyInterview')->del_interview($yid,C('visitor'));
        if(intval($rst['state']) == 1)
        {
            $this->ajaxReturn(1,"删除成功！");
        }
        else
        {
            $this->ajaxReturn(0,"删除失败！");
        }
    }
    // 面试邀请设为已看
    public function set_interview(){
        $yid= I('request.y_id','','trim,badword');
        !$yid && $this->error("你没有选择项目！");
        $rst=D('CompanyInterview')->set_invitation($yid,C('visitor'),2);
        if(!$rst['state']) $this->ajaxReturn(0,$rst['error']);
        $this->ajaxReturn(1,'设置成功！');
    }
    /**
     * 谁看过我
     */
    public function attention_me(){
        $this->check_params();
        $resume_list = M('Resume')->where(array('uid'=>C('visitor.uid')))->getfield('id,title');
        $resume_id =I('get.resume_id',0,'intval');
        if($resume_id){
            $where['resumeid']=$resume_id; //筛选简历
        }else{
            $where['resumeid']=array('in',array_keys($resume_list));
        }
        $hasdown = I('get.hasdown','','intval');
        if($hasdown != '') $where['hasdown'] =$hasdown;
        $settr=I('get.settr',0,'intval');
        $settr && $where['addtime']=array('gt',strtotime("-".$settr." day")); //筛选 查看时间
        $view_list = D('ViewResume')->m_get_view_resume($where);//获取列表
        $this->assign('view_list',$view_list);
        $this->_config_seo(array('title'=>'谁看过我 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'谁看过我'));
        $this->display();
    }
    /**
     * 删除谁在关注我
     */
    public function attention_me_del()
    {
        $yid= I('request.y_id','','trim,badword');
        !$yid && $this->ajaxReturn(0,"你没有选择项目！");
        $reg=D('ViewResume')->del_view_resume($yid);
        if($reg['state']==1){
            //写入会员日志
            $yid = is_array($yid)?$yid:explode(",", $yid);
            foreach ($yid as $k => $v) {
                write_members_log(C('visitor'),'','删除被关注记录（记录id：'.$v.'）');
            }
            $this->ajaxReturn(1,"删除成功！");
        }else{
            $this->ajaxReturn(0,"删除失败！");
        }
    }
    /**
     * 
     */
    public function jobs_apply(){
        $this->check_params();
        $where['personal_uid']=C('visitor.uid');
        $resume_id =I('get.resume_id',0,'intval');
        $resume_id && $where['resume_id']=$resume_id; //筛选简历
        $settr=I('get.settr',0,'intval');
        $settr && $where['apply_addtime']=array('gt',strtotime("-".$settr." day")); //筛选 申请时间
        //筛选 反馈
        $feedbackArr = array(1=>'企业未查看',2=>'待反馈',3=>'合适',4=>'不合适',5=>'待定',6=>'未接通');
        $feedback=I('get.feedback',0,'intval');
        switch ($feedback) 
        {
            case 1:
                $where['personal_look']=1;
                break;
            case 2:
                $where['personal_look']=2;
                $where['is_reply']=0;
                break;
            case 3:
                $where['personal_look']=2;
                $where['is_reply']=1;
                break;
            case 4:
                $where['personal_look']=2;
                $where['is_reply']=2;
                break;
            case 5:
                $where['personal_look']=2;
                $where['is_reply']=3;
                break;
            case 6:
                $where['personal_look']=2;
                $where['is_reply']=4;
                break;
            default:
                break;
        }
        $personal_apply_mod = D('PersonalJobsApply');
        $apply_list = $personal_apply_mod->get_apply_jobs($where);
        $this->assign('feedbackArr',$feedbackArr);
        $this->assign('apply_list',$apply_list);
        $this->_config_seo(array('title'=>'求职反馈 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'求职反馈'));
        $this->display();
    }
    // 删除已申请职位
    public function jobs_apply_del()
    {
        $yid= I('request.y_id','','trim,badword');
        !$yid && $this->ajaxReturn(0,"你没有选择项目！");
        $n=D('PersonalJobsApply')->del_jobs_apply($yid,C('visitor'));
        if($n['state']==1)
        {
            $this->ajaxReturn(1,"删除成功！");
        }
        else
        {
            $this->ajaxReturn(0,"删除失败！");
        }
    }
    /**
     * 职位收藏夹
     */
    public function jobs_favorites(){
        $this->check_params();
        $where['personal_uid']=C('visitor.uid');
        $settr=I('get.settr',0,'intval');
        $settr && $where['addtime']=array('gt',strtotime("-".$settr." day")); //筛选 收藏时间
        $favorites = D('PersonalFavorites')->get_favorites($where);
        $this->assign('favorites',$favorites);
        $this->_config_seo(array('title'=>'职位收藏夹 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'职位收藏夹'));
        $this->display();
    }
    /**
     * 删除收藏 职位
     */
    public function del_favorites(){
        $did= I('request.did','','trim,badword');
        !$did && $this->ajaxReturn(0,"你没有选择项目！");
        $reg=D('PersonalFavorites')->del_favorites($did,C('visitor'));
        if($reg['state']===true){
            $this->ajaxReturn(1,"删除成功！");
        }else{
            $this->ajaxReturn(0,$reg['error']);
        }
    }
    /**
     * 关注的企业
     */
    public function attention_com(){
        $this->check_params();
        $company = D('PersonalFocusCompany')->get_focus_company(array('uid'=>C('visitor.uid')),10,true);
        $this->assign('company',$company);
        $this->_config_seo(array('title'=>'关注的企业 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'关注的企业'));
        $this->display();
    }
    /**
     * 删除关注的企业
     */
    public function del_focus_company(){
        $id = I('request.id',0,'intval');
        !$id && $this->ajaxReturn(0,'请选择要删除的企业！');
        $reg = M('PersonalFocusCompany')->where(array('uid'=>C('visitor.uid'),'company_id'=>$id))->delete();
        if($reg===false){
            $this->ajaxReturn(0,'删除失败，请重新操作！');
        }
        //写入会员日志
       write_members_log(C('visitor'),'','删除关注的企业（记录id：'.$id.'）');
        $this->ajaxReturn(1,'成功删除关注的企业！');
    }
    public function per_security(){
        $this->_config_seo(array('title'=>'账号安全 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'账号安全'));
        $this->display();
    }
    public function per_security_tel(){
        $this->_config_seo(array('title'=>'手机认证 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'手机认证'));
        $this->display();
    }
    public function per_security_email(){
        $this->_config_seo(array('title'=>'邮箱认证 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'邮箱认证'));
        $this->display();
    }
    public function per_security_user(){
        $this->_config_seo(array('title'=>'修改用户名 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'修改用户名'));
        $this->display();
    }
    public function per_security_pwd(){
        $this->_config_seo(array('title'=>'修改密码 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'修改密码'));
        $this->display();
    }
    public function per_security_log(){
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
        $this->_config_seo(array('title'=>'会员登录日志 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'会员登录日志'));
        $this->display();
    }
    /**
     * [shield_company 屏蔽企业]
     */
    public function shield_company(){
        $keywords=M('PersonalShieldCompany')->where(array('uid'=>C('visitor.uid')))->select();
        $this->assign('keywords',$keywords);
        $this->assign('count',10 - count($keywords));
        $this->_config_seo(array('title'=>'屏蔽企业 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'屏蔽企业'));
        $this->display();
    }
    /**
     * [shield_company_add 添加屏蔽企业关键字]
     */
    public function shield_company_add(){
        $keyword = I('post.comkeyword','','trim,badword');
        !$keyword && $this->ajaxReturn(0,'企业关键字不能为空！');
        $data['uid'] = C('visitor.uid');
        if(10 <= $count = M('PersonalShieldCompany')->where($data)->count()) $this->ajaxReturn(0,'您最多屏蔽 10 个企业关键词！');
        $data['comkeyword'] = $keyword;
        $shield_mod = D('PersonalShieldCompany');
        if(false === $shield_mod->create($data)) $this->ajaxReturn(0,$shield_mod->getError());
        if(false === $data['id'] = $shield_mod->add()) $this->ajaxReturn(0,'企业关键字添加失败，请重新添加！');
        //写入会员日志
        write_members_log(C('visitor'),'','添加屏蔽企业（关键字：'.$keyword.'）');
        $this->ajaxReturn(1,'企业关键字添加成功！',$data);
    }
    /**
     * [shield_company_del 删除屏蔽企业关键字]
     */
    public function shield_company_del(){
        $keyword_id = I('request.keyword_id',0,'intval');
        !$keyword_id && $this->ajaxReturn(0,'请选择关键字！');
        $uid = C('visitor.uid');
        if(IS_AJAX){
            if($reg = M('PersonalShieldCompany')->where(array('id'=>$keyword_id,'uid'=>C('visitor.uid')))->delete()){
                //写入会员日志
                write_members_log(C('visitor'),'','删除屏蔽企业（id：'.$keyword_id.'）');
                $this->ajaxReturn(1,'关键字删除成功！');
            }
            $reg === false && $this->ajaxReturn(0,'关键字删除失败！');
            $this->ajaxReturn(0,'关键字不存在或已经删除！');
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
        $this->_config_seo(array('title'=>'意见反馈 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'意见反馈'));
        $this->display();
    }
    public function per_binding(){
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
        $this->_config_seo(array('title'=>'账号绑定 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'账号绑定'));
        $this->display();
    }
    /**
     * 消息提醒
     */
    public function pms_list(){
        if(I('get.type',0,'intval')){//留言咨询
            $msg_list = D('Msg')->msg_list(C('visitor'),false);
            $this->assign('msg_list',$msg_list);
            $this->_config_seo(array('title'=>'消息提醒 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'消息提醒'));
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
            $this->_config_seo(array('title'=>'消息提醒 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'消息提醒'));
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
        $this->_config_seo(array('title'=>'系统消息 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'系统消息'));
        $this->display();
    }
     /**
     * 咨询详细
     */
    public function pms_consult_show(){
        $id = I('get.id',0,'intval');
        if(!$id){
            $uid = I('get.uid',0,'intval');
            !$uid && $this->_404('请选择求职咨询');
        }else{
            $msg_list = D('Msg')->smsg_list($id,C('visitor'));
            if($msg_list){
                $uid = $msg_list[0]['touid'];
            }
            $this->assign('msg_list',$msg_list);
        }
        $company_profile = M('company_profile')->where(array('uid'=>$uid))->find();
        $this->assign('company_profile',$company_profile);
        $this->_config_seo(array('title'=>'留言咨询 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'留言咨询'));
        $this->display();
    }
    public function resume_preview(){
        $resume_list = D('Resume')->get_resume_list(array('where'=>array('uid'=>C('visitor.uid')),'limit'=>1,'order'=>'def desc'));
        if($resume_list){
            $this->ajaxReturn(1,'',url_rewrite('QS_resumeshow',array('id'=>$resume_list[0]['id'])));
        }else{
            $this->ajaxReturn(0,'您还没有创建有效简历，请先创建简历');
        }
    }
    /**
     * 我的红包
     */
    public function allowance(){
        $type = I('get.type','','trim');
        $type && $map[$type] = array('eq',1);
        $status = I('get.status','','trim');
        $status!='' && $map['status'] = array('eq',$status);
        $member_turn = I('get.member_turn',0,'intval');
        $member_turn>0 && $map['member_turn'] = array('eq',$member_turn);
        $map['personal_uid'] = array('eq',C('visitor.uid'));
        $data_count = D('Allowance/AllowanceRecord')->where($map)->count();
        $pagesize = 10;
        $pager = pager($data_count, $pagesize);
        $page = $pager->fshow();
        $list = D('Allowance/AllowanceRecord')->where($map)->order('id desc')->limit($pager->firstRow . ',' . $pager->listRows)->select();
        $infoid_arr = array();
        foreach ($list as $key => $value) {
            $infoid_arr[] = $value['info_id'];
        }
        if(!empty($infoid_arr)){
            $info_list = D('Allowance/AllowanceInfo')->where(array('id'=>array('in',$infoid_arr)))->index('id')->select();
        }else{
            $info_list = array();
        }
        
        foreach ($info_list as $key => $value) {
            $info_list[$key]['type_cn'] = D('Allowance/AllowanceInfo')->get_alias_cn($value['type_alias']);
        }
        foreach ($list as $key => $value) {
            $list[$key]['status_cn'] = D('Allowance/AllowanceRecord')->get_status_cn($value['status']);
            $recordid_arr[] = $value['id'];
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
        $this->assign('info_list',$info_list);
        $this->assign('type',$type);
        $this->assign('status',$status);
        $this->assign('member_turn',$member_turn);
        $this->assign('record',$record);
        $this->assign('personal_nav','allowance');
        $this->assign('status_list',D('Allowance/AllowanceRecord')->status_cn);
        $this->assign('type_list',D('Allowance/AllowanceRecord')->type_cn);
        $this->_config_seo(array('title'=>'我的红包 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'我的红包'));
        $this->display();
    }
    /**
     * 打赏详情
     */
    public function allowance_detail($id){
        $record_info = D('Allowance/AllowanceRecord')->where(array('personal_uid'=>C('visitor.uid'),'id'=>$id))->find();
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
        $this->_config_seo(array('title'=>'打赏详情 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'打赏详情'));
        $this->display();
    }
    /**
     * 我已面试
     */
    public function ever_interview(){
        $record_id = I('request.record_id',0,'intval');
        if(!$record_id){
            $this->ajaxReturn(0,'参数错误！');
        }
        $company_uid = I('request.company_uid',0,'intval');
        if(IS_POST){
            $setsqlarr['uid'] = C('visitor.uid');
            $setsqlarr['company_uid'] = $company_uid;
            $setsqlarr['record_id'] = $record_id;
            $setsqlarr['interviewer_name'] = I('request.interviewer_name','','trim');
            !$setsqlarr['interviewer_name'] && $this->ajaxReturn(0,'请填写面试官称呼！');
            $setsqlarr['interviewer_age'] = I('request.interviewer_age','','trim');
            !$setsqlarr['interviewer_age'] && $this->ajaxReturn(0,'请填写面试官年龄！');
            $setsqlarr['interviewer_glasses'] = I('request.interviewer_glasses',0,'intval');
            $setsqlarr['interview_time'] = I('request.interview_time','','trim');
            !$setsqlarr['interview_time'] && $this->ajaxReturn(0,'请填写面试时间！');
            $setsqlarr['other'] = I('request.other','','trim');
            $r = D('Allowance/AllowanceRecord')->ever_interview($setsqlarr);
            if($r){
                $this->ajaxReturn(1,'操作成功！');
            }else{
                $this->ajaxReturn(0,D('Allowance/AllowanceRecord')->getError());
            }
        }else{
            $this->assign('record_id',$record_id);
            $this->assign('company_uid',$company_uid);
            $this->_config_seo(array('title'=>'我已面试 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'我已面试'));
            $this->display('ever_interview');
        }
    }
    /**
     * 我已入职
     */
    public function ever_entry(){
        $record_id = I('request.record_id',0,'intval');
        if(!$record_id){
            $this->ajaxReturn(0,'参数错误！');
        }
        if(IS_POST){
            $setsqlarr['record_id'] = $record_id;
            $setsqlarr['department'] = I('request.department','','trim');
            !$setsqlarr['department'] && $this->ajaxReturn(0,'请填写入职部门！');
            $setsqlarr['position'] = I('request.position','','trim');
            !$setsqlarr['position'] && $this->ajaxReturn(0,'请填写职务！');
            $setsqlarr['entry_time'] = I('request.entry_time','','trim');
            !$setsqlarr['entry_time'] && $this->ajaxReturn(0,'请填写入职时间！');
            $r = D('Allowance/AllowanceRecord')->ever_entry($setsqlarr);
            if($r){
                $this->ajaxReturn(1,'操作成功！');
            }else{
                $this->ajaxReturn(0,D('Allowance/AllowanceRecord')->getError());
            }
        }else{
            $this->assign('record_id',$record_id);
            $this->_config_seo(array('title'=>'我已入职 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'我已入职'));
            $this->display('ever_entry');
        }
    }
    /**
     * 面试详情
     */
    public function allowance_interview_detail($record_id){
        $info = D('Allowance/AllowanceInterview')->where(array('record_id'=>$record_id))->find();
        $this->assign('info',$info);
        $this->_config_seo(array('title'=>'面试详情 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'面试详情'));
        $this->display();
    }
    /**
     * 检查完整度，是否需要赠送红包
     */
    public function check_complete_percent_allowance(){
        if(C('qscms_perfected_resume_allowance_open')==1 && C('qscms_perfected_resume_allowance_percent')>0){
            $uid = C('visitor.uid');
            $resume_info = D('Resume')->where(array('uid'=>$uid,'def'=>1))->find();
            if($resume_info['complete_percent']>=C('qscms_perfected_resume_allowance_percent')){
                $r = D('Resume')->perfected_resume_allowance($resume_info['complete_percent'],C('visitor'));
                if($r['status']==2){
                    $this->ajaxReturn(2,'你的简历完整度超过'.C('qscms_perfected_resume_allowance_percent').'%，已获得系统赠送的'.$r['data'].'元随机红包！<br />微信关注 '.(C('qscms_weixin_mpname')?C('qscms_weixin_mpname'):C('qscms_site_name')).' 公众号进行账号绑定后即可领取');
                }else if($r['status']==1){
                    $this->ajaxReturn(1,'你的简历完整度超过'.C('qscms_perfected_resume_allowance_percent').'%，已获得系统赠送的'.$r['data'].'元随机红包！请到微信钱包中查收！');
                }else{
                    $this->ajaxReturn($r['status'],$r['msg'],$r['data']);
                }
            }else{
                $this->ajaxReturn(0,'条件不满足');  
            }
        }else{
            $this->ajaxReturn(0,'条件不满足');
        }
    }
    /**
     * 不再提示
     */
    public function check_complete_percent_allowance_nolonger_notice(){
        M('MembersPerfectedAllowance')->where(array('uid'=>C('visitor.uid')))->save(array('notice'=>0));
    }
}
?>