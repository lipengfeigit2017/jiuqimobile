<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class jobsController extends MobileController
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
            redirect(U('Home/Jobs/jobs_list','',true,C('qscms_site_domain')));
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
	    $ntitle['header_title'] = I('request.key')?urldecode(urldecode(I('request.key'))):'找工作';
        $this->_config_seo($ntitle,$seo);
        $this->assign('search_type','jobs');
        $this->wx_share();
        $this->display();
    }

    /**
     * 公司详情
     */
    public function comshow()
    {
        if(C('PLATFORM') != 'mobile'){
            redirect(U('Home/Jobs/com_show',array('id'=>intval($_GET['id'])),true,C('qscms_site_domain')));
        }
        $this->wx_share();
        $this->display();
    }

    /**
     * 职位详情
     */
    public function show()
    {   
	    if(C('PLATFORM') != 'mobile'){
            redirect(U('Home/Jobs/jobs_show',array('id'=>intval($_GET['id'])),true,C('qscms_site_domain')));
		}
	    //$this->_config_seo(array('title'=>'个人会员中心 - '.C('qscms_site_name'),'header_title'=>'个人会员中心'));
        $this->wx_share();
        $this->display();
    }

    /**
     * 企业实地报告
     */
    public function com_report() {
        $id = I('get.id',0,'intval');
        if(C('PLATFORM') != 'mobile'){
            redirect(U('Report/Index/index',array('id'=>$id),true,C('qscms_site_domain')));
        }
        !$id && $this->_empty();
        $where['com_id'] = $id;
        $where['status'] = 1;
        $report = M('CompanyReport')->where($where)->find();
        !$report && $this->_empty();
        $report['img'] && $report['img_arr'] = array_slice(explode('#',$report['img']),0,5);
        $this->assign('info',$report);
        $page_seo = D('Page')->get_page();
        $ntitle = $page_seo[strtolower(MODULE_NAME).'_'.strtolower(CONTROLLER_NAME).'_'.strtolower(ACTION_NAME)];
        $ntitle['header_title'] = '实地认证报告';
        $this->_config_seo($ntitle,$report);
        $this->display();
    }
    
}

?>