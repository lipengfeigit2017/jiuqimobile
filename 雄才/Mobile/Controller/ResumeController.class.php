<?php
namespace Mobile\Controller;

use Mobile\Controller\MobileController;

class ResumeController extends MobileController
{
    // 初始化函数
    public function _initialize()
    {
        parent::_initialize();
        if(I('get.code','','trim')){
            $reg = $this->get_weixin_openid(I('get.code','','trim'));
            $reg && $this->redirect('members/apilogin_binding');
        }
    }

    public function index()
    {
        if(C('PLATFORM') != 'mobile'){
            redirect(U('Home/Resume/resume_list','',true,C('qscms_site_domain')));
        }
        $citycategory = I('get.citycategory','','trim');
        $where = array(
            '类型' => 'QS_citycategory',
            '地区分类' => (C('SUBSITE_VAL.s_id') > 0 && !$citycategory) ? C('SUBSITE_VAL.s_district') : $citycategory
        );
        $classify = new \Common\qscmstag\classifyTag($where);
        $city = $classify->run();
        $seo = array('citycategory'=>$city['select']['categoryname'],'key'=>I('request.key'));
        $page_seo = D('Page')->get_page();
	    $ntitle = $page_seo[strtolower(MODULE_NAME).'_'.strtolower(CONTROLLER_NAME).'_'.strtolower(ACTION_NAME)];
	    $ntitle['header_title'] = I('request.key')?urldecode(urldecode(I('request.key'))):'找人才';
	    $this->_config_seo($ntitle,$seo);
        $this->display();
    }
    /**
     * 简历详情
     */
    public function show()
    {   
        if(C('PLATFORM') != 'mobile'){
            redirect(U('Home/Resume/resume_show',array('id'=>intval($_GET['id'])),true,C('qscms_site_domain')));
        }
        $this->wx_share();
        $this->display();
    }
}

?>