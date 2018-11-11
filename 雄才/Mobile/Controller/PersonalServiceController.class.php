<?php
namespace Mobile\Controller;
use Mobile\Controller\PersonalController;
class PersonalServiceController extends PersonalController{
    public $uid;
    public $my_points;
    public $increment_arr;
    public $timestamp;
    public function _initialize(){
        parent::_initialize();
        //访问者控制
        if (!$this->visitor->is_login && IS_AJAX) $this->ajaxReturn(0, L('login_please'),'',1);
        if(C('visitor.utype') !=2 && IS_AJAX) $this->ajaxReturn(0,'请登录个人账号！');
        $this->uid = C('visitor.uid');
        $this->my_points = D('MembersPoints')->get_user_points($this->uid);
        $this->timestamp=time();
    }
    /**
    * 我的积分
    */
    public function index(){
        $issign = D('MembersHandsel')->check_members_handsel_day(array('uid'=>$this->uid,'htype'=>'task_sign'));
        $this->assign('issign',$issign ? 1 : 0);
        $this->assign('my_points',$this->my_points);
        $this->_config_seo(array('title'=>'我的'.C('qscms_points_byname').' - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'我的'.C('qscms_points_byname')));
        $this->display('Personal/service/index');
    }
    /**
     * 积分明细
     */
    public function gold_log(){
        $where['uid']=$this->uid;
        $operate = I('get.operate',1,'intval');
        $where['operate']=$operate;
        $list = D('MembersHandsel')->get_handsel_list($where);
        $get_num = D('MembersHandsel')->where(array('uid'=>$this->uid,'operate'=>1))->sum('points');
        $use_num = D('MembersHandsel')->where(array('uid'=>$this->uid,'operate'=>2))->sum('points');
        $this->assign('operate',$operate);
        $this->assign('list',$list);
        $this->assign('get_num',intval($get_num));
        $this->assign('use_num',intval($use_num));
        $this->_config_seo(array('title'=>C('qscms_points_byname').'收支明细 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>C('qscms_points_byname').'收支明细'));
        $this->display('Personal/service/gold_log');
    }
    /**
     * 做任务
     */
    public function gold_task(){
        $resume = D('Resume')->Where(array('uid'=>$this->uid))->field('id')->find();
        $this->assign('task_url',D('Task')->task_url_mobile($resume['id'],C('visitor.utype')));
        $this->assign('done_task',D('TaskLog')->get_done_task($this->uid,C('visitor.utype')));
        $this->assign('task',D('Task')->get_task_cache(2));
        $this->_config_seo(array('title'=>'做任务 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'做任务'));
        $this->display('Personal/service/gold_task');
    }
    /**
     * 简历置顶
     */
    public function service_stick(){
        if(IS_POST){
            $payment_type_arr = array('points','wxpay','alipay');
            $params['payment'] = I('post.payment','points','trim');
            if(!in_array($params['payment'],$payment_type_arr)){
                $this->ajaxReturn(0,'支付方式错误！');
            }
            $resume_id = I('post.resume_id',0,'intval');
            if(!$resume_id){
                $this->ajaxReturn(0,'请选择简历！');
            }
            $promotion_field = D('Resume')->where(array('id'=>$resume_id))->find();
            if(!$promotion_field)
            {
                $this->ajaxReturn(0,'简历不存在！');
            }
            if($promotion_field['stick']==1)
            {
                $this->ajaxReturn(0,'该简历已置顶！');
            }
            $service_id = I('post.service_id',0,'intval');
            $service_info = D('PersonalServiceStick')->find($service_id);
            if(!$service_info){
                $this->ajaxReturn(0,'请选择服务！');
            }
            //换算人民币
            $params['amount'] = round($service_info['points']/C('qscms_payment_rate'),2);
            $params['deductible'] = I('post.deductible_val',0,'intval');
            if($service_info['points']==$params['deductible']){
                $params['payment'] = 'points';
            }
            if($params['payment'] == 'points' && ($this->my_points<$service_info['points'] || $this->my_points<$params['deductible']))
            {
                $this->ajaxReturn(0,C('qscms_points_byname').'不足，请使用其他方式支付！');
            }
            $params['order_type'] = 3;
            $params['increment_name'] = '简历置顶'.$service_info['days'].'天';
            $params['is_deductible'] = $params['deductible']>0?1:0;
            $params_array['days'] = $service_info['days'];
            $params_array['resume_id'] = $resume_id;
            $params_array['points'] = $service_info['points'];
            $params['params'] = serialize($params_array);
            $return_order_info = $this->_order_insert($params);
            if(!$return_order_info){
                $this->ajaxReturn(0,'下单错误！请重新提交订单！');
            }else if($return_order_info['pay_type']==1){
                D('Order')->where(array('id'=>$return_order_info['id']))->save(array('is_paid'=>2,'payment_time'=>$this->timestamp));
                $setsqlarr['points']=$service_info['points'];
                $setsqlarr['resume_id']=$resume_id;
                $setsqlarr['days']=$service_info['days'];
                $setsqlarr['resume_uid'] = $this->uid;
                $setsqlarr['endtime'] = strtotime("+{$setsqlarr['days']} day");
                $rst = D('PersonalServiceStickLog')->add_stick_log($setsqlarr);
                if($rst['state']==1){
                    $refreshtime = D('Resume')->where(array('id'=>$resume_id))->getfield('refreshtime');
                    $stime = intval($refreshtime) + 100000000;
                    D('Resume')->where(array('id'=>$resume_id))->save(array('stick'=>1,'stime'=>$stime));
                    D('ResumeSearchPrecise')->where(array('id'=>$resume_id))->setField('stime',$stime);
                    D('ResumeSearchFull')->where(array('id'=>$resume_id))->setField('stime',$stime);
                    /* 会员日志 */
                    write_members_log(C('visitor'),'order','创建增值服务订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));

                    $p_rst = D('MembersPoints')->report_deal($this->uid,2,$return_order_info['pay_points']);
                    if($p_rst){
                        /* 会员日志 */
                        write_members_log(C('visitor'),'order','支付订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));
                        write_members_log(C('visitor'),'increment','开通增值服务【'.$params['increment_name'].'】，支付方式：'.C('qscms_points_byname').'兑换');
                        $handsel['uid'] = $this->uid;
                        $handsel['htype'] = '';
                        $handsel['htype_cn'] = $params['increment_name'];
                        $handsel['operate'] = 2;
                        $handsel['points'] = $return_order_info['pay_points'];
                        $handsel['addtime'] = $this->timestamp;
                        D('MembersHandsel')->members_handsel_add($handsel);
                    }
                }
                $this->ajaxReturn(1,'成功兑换服务',U('order_list'));
            }else{
                $this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
            }
        }else{
            $resume_id = I('get.resume_id',0,'intval');
            $resume_where['uid'] = $this->uid;
            $resume_list = D('Resume')->get_resume_list(array('where'=>$resume_where));
            if($resume_id){
                $resume_info = D('Resume')->find($resume_id);
                $resume_buy = $resume_info['stick']?1:0;
            }else{
                $resume_buy = 0;
            }
            $increment_arr = D('PersonalServiceStick')->select();
            foreach ($increment_arr as $key => $value) {
                $increment_arr[$key]['service_price'] = floatval($value['points']/C('qscms_payment_rate'));
                $increment_arr[$key]['service_points'] = $value['points'];
                $increment_arr[$key]['name'] = '置顶'.$value['days'].'天';
            }
            $this->assign('resume_id',$resume_id);
            $this->assign('resume_buy',$resume_buy);
            $this->assign('resume_list',$resume_list);
            $this->assign('payment_rate',C('qscms_payment_rate'));
            $this->assign('my_points',$this->my_points);
            $this->assign('increment_arr',$increment_arr);
            $this->_config_seo(array('title'=>'简历置顶 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'简历置顶'));
            $this->display('Personal/service/service_stick');
        }
    }
    /**
     * 醒目标签
     */
    public function service_tag(){
        if(IS_POST){
            $payment_type_arr = array('points','wxpay','alipay');
            $params['payment'] = I('post.payment','points','trim');
            if(!in_array($params['payment'],$payment_type_arr)){
                $this->ajaxReturn(0,'支付方式错误！');
            }
            $resume_id = I('post.resume_id',0,'intval');
            if(!$resume_id){
                $this->ajaxReturn(0,'请选择简历！');
            }
            $promotion_field = D('Resume')->where(array('id'=>$resume_id))->find();
            if(!$promotion_field)
            {
                $this->ajaxReturn(0,'简历不存在！');
            }
            if($promotion_field['strong_tag']>0)
            {
                $this->ajaxReturn(0,'该简历已设置醒目标签！');
            }
            $tag_id = I('post.tag_id',0,'intval');
            $tag_info = D('PersonalServiceTagCategory')->find($tag_id);
            if(!$tag_info){
                $this->ajaxReturn(0,'请选择标签！');
            }
            $service_id = I('post.service_id',0,'intval');
            $service_info = D('PersonalServiceTag')->find($service_id);
            if(!$service_info){
                $this->ajaxReturn(0,'请选择天数！');
            }
            //换算人民币
            $params['amount'] = round($service_info['points']/C('qscms_payment_rate'),2);
            $params['deductible'] = I('post.deductible_val',0,'intval');
            if($service_info['points']==$params['deductible']){
                $params['payment'] = 'points';
            }
            if($params['payment'] == 'points' && ($this->my_points<$service_info['points'] || $this->my_points<$params['deductible']))
            {
                $this->ajaxReturn(0,C('qscms_points_byname').'不足，请使用其他方式支付！');
            }
            $params['order_type'] = 4;
            $params['increment_name'] = $tag_info['name'];
            $params['is_deductible'] = $params['deductible']>0?1:0;
            $params_array['days'] = $service_info['days'];
            $params_array['resume_id'] = $resume_id;
            $params_array['tag_id'] = $tag_id;
            $params_array['points'] = $service_info['points'];
            $params['params'] = serialize($params_array);
            $return_order_info = $this->_order_insert($params);
            if(!$return_order_info){
                $this->ajaxReturn(0,'下单错误！请重新提交订单！');
            }else if($return_order_info['pay_type']==1){
                D('Order')->where(array('id'=>$return_order_info['id']))->save(array('is_paid'=>2,'payment_time'=>$this->timestamp));
                $setsqlarr['points']=$service_info['points'];
                $setsqlarr['resume_id']=$resume_id;
                $setsqlarr['tag_id'] = $tag_id;
                $setsqlarr['days']=$service_info['days'];
                $setsqlarr['resume_uid'] = $this->uid;
                $setsqlarr['endtime'] = strtotime("+{$setsqlarr['days']} day");
                $rst = D('PersonalServiceTagLog')->add_tag_log($setsqlarr);
                if($rst['state']==1){
                    D('Resume')->where(array('id'=>array('eq',$setsqlarr['resume_id'])))->setField('strong_tag',$tag_id);
                    /* 会员日志 */
                    write_members_log(C('visitor'),'order','创建增值服务订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));

                    $p_rst = D('MembersPoints')->report_deal($this->uid,2,$return_order_info['pay_points']);
                    if($p_rst){
                        /* 会员日志 */
                        write_members_log(C('visitor'),'order','支付订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));
                        write_members_log(C('visitor'),'increment','开通增值服务【'.$params['increment_name'].'】，支付方式：'.C('qscms_points_byname').'兑换');
                        $handsel['uid'] = $this->uid;
                        $handsel['htype'] = '';
                        $handsel['htype_cn'] = $params['increment_name'];
                        $handsel['operate'] = 2;
                        $handsel['points'] = $return_order_info['pay_points'];
                        $handsel['addtime'] = $this->timestamp;
                        D('MembersHandsel')->members_handsel_add($handsel);
                    }
                }
                $this->ajaxReturn(1,'成功兑换服务',U('order_list'));
            }else{
                $this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
            }
        }else{
            $resume_id = I('get.resume_id',0,'intval');
            $resume_where['uid'] = $this->uid;
            $resume_list = D('Resume')->get_resume_list(array('where'=>$resume_where));
            if($resume_id){
                $resume_info = D('Resume')->find($resume_id);
                $resume_buy = $resume_info['strong_tag']?1:0;
            }else{
                $resume_buy = 0;
            }
            $increment_arr = D('PersonalServiceTag')->select();
            foreach ($increment_arr as $key => $value) {
                $increment_arr[$key]['service_price'] = floatval($value['points']/C('qscms_payment_rate'));
                $increment_arr[$key]['service_points'] = $value['points'];
                $increment_arr[$key]['name'] = $value['days'].'天';
            }
            $tag_arr = D('PersonalServiceTagCategory')->select();
            $this->assign('resume_id',$resume_id);
            $this->assign('resume_buy',$resume_buy);
            $this->assign('resume_list',$resume_list);
            $this->assign('payment_rate',C('qscms_payment_rate'));
            $this->assign('my_points',$this->my_points);
            $this->assign('increment_arr',$increment_arr);
            $this->assign('tag_arr',$tag_arr);
            $this->_config_seo(array('title'=>'醒目标签 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'醒目标签'));
            $this->display('Personal/service/service_tag');
        }
    }
    /**
     *  插入订单数据表数据
     *  $params['order_type']
     *  $params['amount']
     *  $params['deductible']
     *  $params['increment_name']
     *  $params['payment_name']
     *  $params['description']
     *  $params['points']
     *  $params['stemeal']
     *  $params['params']
     *  $params['discount']
     *  $params['is_deductible']
     */
    protected function _order_insert($params = array()){
        if(empty($params)){
            $this->_404('参数错误！');
        }
        if($params['payment']=='points'){
            $params['is_deductible'] = 1;
            $params['deductible'] = intval($params['amount']*C('qscms_payment_rate'));
            $paymenttpye['typename'] = 'points';
            $paymenttpye['byname'] = C('qscms_points_byname').'支付';
        }else{
            $paymenttpye=D('Payment')->get_payment_info($params['payment']);
        }
        if($params['is_deductible']==0){
            $params['deductible'] = 0;
        }
        if($params['deductible']>0){
            $params['pay_amount'] = $params['amount']-floatval($params['deductible']/C('qscms_payment_rate'));
        }else{
            $params['pay_amount'] = $params['amount'];
        }
        
        $oid = strtoupper(substr($paymenttpye['typename'],0,1))."-".date('ymd',$this->timestamp)."-".date('His',$this->timestamp);
        if($params['description']==''){
            $params['description'] = '购买服务：'.$params['increment_name'];
        }
        if($params['is_deductible']){
            $params['description'] .= ';'.C('qscms_points_byname').'支付'.$params['deductible'].C('qscms_points_quantifier');
        }
        if($params['pay_amount']>0){
            $params['description'] .= ';'.$paymenttpye['byname'].$params['pay_amount'].'元';
        }
        $insert_id = D('Order')->add_order(C('visitor'),$oid,$params['order_type'],$params['amount'],$params['pay_amount'],$params['deductible'],$params['increment_name'],$params['payment'],$paymenttpye['byname'],$params['description'],$this->timestamp,1,$params['points'],$params['stemeal'],0,$params['params'],$params['discount']);
        write_members_log(C('visitor'),'order','创建订单【'.$params['increment_name'].'】（订单号：'.$oid.'），支付方式：'.$params['payment'],false,array('order_id'=>$insert_id));
        return D('Order')->find($insert_id);
    }
    /**
     * 订单列表
     */
    public function order_list(){
        $type = I('get.type',0,'intval');
        $where['utype']=array('eq',2);
        $type && $where['order_type'] = array('eq',$type);   
        $is_paid=I('get.is_paid',0,'intval');
        if($is_paid>0){
            $where['is_paid']=$is_paid;
        }
        $where['uid']=$this->uid;
        $order = D('Order')->get_order_list($where);
        $order_type_choose = array(3=>'简历置顶',4=>'醒目标签');
        $pay_status_choose = array(1=>'未支付',2=>'已完成',3=>'已取消');
        $this->assign('order_type_choose',$order_type_choose);
        $this->assign('pay_status_choose',$pay_status_choose);
        $this->assign('order',$order);
        $this->assign('type',$type);
        $this->assign('order_type',D('Order')->order_type);
        $this->assign('is_paid',$is_paid);
        $this->_config_seo(array('title'=>'我的订单 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'我的订单'));
        $this->display('Personal/service/order_list');
    }
    /**
     * 订单详情
     */
    public function order_detail($order_id){
        $order = D('Order')->where(array('uid'=>$this->uid,'id'=>$order_id))->find();
        if(!$order){
            $this->_404('订单不存在！');
        }
        $order['params'] = $order['params']?unserialize($order['params']):array();
        if(!empty($order['params'])){
            $resume_info = D('Resume')->where(array('uid'=>$order['uid'],'id'=>$order['params']['resume_id']))->find();
            if($order['params']['tag_id']){
                $tag_info = D('PersonalServiceTagCategory')->find($order['params']['tag_id']);
            }else{
                $tag_info = false;
            }
        }else{
            $resume_info = false;
            $tag_info = false;
        }
        //抵扣的积分折算成金额
        $order['discount_money'] = floatval(intval($order['pay_points'])/C('qscms_payment_rate'));

        if($this->my_points<$order['pay_points']){
            $points_enough = 0;
        }else{
            $points_enough = 1;
        }
        if($order['payment']=='wxpay' && $order['is_paid']==1 && $points_enough){
            /**
             * 生成支付所需的参数
             */
            $paysetarr = $this->_get_pay_params($order);
            if($this->is_weixin){
                $r = D('Payment')->pay($paysetarr);
                $this->assign('jsApiParameters',$r['jsApiParameters']);
            }else{
                $paysetarr['h5_pay'] = 1;
                $r = D('Payment')->pay($paysetarr);
                if($r['status']==1){
                    $this->assign('h5pay_error','');
                    $this->assign('pay_url',$r['url']);
                }else{
                    $this->assign('h5pay_error',$r['msg']);
                }
            }
        }else{
            $this->assign('jsApiParameters','');
        }
        
        if($tag_info){
            $this->assign('tag_info',$tag_info);
        }
        $this->assign('points_enough',$points_enough);
        $this->assign('order',$order);
        $this->assign('resume_info',$resume_info);
        $this->assign('my_setmeal',$this->my_setmeal);
        $this->assign('order_type',D('Order')->order_type);
        $this->_config_seo(array('title'=>'订单详情 - 个人会员中心 - '.C('qscms_site_name'),'header_title'=>'订单详情'));
        $this->display('Personal/service/order_detail');
    }
    /**
     * 支付宝支付跳转
     */
    public function alipay_submit(){
        $order_id = I('post.order_id',0,'intval');
        !$order_id && $this->_404('参数错误！');
        $order = D('Order')->where(array('uid'=>$this->uid,'id'=>$order_id,'is_paid'=>1))->find();
        !$order && $this->_404('订单不存在！');
        if($this->my_points<$order['pay_points']){
            $this->_404('你的'.C('qscms_points_byname').'不足，无法完成支付，请重新下单！');
        }
        $paysetarr = $this->_get_pay_params($order);
        D('Payment')->pay($paysetarr);
    }
    /**
     * 取消订单
     */
    public function order_cancel($order_id){
        $order = D('Order')->where(array('uid'=>$this->uid,'id'=>$order_id))->find();
        if(!$order){
            $this->_404('订单不存在！');
        }
        D('Order')->where(array('uid'=>$this->uid,'id'=>$order_id))->setField('is_paid',3);
        $this->redirect('order_list');
    }
    /**
     * 获取支付相关参数
     */
    protected function _get_pay_params($order){
        $paysetarr['payFrom'] = 'wap';
        $paysetarr['type'] = $order['payment'];
        $paysetarr['ordsubject'] = $order['service_name'];
        $paysetarr['ordbody'] = $order['service_name'];
        $paysetarr['ordtotal_fee'] = $order['pay_amount'];
        $paysetarr['oid'] = $order['oid'];
        return $paysetarr;
    }
    /**
     * 订单详情页获取原价
     */
    protected function _get_old_price($order){
        $arr = array(6,7,8,9,12);
        if(in_array($order['order_type'], $arr)){
            $increment = D('SetmealIncrement')->find($order['setmeal']);
            return $increment['price'];
        }else{
            return false;
        }
    }
}
?>