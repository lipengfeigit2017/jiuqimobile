<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class ScanUploadController extends MobileController
{
    // 初始化函数
    public function _initialize()
    {
        parent::_initialize();
        //dump(I('get.'));
        $uname = I('get.uname', '', 'trim');
        $pwd = I('get.pwd', '', 'trim');
        if($uname && $pwd){
            if ($user = D('Members')->where(array('username'=>$uname))->find()) {
                if ($pwd == $user['password']) {
                    $this->visitor->login($user['uid']);
                    $this->user = $user;
                } else {
                    $this->error('用户名或密码错误！');
                }
            } else {
                $this->error('该用户不存在！');
            }
        } else {
            $this->error('用户名和密码不能为空！');
        }
    }
    /**
     * 简历 - 照片/作品
     */
    public function resume_img()
    {
        $rid = I('get.rid',0,'intval');
        !$rid && $this->error('简历id不能为空！');
        $resume = M('Resume')->where(array('uid'=>$this->user['uid'],'id'=>$rid))->find();
        !$resume && $this->error('该简历不存在！');
        $avatar_default = $resume['sex']==1?'no_photo_male.png':'no_photo_female.png';
        if($this->user['avatars']){
            $resume['avatars'] = attach($this->user['avatars'],'avatar');
            $resume['is_avatars'] = 1;
        }else{
            $resume['avatars'] = attach($avatar_default,'resource');
            $resume['is_avatars'] = 0;
        }
        $img = M('ResumeImg')->where(array('resume_id'=>$rid))->select();
        $this->assign('info',$resume);
        $this->assign('img',$img);
        $this->display('resume_img');
    }
    /**
     * 企业 - 营业执照
     */
    public function certificate_img()
    {
        $company = M('CompanyProfile')->where(array('uid'=>$this->user['uid']))->find();
        !$company && $this->error('该公司不存在！');
        $this->assign('info',$company);
        $this->display('certificate_img');
    }
    /**
     * 企业 - 企业风采
     */
    public function company_img()
    {
        $company = M('CompanyProfile')->field('id,uid,companyname,logo')->where(array('uid'=>$this->user['uid']))->find();
        !$company && $this->error('该公司不存在！');
        $img = M('CompanyImg')->where(array('uid'=>$this->user['uid']))->select();
        $this->assign('info',$company);
        $this->assign('img',$img);
        $this->display('company_img');
    }
}

?>