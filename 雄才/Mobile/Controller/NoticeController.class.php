<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;

class NoticeController extends MobileController
{
    // 初始化函数
    public function _initialize()
    {
        parent::_initialize();
    }

    public function index()
    {
        if(C('PLATFORM') != 'mobile'){
            redirect(U('Home/Notice/index','',true,C('qscms_site_domain')));
        }
        $this->display();
    }

    /**
     * 公告详情
     */
    public function show()
    {
        if(C('PLATFORM') != 'mobile'){
            redirect(U('Home/Notice/notice_show',array('id'=>intval($_GET['id'])),true,C('qscms_site_domain')));
        }
        $this->display();
    }
}

?>