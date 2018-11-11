<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;

class MallController extends MobileController
{
    // 初始化函数
    public function _initialize()
    {
        parent::_initialize();
    }

    /**
     * 商城首页
     */
    public function index()
    {
        $uid = C('visitor.uid');
        $utype = C('visitor.utype');
        $task_url = $utype==1?U('CompanyService/gold_task'):U('PersonalService/gold_task');
        $gold_url = $utype==1?U('CompanyService/gold'):U('PersonalService/index');
        $this->assign('my_points',$uid?D('MembersPoints')->get_user_points($uid):'--');
        $this->assign('task_url',$task_url);
        $this->assign('gold_url',$gold_url);
        $this->_config_seo(array('title'=>'商城首页 - '.C('qscms_site_name'),'header_title'=>'商城首页'));
        $this->display();
    }

    /**
     * 商品详情
     */
    public function show()
    {
        $this->display();
    }

    /**
     * 商品列表
     */
    public function plist()
    {
        $cid = I('get.cid',0,'intval');
        $points_range_list = array('1-300'=>'1-300','300-1000'=>'300-1000','1000-5000'=>'1000-5000','5000-10000'=>'5000-10000');
        $this->assign('choose_category',D('Mall/MallCategory')->find($cid));
        $this->assign('points_range_list',$points_range_list);
        $this->display();
    }

    /**
     * 商品订单列表
     */
    public function order_list()
    {
        $status=I('get.status',0,'intval');
        $settr=I('get.settr',0,'intval');
        if($status>0){
            $where['status']=$status;
        }
        if($settr>0){
            $where['addtime']=array('gt',strtotime('-'.$settr.' day'));
        }
        $where['uid']=C('visitor.uid');
        $order = D('Mall/MallOrder')->get_order_list($where);
        $goods_id_arr = array();
        foreach ($order['list'] as $key => $value) {
            $goods_id_arr[] = $value['goods_id'];
        }
        $pic_arr = array();
        $goods_id_arr = array_unique($goods_id_arr);
        if($goods_id_arr){
            $goods_arr = D('Mall/MallGoods')->where(array('id'=>array('in',$goods_id_arr)))->select();
            foreach ($goods_arr as $key => $value) {
                $pic_arr[$value['id']] = $value['goods_img']?attach($value['goods_img'],'mall'):attach($value['goods_img'],'resource');
            }
        }
        $this->assign('pic_arr',$pic_arr);
        $this->assign('order',$order);
        $this->_config_seo(array('title'=>'兑换记录 - '.C('qscms_site_name'),'header_title'=>'兑换记录'));
        $this->display();
    }
    /**
     * 订单详情
     */
    public function order_show($id)
    {
        $order = D('Mall/MallOrder')->where(array('id'=>$id,'uid'=>C('visitor.uid')))->find();
        if(!$order){
            $this->_404('商品不存在！');
        }
        $goods_info = D('Mall/MallGoods')->find($order['goods_id']);
        $goods_info['goods_img'] = $goods_info['goods_img']?attach($goods_info['goods_img'],'mall'):attach($goods_info['goods_img'],'resource');
        $this->assign('goods_info',$goods_info);
        $this->assign('order',$order);
        $this->display();
    }

    /**
     * 生成订单
     */
    public function create_order($id)
    {
        if(!C('visitor')){
            $this->_404('请先登录！');
        }
        $goods_id = $id;
        $num = 1;
        !$goods_id && $this->_404('请选择商品！');

        if(C('visitor.utype') == 1){
            $contact_info = D('CompanyProfile')->where(array('uid'=>C('visitor.uid')))->find();
            $contact_info['contact'] = $contact_info['contact'];
            $contact_info['mobile'] = C('visitor.mobile');
            $contact_info['address'] = $contact_info['address'];
        }else{
            $contact_info = D('Resume')->where(array('uid'=>C('visitor.uid'),'def'=>1))->find();
            $contact_info['contact'] = $contact_info['fullname'];
            $contact_info['mobile'] = C('visitor.mobile');
            $contact_info['address'] = '';
        }
        
        $this->assign('contact_info',$contact_info);
        $this->display();
    }
    public function create_order_save(){
        if(IS_POST){
            $goods_info = D('MallGoods')->find($goods_id);
            if($goods_info['goods_stock']<$num){
                $this->ajaxReturn(0,'库存不足，请重新选择商品！');
            }
            $log_count = D('MallOrder')->where(array('goods_id'=>$goods_id,'uid'=>C('visitor.uid'),'status'=>array('neq',1)))->count();
            if($num+$log_count > $goods_info['goods_customer']){
                $this->ajaxReturn(0,'您已超过该商品的最大可兑换数量！');
            }
            $r = D('Mall/MallOrder')->order_add(C('visitor'),I('post.'));
            $this->ajaxReturn($r['state'],$r['msg']);
        }
    }
    /**
     * ajax检测是否可兑换
     */
    public function ajax_exchange_check(){
        if(!C('visitor')){
            $this->ajaxReturn(0,'请先登录！','',1);
        }
        $goods_id = I('get.goods_id',0,'intval');
        $num = 1;
        !$goods_id && $this->ajaxReturn(0,'请选择商品！');
        $goods_info = D('Mall/MallGoods')->find($goods_id);
        if($goods_info['goods_stock']<$num){
            $this->ajaxReturn(0,'库存不足，请重新选择商品！');
        }
        $log_count = D('Mall/MallOrder')->where(array('goods_id'=>$goods_id,'uid'=>C('visitor.uid')))->count();
        if($num+$log_count > $goods_info['goods_customer']){
            $this->ajaxReturn(0,'您已超过该商品的最大可兑换数量！');
        }
        $user_points = D('MembersPoints')->get_user_points(C('visitor.uid'));
        $total_points = $num*$goods_info['goods_points'];
        if($total_points>$user_points){
            $this->ajaxReturn(0,C('qscms_points_byname').'不足，无法兑换！');
        }
        $this->ajaxReturn(1,'获取数据成功',array('url'=>U('create_order',array('id'=>$goods_id))));
    }
}

?>