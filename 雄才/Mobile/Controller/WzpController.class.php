<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class WzpController extends MobileController
{
    // 初始化函数
    public function _initialize()
    {
        parent::_initialize();
        $this->wx_share();
    }
    /**
     * 企业
     */
    public function com()
    {
        $id = I('get.id',0,'intval');
        $company = D('CompanyProfile')->find($id);
        //统计点赞数
        $praise = D('CompanyPraise')->where(array('company_id'=>$id,'click_type'=>2))->count();
        $show_menu = $company['uid']==C('visitor.uid')?1:0;
        $this->assign('show_menu',$show_menu);
        $this->assign('praise',$praise);
        if($company['telephone_show'] ==1 ){
            if(C('qscms_showjobcontact') == 0){
                $phone_error='企业未填写联系方式';
                $phone_error_tit='确定';
                $phone_url= '';
                $phone_code = 1;
            }else{
                
                if (C('visitor.uid')=='' || C('visitor.username')=='' || C('visitor.uid')==0)
                {
                    $phone_error='对不起，请登录后继续查看企业联系方式';
                    $phone_error_tit='登录';
                    $phone_url= U('Members/login');
                    $phone_code = 0;
                }else{
                    $phone_code = 1;
                }
                
            }
        }else{
                $phone_error='该企业不接受电话咨询，请直接投递简历';
                $phone_error_tit='投递';
                $phone_url = U('Wzp/com',array('id'=>$company['id']));
                $phone_code = 0;
        }
        if(C('visitor.uid') && C('visitor.utype')==2){
            $resume = D('Resume')->where(array('uid'=>C('visitor.uid'),'def'=>1))->find();
        }
        $this->assign('phone_code',$phone_code);
        $this->assign('phone_url',$phone_url);
        $this->assign('phone_error',$phone_error);
        $this->assign('phone_error_tit',$phone_error_tit);
        $this->assign('resume',$resume);
        $tpl = $company['wzp_tpl']?'comred':'com';
        $this->display($tpl);
    }
    /**
     * 给企业点赞
     */
    public function com_praise_click($id){
        if($id>0)
        {   
            //插入访问记录(1->访问  2->点赞  3->分享)
            $insetarr['company_id']=$id;
            $insetarr['uid']=C('visitor.uid');
            $insetarr['click_type']=2;
            $insetarr['addtime']=strtotime(date("Y-m-d"));
            $insetarr['ip']=get_client_ip();
            $insertid = D('CompanyPraise')->add($insetarr);
            if($insertid)
            {
                //统计点赞数
                $praise = D('CompanyPraise')->where(array('company_id'=>$id,'click_type'=>2))->count();
                $this->ajaxReturn(1,'',$praise);
            }
            else
            {
                $this->ajaxReturn(0,'');
            }
        }
        else
        {
            $this->ajaxReturn(0,'');
        }
    }
    /**
     * 分享
     */
    public function com_share($id){
        if($id>0)
        {   
            //插入访问记录(1->访问  2->点赞  3->分享)
            $insetarr['company_id']=$id;
            $insetarr['uid']=C('visitor.uid');
            $insetarr['click_type']=3;
            $insetarr['addtime']=strtotime(date("Y-m-d"));
            $insetarr['ip']=get_client_ip();
            $insertid = D('CompanyPraise')->add($insetarr);
            if($insertid)
            {
                $this->ajaxReturn(1,'ok');
            }
            else
            {
                $this->ajaxReturn(0,'error');
            }
        }
        else
        {
            $this->ajaxReturn(0,'error');
        }
    }
    /**
     * 企业模板
     */
    public function comtpl(){
        $id = I('request.id',0,'intval');
        $company = D('CompanyProfile')->find($id);
        if($company['uid']!=C('visitor.uid')){
            $this->_404('非法操作！');
        }
        if(IS_POST){
            $wzp_tpl = I('post.wzp_tpl',0,'intval');
            D('CompanyProfile')->where(array('uid'=>C('visitor.uid')))->setField('wzp_tpl',$wzp_tpl);
        }else{
            $this->display();
        }
    }
    /**
     * 企业福利
     */
    public function comtag(){
        $id = I('request.id',0,'intval');
        $company = D('CompanyProfile')->find($id);
        if($company['uid']!=C('visitor.uid')){
            $this->_404('非法操作！');
        }
        if(IS_POST){
            $tag = I('post.tag','','trim');
            $setarr['tag']=ltrim($tag,",");
            D('CompanyProfile')->where(array('uid'=>C('visitor.uid')))->save($setarr);
        }else{
            // 企业标签
            $company_tag=explode(",", $company['tag']);
            foreach ($company_tag as $key => $value)
            {
                $val=explode("|", $value);
                $company_tagarr['id'][]=$val[0];
                $company_tagarr['tag_cn'][]=$val[1];
            }
            $this->assign('company_tag',$company_tagarr['id']);
            $this->display();
        }
    }
}

?>