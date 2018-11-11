<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class JobfairController extends MobileController{
    // 初始化函数
    public function _initialize()
    {
        parent::_initialize();
        if(I('get.code','','trim')){
            $reg = $this->get_weixin_openid(I('get.code','','trim'));
            $reg && $this->redirect('members/apilogin_binding');
        }
    }

    /**
     * 招聘会首页
     */
    public function index()
    {
        $this->display();
    }

    /**
     * 招聘会详情
     */
    public function show()
    {
        if (C('visitor.utype')==1) {
            $this->assign('show_booth',1);
        }else{
            $this->assign('show_booth',0);
        }
        $this->wx_share();
        $this->display();
    }

    /**
     * 参会企业
     */
    public function comlist()
    {
        $this->wx_share();
        $this->display();
    }
    /**
     * 展位预定
     * $booth_status  0不可预定 1可预订 2预订成功 3审核中
     */
    public function reserve(){
        $id = I('get.id',0,'intval');
        if(!$id){
            $this->_404('参数错误！');
        }
        if (!C('visitor')) {
            IS_AJAX && $this->ajaxReturn(0, L('login_please'),'',1);
            //非ajax的跳转页面
            $this->redirect('members/login');
        }
        $area = D('Jobfair/JobfairArea')->where(array('jobfair_id'=>$id))->order('area asc')->select();
        foreach ($area as $key => $value) {
            $position[$value['id']] = D('Jobfair/JobfairPosition')->where(array('jobfair_id'=>$id,'area_id'=>$value['id'],'status'=>0))->order('orderid asc')->select();
        }
        foreach ($position as $key => $value) {
            if(empty($value)){
                unset($position[$key]);
            }
        }
        $booth_status = 0;
        if(C('visitor.utype')==1){
            $booth_info = D('Jobfair/JobfairExhibitors')->where(array('jobfair_id'=>$id,'uid'=>C('visitor.uid')))->find();
            if($booth_info){
                if($booth_info['audit']==1){
                    $booth_status = 2;
                }else{
                    $booth_status = 3;
                }
            }else{
                $booth_status = 1;
            }
        }
        $img = M('JobfairPositionImg')->where(array('jobfair_id'=>$id))->select();
        $position_img = array();
        foreach ($img as $key => $value) {
            $arr['src'] = attach($value['img'],'jobfair');
            $arr['w'] = 750;
            $arr['h'] = 400;
            $position_img[] = $arr;
        }
        $has_img = !empty($position_img)?1:0;
        $this->assign('has_img',$has_img);
        $this->assign('position_img',json_encode($position_img));
        $this->assign('booth_info',$booth_info);
        $this->assign('booth_status',$booth_status);
        $this->assign('area',$area);
        $this->assign('position',$position);
        $this->wx_share();
        $this->display();
    }
    /**
     * 展位预定保存
     */
    public function reserve_save(){
        $jobfair_id = I('post.jobfair_id',0,'intval');
        $position_id = I('post.position_id',0,'intval');
        if(!$jobfair_id){
            $this->ajaxReturn(0,'参数错误！');
        }
        if(!$position_id){
            $this->ajaxReturn(0,'请选择展位！');
        }
        
        $r = D('Jobfair/Jobfair')->jobfair_booth(C('visitor'),$jobfair_id,$position_id);
        $this->ajaxReturn($r['state'],$r['msg']);
    }
}
?>