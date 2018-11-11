<?php
namespace Mobile\Controller;
use Mobile\Controller\CompanyController;
class CompanyServiceController extends CompanyController{
	public $uid;
	public $my_setmeal;
	public $my_points;
	public $increment_arr;
	public $timestamp;
	public function _initialize(){
        parent::_initialize();
        //访问者控制
        if (!$this->visitor->is_login && IS_AJAX) $this->ajaxReturn(0, L('login_please'),'',1);
        if(C('visitor.utype') !=1 && IS_AJAX) $this->ajaxReturn(0,'请登录企业账号！');
        $this->uid = C('visitor.uid');
        $this->my_setmeal = D('MembersSetmeal')->get_user_setmeal($this->uid);
        $this->my_points = D('MembersPoints')->get_user_points($this->uid);
        $this->timestamp=time();
        $setmeal_increment_pay_points_rule = C('qscms_setmeal_increment_pay_points_rule');
        $this->assign('setmeal_increment_pay_points_rule',$setmeal_increment_pay_points_rule);
    } 
    /**
     * 我的套餐
     */
    public function index(){
		$total[0]=M('Jobs')->where(array('uid'=>C('visitor.uid')))->count();
        $total[1]=M('JobsTmp')->where(array('uid'=>C('visitor.uid'),'display'=>array('neq',2)))->count();
        $this->my_setmeal['surplus_jobs'] = $this->my_setmeal['jobs_meanwhile'] - $total[0] - $total[1];
		$this->assign('my_setmeal',$this->my_setmeal);
		$this->assign('my_userinfo',D('Members')->get_user_one(array('uid'=>$this->uid)));
		$this->_config_seo(array('title'=>'我的套餐 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'我的套餐'));
    	$this->display('Company/service/index');
    }
    /**
     * 套餐明细
     */
    public function setmeal_detail(){
    	$where['log_utype'] = 1;
		$where['log_uid'] = $this->uid;
    	$log = D('MembersSetmealLog')->get_members_setmeal_log($where);
    	$this->assign('log',$log);
		$this->_config_seo(array('title'=>'套餐使用明细 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'套餐使用明细'));
		$this->display('Company/service/setmeal_detail');
    }
    /**
     * 我的积分
     */
    public function gold(){
        $issign = D('MembersHandsel')->check_members_handsel_day(array('uid'=>$this->uid,'htype'=>'task_sign'));
        $this->assign('issign',$issign ? 1 : 0);
		$this->assign('my_points',$this->my_points);
		$this->_config_seo(array('title'=>'我的'.C('qscms_points_byname').' - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'我的'.C('qscms_points_byname')));
    	$this->display('Company/service/gold');
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
    	$this->_config_seo(array('title'=>C('qscms_points_byname').'使用明细 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>C('qscms_points_byname').'使用明细'));
    	$this->display('Company/service/gold_log');
    }
    /**
     * 做任务
     */
    public function gold_task(){
        $this->assign('task_url',D('Task')->task_url_mobile(C('visitor.utype')));
		$this->assign('done_task',D('TaskLog')->get_done_task($this->uid,C('visitor.utype')));
        $this->assign('task',D('Task')->get_task_cache(1));
    	$this->_config_seo(array('title'=>'做任务 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'做任务'));
    	$this->display('Company/service/gold_task');
    }
    /**
     * 订单列表
     */
    public function order_list(){
    	$type = I('get.type','','trim,badword');
    	switch($type){
    		case 'setmeal':
    			$where['order_type']=array('eq',1);
    			break;
    		case 'increment':
    			$where['order_type']=array(array('eq',6),array('eq',7),array('eq',8),array('eq',9),array('eq',10),array('eq',11),array('eq',12),'or');
    			break;
    		case 'points':
    			$where['order_type']=array('eq',2);
    			break;
    	}
    	$is_paid=I('get.is_paid',0,'intval');
    	if($is_paid>0){
    		$where['is_paid']=$is_paid;
    	}
    	$where['uid']=$this->uid;
    	$order = D('Order')->get_order_list($where);
    	$order_type_choose = array('setmeal'=>'套餐订单','increment'=>'增值服务订单','points'=>C('qscms_points_byname').'订单');
    	if(!C('qscms_enable_com_buy_points')){
    		unset($order_type_choose['points']);
    	}
    	$pay_status_choose = array(1=>'未支付',2=>'已完成',3=>'已取消');
    	$this->assign('order_type_choose',$order_type_choose);
    	$this->assign('pay_status_choose',$pay_status_choose);
    	$this->assign('order',$order);
    	$this->assign('type',$type);
    	$this->assign('order_type',D('Order')->order_type);
    	$this->assign('is_paid',$is_paid);
    	$this->_config_seo(array('title'=>'我的订单 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'我的订单'));
    	$this->display('Company/service/order_list');
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
    	$order['old_price'] = $this->_get_old_price($order);
    	$order['old_price'] = $order['old_price']==false?$order['amount']:$order['old_price'];
    	//抵扣的积分折算成金额
    	$order['discount_money'] = floatval(intval($order['pay_points'])/C('qscms_payment_rate'));
    	//发票信息
    	$invoice = D('OrderInvoice')->where(array('order_id'=>$order['id']))->find();

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
        $contact = M('CompanyProfile')->field('companyname,contact,telephone,landline_tel,address')->where(array('uid'=>$order['uid']))->find();
        $this->assign('contact',$contact);
        $this->assign('points_enough',$points_enough);
    	$this->assign('invoice',$invoice);
    	$this->assign('order',$order);
    	$this->assign('my_setmeal',$this->my_setmeal);
    	$this->assign('order_type',D('Order')->order_type);
    	$this->_config_seo(array('title'=>'订单详情 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'订单详情'));
    	$this->display('Company/service/order_detail');
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
     * 索取发票保存
     */
    public function invoice_save(){
    	$data = I('post.');
    	$data['uid'] = $this->uid;
    	$result = D('OrderInvoice')->addone($data,C('visitor'));
    	$this->ajaxReturn($result['state'],$result['error'],$result['data']);
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
    /**
     * 增值服务首页
     */
    public function service(){
    	$model = D('Setmeal');
		//计算各种增值服务的最大折扣
		$return_discount[0] = $model->get_max_discount('download_resume');
		$return_discount[1] = $model->get_max_discount('sms');
		$return_discount[2] = $model->get_max_discount('stick');
		$return_discount[3] = $model->get_max_discount('emergency');
		$return_discount[4] = $model->get_max_discount('tpl');
		$return_discount[5] = $model->get_max_discount('auto_refresh_jobs');
		$this->assign('return_discount',$return_discount);
    	$this->_config_seo(array('title'=>'增值服务 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'增值服务'));
    	$this->display('Company/service/service');
    }
    /**
     * 增值服务 - 购买简历包
     */
    public function service_resume(){
    	if(IS_POST){
    		$payment_type_arr = array('points','wxpay','alipay');
			$params['payment'] = I('post.payment','points','trim');
			if(!in_array($params['payment'],$payment_type_arr)){
				$this->ajaxReturn(0,'支付方式错误！');
			}
			$service_id = I('post.service_id',0,'intval');
			$service_info = D('SetmealIncrement')->get_cache('',$service_id);
			if(!$service_info){
				$this->ajaxReturn(0,'请选择服务！');
			}
            if(C('qscms_mobile_setmeal_increment_discount_value')>0){
                $service_info['price'] = C('qscms_mobile_setmeal_increment_discount_type')==1?$service_info['price']/100*C('qscms_mobile_setmeal_increment_discount_value'):$service_info['price']-C('qscms_mobile_setmeal_increment_discount_value');
                $service_info['price'] = $service_info['price']<0?0:$service_info['price'];
            }
			$my_discount = D('Setmeal')->get_increment_discount_by_array($service_info['cat'],$this->my_setmeal);
			//当前会员需要付的价格
			$new_price = $my_discount>0?round($service_info['price']*$my_discount/10,1):$service_info['price'];
			$params['amount'] = $new_price;
			$params['deductible'] = I('post.deductible_val',0,'intval');
			//换算积分
			$price_to_points = round($new_price*C('qscms_payment_rate'));
            $price_to_points = $price_to_points<=1?1:$price_to_points;
			if($price_to_points==$params['deductible']){
				$params['payment'] = 'points';
			}
			if(($params['payment'] == 'points' && $this->my_points<$price_to_points) || $this->my_points<$params['deductible'])
            {
            	$this->ajaxReturn(0,C('qscms_points_byname').'不足，请使用其他方式支付！');
            }
			$params['order_type'] = 6;
			$params['increment_name'] = $service_info['name'];
			$params['stemeal'] = $service_id;
			$params['discount'] = '专享'.$my_discount.'折优惠';
			$params['is_deductible'] = $params['deductible']>0?1:0;
			$return_order_info = $this->_order_insert($params);
			if(!$return_order_info){
				$this->ajaxReturn(0,'下单错误！请重新提交订单！');
			}else if($return_order_info['pay_type']==1){
				D('Order')->where(array('id'=>$return_order_info['id']))->save(array('is_paid'=>2,'payment_time'=>$this->timestamp));
				if($return_order_info['pay_points']>0){
					$p_rst = D('MembersPoints')->report_deal($this->uid,2,$return_order_info['pay_points']);
					if($p_rst){
		                $handsel['uid'] = C('visitor.uid');
		                $handsel['htype'] = '';
		                $handsel['htype_cn'] = $return_order_info['service_name'];
		                $handsel['operate'] = 2;
		                $handsel['points'] = $return_order_info['pay_points'];
		                $handsel['addtime'] = $this->timestamp;
		                D('MembersHandsel')->members_handsel_add($handsel);
		            }
				}
				D('MembersSetmeal')->where(array('uid'=>$this->uid))->setInc('download_resume',$service_info['value']);
				/* 会员日志 */
		        if($params['deductible']>0){
		        	$log_payment = $return_order_info['payment_cn'].'+'.C('qscms_points_byname').'抵扣';
		        }else{
		        	$log_payment = $return_order_info['payment_cn'];
		        }
                write_members_log(C('visitor'),'order','支付订单（订单号：'.$return_order_info['oid'].'），支付方式：'.$log_payment,false,array('order_id'=>$return_order_info['id']));
                write_members_log(C('visitor'),'increment','开通增值服务【'.$return_order_info['service_name'].'】，支付方式：'.$log_payment);
				$this->ajaxReturn(1,'成功兑换服务',U('order_list'));
			}else{
				$this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
			}
    	}else{
        	$this->assign('payment_rate',C('qscms_payment_rate'));
    		$this->assign('my_points',$this->my_points);
			$this->assign('my_setmeal',$this->my_setmeal);
			$this->assign('increment_arr',$this->_get_service_list('download_resume'));
	    	$this->_config_seo(array('title'=>'购买简历包 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'购买简历包'));
	    	$this->display('Company/service/service_resume');
    	}
    }
    /**
     * 增值服务 - 购买短信包
     */
    public function service_sms(){
    	if(IS_POST){
    		$payment_type_arr = array('points','wxpay','alipay');
			$params['payment'] = I('post.payment','points','trim');
			if(!in_array($params['payment'],$payment_type_arr)){
				$this->ajaxReturn(0,'支付方式错误！');
			}
			$service_id = I('post.service_id',0,'intval');
			$service_info = D('SetmealIncrement')->get_cache('',$service_id);
			if(!$service_info){
				$this->ajaxReturn(0,'请选择服务！');
			}
            if(C('qscms_mobile_setmeal_increment_discount_value')>0){
                $service_info['price'] = C('qscms_mobile_setmeal_increment_discount_type')==1?$service_info['price']/100*C('qscms_mobile_setmeal_increment_discount_value'):$service_info['price']-C('qscms_mobile_setmeal_increment_discount_value');
                $service_info['price'] = $service_info['price']<0?0:$service_info['price'];
            }
			$my_discount = D('Setmeal')->get_increment_discount_by_array($service_info['cat'],$this->my_setmeal);
			//当前会员需要付的价格
			$new_price = $my_discount>0?round($service_info['price']*$my_discount/10,1):$service_info['price'];
			$params['amount'] = $new_price;
			$params['deductible'] = I('post.deductible_val',0,'intval');
			//换算积分
			$price_to_points = round($new_price*C('qscms_payment_rate'));
            $price_to_points = $price_to_points<=1?1:$price_to_points;
			if($price_to_points==$params['deductible']){
				$params['payment'] = 'points';
			}
			if(($params['payment'] == 'points' && $this->my_points<$price_to_points) || $this->my_points<$params['deductible'])
            {
            	$this->ajaxReturn(0,C('qscms_points_byname').'不足，请使用其他方式支付！');
            }
			$params['order_type'] = 7;
			$params['increment_name'] = $service_info['name'];
			$params['stemeal'] = $service_id;
			$params['discount'] = '专享'.$my_discount.'折优惠';
			$params['is_deductible'] = $params['deductible']>0?1:0;
			$return_order_info = $this->_order_insert($params);
			if(!$return_order_info){
				$this->ajaxReturn(0,'下单错误！请重新提交订单！');
			}else if($return_order_info['pay_type']==1){
				D('Order')->where(array('id'=>$return_order_info['id']))->save(array('is_paid'=>2,'payment_time'=>$this->timestamp));
				if($return_order_info['pay_points']>0){
					$p_rst = D('MembersPoints')->report_deal($this->uid,2,$return_order_info['pay_points']);
					if($p_rst){
		                $handsel['uid'] = C('visitor.uid');
		                $handsel['htype'] = '';
		                $handsel['htype_cn'] = $return_order_info['service_name'];
		                $handsel['operate'] = 2;
		                $handsel['points'] = $return_order_info['pay_points'];
		                $handsel['addtime'] = $this->timestamp;
		                D('MembersHandsel')->members_handsel_add($handsel);
		            }
				}
				D('Members')->where(array('uid'=>$this->uid))->setInc('sms_num',$service_info['value']);
				/* 会员日志 */
		        if($params['deductible']>0){
		        	$log_payment = $return_order_info['payment_cn'].'+'.C('qscms_points_byname').'抵扣';
		        }else{
		        	$log_payment = $return_order_info['payment_cn'];
		        }
                write_members_log(C('visitor'),'order','支付订单（订单号：'.$return_order_info['oid'].'），支付方式：'.$log_payment,false,array('order_id'=>$return_order_info['id']));
				write_members_log(C('visitor'),'increment','开通增值服务【'.$return_order_info['service_name'].'】，支付方式：'.$log_payment);
				$this->ajaxReturn(1,'成功兑换服务',U('order_list'));
			}else{
				$this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
			}
    	}else{
        	$this->assign('payment_rate',C('qscms_payment_rate'));
    		$this->assign('my_points',$this->my_points);
			$this->assign('my_setmeal',$this->my_setmeal);
			$this->assign('increment_arr',$this->_get_service_list('sms'));
	    	$this->_config_seo(array('title'=>'购买短信包 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'购买短信包'));
	    	$this->display('Company/service/service_sms');
    	}
    }
    /**
     * 增值服务 - 职位置顶
     */
    public function service_stick(){
    	if(IS_POST){
    		$payment_type_arr = array('points','wxpay','alipay');
			$params['payment'] = I('post.payment','points','trim');
			if(!in_array($params['payment'],$payment_type_arr)){
				$this->ajaxReturn(0,'支付方式错误！');
			}
			$jobs_id = I('post.jobs_id',0,'intval');
			if(!$jobs_id){
				$this->ajaxReturn(0,'请选择职位！');
			}
			$promotion_field = D('Jobs')->where(array('id'=>$jobs_id))->find();
            if(!$promotion_field){
                $promotion_field = D('JobsTmp')->where(array('id'=>$jobs_id))->find();
            }
            if(!$promotion_field){
                $this->ajaxReturn(0,'职位不存在！');
            }
	        if($promotion_field['stick']==1)
	        {
	        	$this->ajaxReturn(0,'该职位已置顶！');
	        }
			$service_id = I('post.service_id',0,'intval');
			$service_info = D('SetmealIncrement')->get_cache('',$service_id);
			if(!$service_info){
				$this->ajaxReturn(0,'请选择服务！');
			}
            if(C('qscms_mobile_setmeal_increment_discount_value')>0){
                $service_info['price'] = C('qscms_mobile_setmeal_increment_discount_type')==1?$service_info['price']/100*C('qscms_mobile_setmeal_increment_discount_value'):$service_info['price']-C('qscms_mobile_setmeal_increment_discount_value');
                $service_info['price'] = $service_info['price']<0?0:$service_info['price'];
            }
			$my_discount = D('Setmeal')->get_increment_discount_by_array($service_info['cat'],$this->my_setmeal);
			//当前会员需要付的价格
			$new_price = $my_discount>0?round($service_info['price']*$my_discount/10,1):$service_info['price'];
			$params['amount'] = $new_price;
			$params['deductible'] = I('post.deductible_val',0,'intval');
			//换算积分
			$price_to_points = round($new_price*C('qscms_payment_rate'));
            $price_to_points = $price_to_points<=1?1:$price_to_points;
			if($price_to_points==$params['deductible']){
				$params['payment'] = 'points';
			}
			if(($params['payment'] == 'points' && $this->my_points<$price_to_points) || $this->my_points<$params['deductible'])
            {
            	$this->ajaxReturn(0,C('qscms_points_byname').'不足，请使用其他方式支付！');
            }
			$params['order_type'] = 8;
			$params['increment_name'] = $service_info['name'];
			$params['stemeal'] = $service_id;
			$params['discount'] = '专享'.$my_discount.'折优惠';
			$params['is_deductible'] = $params['deductible']>0?1:0;
			$params_array = array('days'=>$service_info['value']);
			$params_array['jobs_id'] = $jobs_id;
			$params['params'] = serialize($params_array);
			$return_order_info = $this->_order_insert($params);
			if(!$return_order_info){
				$this->ajaxReturn(0,'下单错误！请重新提交订单！');
			}else if($return_order_info['pay_type']==1){
				D('Order')->where(array('id'=>$return_order_info['id']))->save(array('is_paid'=>2,'payment_time'=>$this->timestamp));
				// 推广操作
				$promotionsqlarr['cp_uid']=$this->uid;
				$promotionsqlarr['cp_jobid']=$jobs_id;
				$promotionsqlarr['cp_ptype']=$service_info['cat'];
				$promotionsqlarr['cp_days']=$service_info['value'];
				$promotionsqlarr['cp_starttime']=$this->timestamp;
				$promotionsqlarr['cp_endtime']=strtotime("{$service_info['value']} day");
                $promotion_insert_id = D('Promotion')->add_promotion($promotionsqlarr);
                write_members_log(array('uid'=>$promotionsqlarr['cp_uid'],'utype'=>1,'username'=>''),'promotion','开通增值服务【置顶】',false,array('promotion_id'=>$promotion_insert_id));
				D('Promotion')->set_job_promotion($jobs_id,$service_info['cat']);
				/* 会员日志 */
                write_members_log(C('visitor'),'order','创建增值服务订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));
	            $p_rst = D('MembersPoints')->report_deal($this->uid,2,$return_order_info['pay_points']);
	            if($p_rst)
	            {
	            	/* 会员日志 */
                    write_members_log(C('visitor'),'order','支付订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));
                    write_members_log(C('visitor'),'increment','开通增值服务【'.$service_info['name'].'】，支付方式：'.C('qscms_points_byname').'兑换');
	                $handsel['uid'] = $this->uid;
	                $handsel['htype'] = '';
	                $handsel['htype_cn'] = '购买增值包:'.$service_info['name'];
	                $handsel['operate'] = 2;
	                $handsel['points'] = $return_order_info['pay_points'];
	                $handsel['addtime'] = $this->timestamp;
	                D('MembersHandsel')->members_handsel_add($handsel);
	            }
				$this->ajaxReturn(1,'成功兑换服务',U('order_list'));
			}else{
				$this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
			}
    	}else{
    		$jobs_id = I('get.jobs_id',0,'intval');
			$jobs_where['uid'] = $this->uid;
            C('qscms_jobs_display') == 1 && $jobs_where['audit'] = 1 ;
            $jobs_list = D('Jobs')->where($jobs_where)->select();
			if($jobs_id){
				$jobs_info = D('Jobs')->find($jobs_id);
				if(!$jobs_info){
					$jobs_info = D('JobsTmp')->find($jobs_id);
				}
				$jobs_buy = $jobs_info['stick']?1:0;
			}else{
				$jobs_buy = 0;
			}
			$this->assign('jobs_id',$jobs_id);
			$this->assign('jobs_buy',$jobs_buy);
			$this->assign('jobs_arr',$jobs_list);
        	$this->assign('payment_rate',C('qscms_payment_rate'));
    		$this->assign('my_points',$this->my_points);
			$this->assign('my_setmeal',$this->my_setmeal);
			$this->assign('increment_arr',$this->_get_service_list('stick'));
	    	$this->_config_seo(array('title'=>'职位置顶 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'职位置顶'));
	    	$this->display('Company/service/service_stick');
    	}
    }
    /**
     * 增值服务 - 职位紧急
     */
    public function service_emergency(){
    	if(IS_POST){
    		$payment_type_arr = array('points','wxpay','alipay');
			$params['payment'] = I('post.payment','points','trim');
			if(!in_array($params['payment'],$payment_type_arr)){
				$this->ajaxReturn(0,'支付方式错误！');
			}
			$jobs_id = I('post.jobs_id',0,'intval');
			if(!$jobs_id){
				$this->ajaxReturn(0,'请选择职位！');
			}
			$promotion_field = D('Jobs')->where(array('id'=>$jobs_id))->find();
            if(!$promotion_field){
                $promotion_field = D('JobsTmp')->where(array('id'=>$jobs_id))->find();
            }
            if(!$promotion_field){
                $this->ajaxReturn(0,'职位不存在！');
            }
	        if($promotion_field['emergency']==1)
	        {
	        	$this->ajaxReturn(0,'该职位已紧急！');
	        }
			$service_id = I('post.service_id',0,'intval');
			$service_info = D('SetmealIncrement')->get_cache('',$service_id);
			if(!$service_info){
				$this->ajaxReturn(0,'请选择服务！');
			}
            if(C('qscms_mobile_setmeal_increment_discount_value')>0){
                $service_info['price'] = C('qscms_mobile_setmeal_increment_discount_type')==1?$service_info['price']/100*C('qscms_mobile_setmeal_increment_discount_value'):$service_info['price']-C('qscms_mobile_setmeal_increment_discount_value');
                $service_info['price'] = $service_info['price']<0?0:$service_info['price'];
            }
			$my_discount = D('Setmeal')->get_increment_discount_by_array($service_info['cat'],$this->my_setmeal);
			//当前会员需要付的价格
			$new_price = $my_discount>0?round($service_info['price']*$my_discount/10,1):$service_info['price'];
			$params['amount'] = $new_price;
			$params['deductible'] = I('post.deductible_val',0,'intval');
			//换算积分
			$price_to_points = round($new_price*C('qscms_payment_rate'));
            $price_to_points = $price_to_points<=1?1:$price_to_points;
			if($price_to_points==$params['deductible']){
				$params['payment'] = 'points';
			}
			if(($params['payment'] == 'points' && $this->my_points<$price_to_points) || $this->my_points<$params['deductible'])
            {
            	$this->ajaxReturn(0,C('qscms_points_byname').'不足，请使用其他方式支付！');
            }
			$params['order_type'] = 9;
			$params['increment_name'] = $service_info['name'];
			$params['stemeal'] = $service_id;
			$params['discount'] = '专享'.$my_discount.'折优惠';
			$params['is_deductible'] = $params['deductible']>0?1:0;
			$params_array = array('days'=>$service_info['value']);
			$params_array['jobs_id'] = $jobs_id;
			$params['params'] = serialize($params_array);
			$return_order_info = $this->_order_insert($params);
			if(!$return_order_info){
				$this->ajaxReturn(0,'下单错误！请重新提交订单！');
			}else if($return_order_info['pay_type']==1){
				D('Order')->where(array('id'=>$return_order_info['id']))->save(array('is_paid'=>2,'payment_time'=>$this->timestamp));
				// 推广操作
				$promotionsqlarr['cp_uid']=$this->uid;
				$promotionsqlarr['cp_jobid']=$jobs_id;
				$promotionsqlarr['cp_ptype']=$service_info['cat'];
				$promotionsqlarr['cp_days']=$service_info['value'];
				$promotionsqlarr['cp_starttime']=$this->timestamp;
				$promotionsqlarr['cp_endtime']=strtotime("{$service_info['value']} day");
                $promotion_insert_id = D('Promotion')->add_promotion($promotionsqlarr);
                write_members_log(array('uid'=>$promotionsqlarr['cp_uid'],'utype'=>1,'username'=>''),'promotion','开通增值服务【紧急】',false,array('promotion_id'=>$promotion_insert_id));
				D('Promotion')->set_job_promotion($jobs_id,$service_info['cat']);
				/* 会员日志 */
                write_members_log(C('visitor'),'order','创建增值服务订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));
	            $p_rst = D('MembersPoints')->report_deal($this->uid,2,$return_order_info['pay_points']);
	            if($p_rst)
	            {
	            	/* 会员日志 */
                    write_members_log(C('visitor'),'order','支付订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));
                    write_members_log(C('visitor'),'increment','开通增值服务【'.$service_info['name'].'】，支付方式：'.C('qscms_points_byname').'兑换');
	                $handsel['uid'] = $this->uid;
	                $handsel['htype'] = '';
	                $handsel['htype_cn'] = '购买增值包:'.$service_info['name'];
	                $handsel['operate'] = 2;
	                $handsel['points'] = $return_order_info['pay_points'];
	                $handsel['addtime'] = $this->timestamp;
	                D('MembersHandsel')->members_handsel_add($handsel);
	            }
				$this->ajaxReturn(1,'成功兑换服务',U('order_list'));
			}else{
				$this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
			}
    	}else{
    		$jobs_id = I('get.jobs_id',0,'intval');
    		$jobs_where['uid'] = $this->uid;
            C('qscms_jobs_display') == 1 && $jobs_where['audit'] = 1 ;
            $jobs_list = D('Jobs')->where($jobs_where)->select();
			if($jobs_id){
				$jobs_info = D('Jobs')->find($jobs_id);
				if(!$jobs_info){
					$jobs_info = D('JobsTmp')->find($jobs_id);
				}
				$jobs_buy = $jobs_info['emergency']?1:0;
			}else{
				$jobs_buy = 0;
			}
			$this->assign('jobs_id',$jobs_id);
			$this->assign('jobs_buy',$jobs_buy);
			$this->assign('jobs_arr',$jobs_list);
        	$this->assign('payment_rate',C('qscms_payment_rate'));
    		$this->assign('my_points',$this->my_points);
			$this->assign('my_setmeal',$this->my_setmeal);
			$this->assign('increment_arr',$this->_get_service_list('emergency'));
	    	$this->_config_seo(array('title'=>'职位紧急 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'职位紧急'));
	    	$this->display('Company/service/service_emergency');
    	}
    }
    /**
     * 增值服务 - 预约刷新
     */
    public function service_refresh(){
    	if(IS_POST){
    		$payment_type_arr = array('points','wxpay','alipay');
			$params['payment'] = I('post.payment','points','trim');
			if(!in_array($params['payment'],$payment_type_arr)){
				$this->ajaxReturn(0,'支付方式错误！');
			}
			$jobs_id = I('post.jobs_id',0,'intval');
			if(!$jobs_id){
				$this->ajaxReturn(0,'请选择职位！');
			}
	        $promotion_field = M('QueueAutoRefresh')->where(array('pid'=>$jobs_id,'type'=>1))->find();
	        if($promotion_field)
	        {
	        	$this->ajaxReturn(0,'该职位已预约刷新！');
	        }
			$service_id = I('post.service_id',0,'intval');
			$service_info = D('SetmealIncrement')->get_cache('',$service_id);
			if(!$service_info){
				$this->ajaxReturn(0,'请选择服务！');
			}
            if(C('qscms_mobile_setmeal_increment_discount_value')>0){
                $service_info['price'] = C('qscms_mobile_setmeal_increment_discount_type')==1?$service_info['price']/100*C('qscms_mobile_setmeal_increment_discount_value'):$service_info['price']-C('qscms_mobile_setmeal_increment_discount_value');
                $service_info['price'] = $service_info['price']<0?0:$service_info['price'];
            }
			$my_discount = D('Setmeal')->get_increment_discount_by_array($service_info['cat'],$this->my_setmeal);
			//当前会员需要付的价格
			$new_price = $my_discount>0?round($service_info['price']*$my_discount/10,1):$service_info['price'];
			$params['amount'] = $new_price;
			$params['deductible'] = I('post.deductible_val',0,'intval');
			//换算积分
			$price_to_points = round($new_price*C('qscms_payment_rate'));
            $price_to_points = $price_to_points<=1?1:$price_to_points;
			if($price_to_points==$params['deductible']){
				$params['payment'] = 'points';
			}
			if(($params['payment'] == 'points' && $this->my_points<$price_to_points) || $this->my_points<$params['deductible'])
            {
            	$this->ajaxReturn(0,C('qscms_points_byname').'不足，请使用其他方式支付！');
            }
			$params['order_type'] = 12;
			$params['increment_name'] = $service_info['name'];
			$params['stemeal'] = $service_id;
			$params['discount'] = '专享'.$my_discount.'折优惠';
			$params['is_deductible'] = $params['deductible']>0?1:0;
			$params_array = array('days'=>$service_info['value']);
			$params_array['starttime'] = $this->timestamp;
            for ($i=0; $i < $service_info['value']*4; $i++) {
                $timespace = 3600*6*$i;
                if($i+1==$service_info['value']*4){
                    $params_array['endtime'] = $params_array['starttime']+$timespace;
                }
            }
			$params_array['jobs_id'] = $jobs_id;
			$params['params'] = serialize($params_array);
			$return_order_info = $this->_order_insert($params);
			if(!$return_order_info){
				$this->ajaxReturn(0,'下单错误！请重新提交订单！');
			}else if($return_order_info['pay_type']==1){
				D('Order')->where(array('id'=>$return_order_info['id']))->save(array('is_paid'=>2,'payment_time'=>$this->timestamp));
				for ($i=0; $i < $service_info['value']*4; $i++) { 
					$timespace = 3600*6*$i;
					M('QueueAutoRefresh')->add(array('uid'=>$this->uid,'pid'=>$jobs_id,'type'=>1,'refreshtime'=>$this->timestamp+$timespace));
				}
				/* 会员日志 */
                write_members_log(C('visitor'),'order','创建增值服务订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));
	            $p_rst = D('MembersPoints')->report_deal($this->uid,2,$return_order_info['pay_points']);
	            if($p_rst)
	            {
	            	/* 会员日志 */
                    write_members_log(C('visitor'),'order','支付订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));
                    write_members_log(C('visitor'),'increment','开通增值服务【'.$service_info['name'].'】，支付方式：'.C('qscms_points_byname').'兑换');
	                $handsel['uid'] = $this->uid;
	                $handsel['htype'] = '';
	                $handsel['htype_cn'] = '购买增值包:'.$service_info['name'];
	                $handsel['operate'] = 2;
	                $handsel['points'] = $return_order_info['pay_points'];
	                $handsel['addtime'] = $this->timestamp;
	                D('MembersHandsel')->members_handsel_add($handsel);
	            }
				$this->ajaxReturn(1,'成功兑换服务',U('order_list'));
			}else{
				$this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
			}
    	}else{
    		$jobs_id = I('get.jobs_id',0,'intval');
    		$jobs_where['uid'] = $this->uid;
            C('qscms_jobs_display') == 1 && $jobs_where['audit'] = 1 ;
            $jobs_list = D('Jobs')->where($jobs_where)->select();
			foreach ($jobs_list as $key => $value) {
				$has_auto = M('QueueAutoRefresh')->where(array('pid'=>$value['id'],'type'=>1))->find();
				$jobs_list[$key]['auto_refresh'] = $has_auto?1:0;
			}
			if($jobs_id){
				$has_auto = M('QueueAutoRefresh')->where(array('pid'=>$jobs_id,'type'=>1))->find();
				$jobs_buy = $has_auto?1:0;
			}else{
				$jobs_buy = 0;
			}
			$this->assign('jobs_id',$jobs_id);
			$this->assign('jobs_buy',$jobs_buy);
			$this->assign('jobs_arr',$jobs_list);
        	$this->assign('payment_rate',C('qscms_payment_rate'));
    		$this->assign('my_points',$this->my_points);
			$this->assign('my_setmeal',$this->my_setmeal);
			$this->assign('increment_arr',$this->_get_service_list('auto_refresh_jobs'));
	    	$this->_config_seo(array('title'=>'职位智能刷新 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'职位智能刷新'));
	    	$this->display('Company/service/service_refresh');
    	}
    }
    /**
     * 增值服务 - 单个职位刷新
     */
    public function service_refresh_jobs_one(){
        $payment_type_arr = array('wxpay','alipay');
        $params['payment'] = I('request.payment','wxpay','trim');
        if(!in_array($params['payment'],$payment_type_arr)){
            $this->_404('支付方式错误！');
        }
        $jobs_id = I('request.jobs_id',0,'intval');
        if(!$jobs_id){
            $this->_404('请选择职位！');
        }
        $jobs_id = is_array($jobs_id)?$jobs_id:explode(",", $jobs_id);
        $params['amount'] = C('qscms_refresh_jobs_price');
        $params['order_type'] = 13;
        $params['increment_name'] = '职位刷新';
        $params['is_deductible'] = 0;
        $params_array['jobs_id'] = $jobs_id;
        $params_array['type'] = 'jobs_refresh';
        $params['params'] = serialize($params_array);
        $return_order_info = $this->_order_insert($params);
        if(!$return_order_info){
            $this->_404('下单错误！请重新提交订单！');
        }else{
            $this->redirect('order_detail',array('order_id'=>$return_order_info['id']));
        }
    }
    /**
     * 增值服务 - 单个简历下载
     */
    public function service_down_resume_one(){
        $payment_type_arr = array('wxpay','alipay');
        $params['payment'] = I('request.payment','wxpay','trim');
        if(!in_array($params['payment'],$payment_type_arr)){
            $this->_404('支付方式错误！');
        }
        $resume_id = I('request.resume_id',0,'intval');
        if(!$resume_id){
            $this->_404('请选择简历！');
        }
        $resume_id = is_array($resume_id)?$resume_id:explode(",", $resume_id);
        $params['amount'] = C('qscms_download_resume_price');
        $params['order_type'] = 14;
        $params['increment_name'] = '简历下载';
        $params['is_deductible'] = 0;
        $params_array['resume_id'] = $resume_id;
        $params_array['type'] = 'resume_download';
        $params['params'] = serialize($params_array);
        $return_order_info = $this->_order_insert($params);
        if(!$return_order_info){
            $this->_404('下单错误！请重新提交订单！');
        }else{
            $this->redirect('order_detail',array('order_id'=>$return_order_info['id']));
        }
    }
    /**
     * 增值服务 - 诚聘通
     */
    public function service_famous(){
    	if(IS_POST){
    		$payment_type_arr = array('wxpay','alipay');
			$params['payment'] = I('post.payment','wxpay','trim');
			if(!in_array($params['payment'],$payment_type_arr)){
				$this->ajaxReturn(0,'支付方式错误！');
			}
	        $promotion_field = M('CompanyProfile')->where(array('uid'=>$this->uid,'famous'=>1))->find();
	        if($promotion_field)
	        {
	        	$this->ajaxReturn(0,'您已经是诚聘通企业了！');
	        }
			$params['amount'] = C('qscms_famous_company_price');
			$params['order_type'] = 11;
			$params['increment_name'] = '诚聘通';
			$return_order_info = $this->_order_insert($params);
			if(!$return_order_info){
				$this->ajaxReturn(0,'下单错误！请重新提交订单！');
			}else{
				$this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
			}
    	}else{
	    	$this->_config_seo(array('title'=>'诚聘通 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'诚聘通'));
	    	$this->display('Company/service/service_famous');
    	}
    }
    /**
     * 增值服务 - 购买积分
     */
    public function gold_add(){
    	if(IS_POST){
    		$payment_type_arr = array('wxpay','alipay');
			$params['payment'] = I('post.payment','wxpay','trim');
			if(!in_array($params['payment'],$payment_type_arr)){
				$this->ajaxReturn(0,'支付方式错误！');
			}
			$add_num = I('post.add_num',0,'intval');
	        if(!$add_num)
	        {
	        	$this->ajaxReturn(0,'请输入要充值的'.C('qscms_points_byname').'数！');
	        }
			$params['amount'] = floatval($add_num/C('qscms_payment_rate'));
			$params['order_type'] = 2;
			$params['points'] = $add_num;
			$params['increment_name'] = '充值'.C('qscms_points_byname');
			$return_order_info = $this->_order_insert($params);
			if(!$return_order_info){
				$this->ajaxReturn(0,'下单错误！请重新提交订单！');
			}else{
				$this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
			}
    	}else{
        	$this->assign('sys_min',C('qscms_com_buy_points_min'));
        	$this->assign('payment_rate',C('qscms_payment_rate'));
    		$this->assign('my_points',$this->my_points);
	    	$this->_config_seo(array('title'=>'充值'.C('qscms_points_byname').' - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'充值'.C('qscms_points_byname')));
	    	$this->display('Company/service/gold_add');
    	}
    }
    /**
     * 增值服务 - 升级套餐
     */
    public function setmeal_add(){
    	if(IS_POST){
    		$payment_type_arr = array('points','wxpay','alipay');
			$params['payment'] = I('post.payment','points','trim');
			if(!in_array($params['payment'],$payment_type_arr)){
				$this->ajaxReturn(0,'支付方式错误！');
			}
			$service_id = I('post.service_id',0,'intval');
			$service_info = D('Setmeal')->get_setmeal_one($service_id);
			if(!$service_info){
				$this->ajaxReturn(0,'请选择服务！');
			}
            if(C('qscms_mobile_setmeal_discount_value')>0){
                $service_info['expense'] = C('qscms_mobile_setmeal_discount_type')==1?$service_info['expense']/100*C('qscms_mobile_setmeal_discount_value'):$service_info['expense']-C('qscms_mobile_setmeal_discount_value');
                $service_info['expense'] = $service_info['expense']<0?0:$service_info['expense'];
            }
			$params['amount'] = $service_info['expense'];
			$params['deductible'] = I('post.deductible_val',0,'intval');
            // 如果后台设置购买套餐不允许使用积分
            if (!C('qscms_setmeal_by_points')){
                $params['deductible'] = 0;
            }
			//换算积分
			$price_to_points = round($service_info['expense']*C('qscms_payment_rate'));
            $price_to_points = $price_to_points<=1?1:$price_to_points;
			if($price_to_points==$params['deductible']){
				$params['payment'] = 'points';
			}
			if(($params['payment'] == 'points' && $this->my_points<$price_to_points) || $this->my_points<$params['deductible'])
            {
            	$this->ajaxReturn(0,C('qscms_points_byname').'不足，请使用其他方式支付！');
            }
			$params['order_type'] = 1;
			$params['increment_name'] = $service_info['setmeal_name'];
			$params['stemeal'] = $service_id;
			$params['is_deductible'] = $params['deductible']>0?1:0;
			$return_order_info = $this->_order_insert($params);
			if(!$return_order_info){
				$this->ajaxReturn(0,'下单错误！请重新提交订单！');
			}else if($return_order_info['pay_type']==1){
				D('Order')->where(array('id'=>$return_order_info['id']))->save(array('is_paid'=>2,'payment_time'=>$this->timestamp));
				D('MembersSetmeal')->set_members_setmeal($this->uid,$service_id);
				if($return_order_info['pay_points']>0){
					$p_rst = D('MembersPoints')->report_deal($this->uid,2,$return_order_info['pay_points']);
					if($p_rst){
		                $handsel['uid'] = C('visitor.uid');
		                $handsel['htype'] = '';
		                $handsel['htype_cn'] = $return_order_info['service_name'];
		                $handsel['operate'] = 2;
		                $handsel['points'] = $return_order_info['pay_points'];
		                $handsel['addtime'] = $this->timestamp;
		                D('MembersHandsel')->members_handsel_add($handsel);

						/* 会员日志 */
                        write_members_log(C('visitor'),'order','支付订单（订单号：'.$return_order_info['oid'].'），支付方式：'.C('qscms_points_byname').'兑换',false,array('order_id'=>$return_order_info['id']));
                        write_members_log(C('visitor'),'setmeal','开通增值服务【'.$return_order_info['service_name'].'】，支付方式：'.C('qscms_points_byname').'兑换');
						//会员套餐变更记录。会员购买成功。log_type 2表示：会员自己购买
						$members_charge_log['_t']='MembersChargeLog';
						$members_charge_log["log_uid"]=C('visitor.uid');
					 	$members_charge_log["log_username"]=C('visitor.username');
					 	$members_charge_log["log_type"]=2;
					 	$members_charge_log["log_value"]=$notes;
					 	$members_charge_log["log_amount"]=0;
					 	$members_charge_log["log_ismoney"]= 1;
					 	$members_charge_log["log_mode"]=1;
					 	$members_charge_log["log_utype"]=C('visitor.utype');
					 	setLog($members_charge_log);
					 	unset($members_charge_log);
		            }
				}
				$this->ajaxReturn(1,'成功兑换服务',U('order_list'));
			}else{
				$this->ajaxReturn(1,'下单成功',U('order_detail',array('order_id'=>$return_order_info['id'])));
			}
    	}else{
    		$setmeal_list = D('Setmeal')->order('show_order desc')->where(array('display'=>1,'apply'=>1))->select();
    		foreach ($setmeal_list as $key => $value) {
    			$setmeal_list[$key]['long'] = $value['days']==0?'永久':$this->format_days($value['days']);
    			$setmeal_list[$key]['discount'] = D('Setmeal')->get_discount_for_setmeal_one($value);
                if(C('qscms_mobile_setmeal_discount_value')>0){
                    $setmeal_list[$key]['expense'] = C('qscms_mobile_setmeal_discount_type')==1?$value['expense']/100*C('qscms_mobile_setmeal_discount_value'):$value['expense']-C('qscms_mobile_setmeal_discount_value');
                }
    			$setmeal_list[$key]['service_points'] = round($setmeal_list[$key]['expense']*C('qscms_payment_rate'));
    		}
    		$this->assign('setmeal_list',$setmeal_list);
        	$this->assign('payment_rate',C('qscms_payment_rate'));
    		$this->assign('my_points',$this->my_points);
			$this->assign('my_setmeal',$this->my_setmeal);
	    	$this->_config_seo(array('title'=>'升级套餐 - 企业会员中心 - '.C('qscms_site_name'),'header_title'=>'升级套餐'));
	    	$this->display('Company/service/setmeal_add');
    	}
    }
    /**
     * 添加订单确认
     */
    public function setmeal_add_confirm(){
        if(C('qscms_is_superposition')==0 && C('qscms_is_superposition_time')==0)//项目和时间都不叠加
        {
            $tip='您当前是【'.$this->my_setmeal['setmeal_name'].'】重新开通套餐<br /><span class="font_yellow">1. 原有套餐资源以新开套餐资源为准；</span><br /><span class="font_yellow">2. 原有会员服务时长以新开套餐时长为准；</span><br />确定要重新开通套餐吗？';
        }
        else if(C('qscms_is_superposition')==0 && C('qscms_is_superposition_time')==1)//项目不叠加时间叠加
        {
            $tip='您当前是【'.$this->my_setmeal['setmeal_name'].'】重新开通套餐<br /><span class="font_yellow">您的原套餐资源会被新开通的套餐资源覆盖</span><br />确定要重新开通套餐吗？';
        }
        else if(C('qscms_is_superposition')==1 && C('qscms_is_superposition_time')==0)//项目叠加时间不叠加
        {
            $tip='您当前是【'.$this->my_setmeal['setmeal_name'].'】重新开通套餐<br /><span class="font_yellow">您的会员服务时长将以新开套餐服务时长为准</span><br />确定要重新开通套餐吗？';
        }
        $this->ajaxReturn(1,'','<div class="dialog_notice nospace">'.$tip.'</div>');
    }
    /**
     * 购买增值服务提示
     */
    public function increment_add_confirm(){
        $setmeal_end_days = '永久';
        if($this->my_setmeal['endtime']==0){
            $tip='您当前【'.$this->my_setmeal['setmeal_name'].'】有效期 '.date('Y-m-d',$this->my_setmeal['starttime']).'至永久。增值包有效期与会员有效期一致（'.$setmeal_end_days.'），是否继续购买增值包？';
        }else{
            if($this->my_setmeal['endtime']>time()){
                $sub_day = sub_day($this->my_setmeal['endtime'],time());
                $sub_day = preg_replace('/(\d+)/','<span class="font_yellow">\1</span>',$sub_day);
                $setmeal_end_days = $sub_day.'后到期';
            }else{
                $setmeal_end_days = '已经到期';
            }
            $tip='您当前【'.$this->my_setmeal['setmeal_name'].'】有效期 '.date('Y-m-d',$this->my_setmeal['starttime']).'至'.date('Y-m-d',$this->my_setmeal['endtime']).'。增值包有效期与会员有效期一致（'.$setmeal_end_days.'），是否继续购买增值包？';
        }
        
        $this->ajaxReturn(1,'','<div class="dialog_notice nospace">'.$tip.'</div>');
    }
	protected function format_days($days){
		if($days<30){
			return $days.'天';
		}
		else
		{
			return intval($days/30).'个月';
		}
	}
    /**
     * 获取某项增值服务列表
     */
    protected function _get_service_list($cat){
    	$increment_arr = D('SetmealIncrement')->get_cache($cat);
		foreach ($increment_arr as $key => $value) {
			$free_discount = D('Setmeal')->get_increment_discount_by_array($cat,$this->my_setmeal);
			//当前会员需要付的价格
			$value['service_price'] = $free_discount>0?round($value['price']*$free_discount/10,1):$value['price'];
            if(C('qscms_mobile_setmeal_increment_discount_value')>0){
                $value['service_price'] = C('qscms_mobile_setmeal_increment_discount_type')==1?$value['service_price']/100*C('qscms_mobile_setmeal_increment_discount_value'):$value['service_price']-C('qscms_mobile_setmeal_increment_discount_value');
                $value['service_price'] = $value['service_price']<0?0:$value['service_price'];
            }
            $increment_arr[$key]['service_price'] = round($value['service_price'],1);
			//VIP会员价格,取出折扣最大的套餐折扣
			$vip_discount = D('Setmeal')->get_max_discount($cat);
			$increment_arr[$key]['vip_price'] = intval($vip_discount)>0?round($value['service_price']*$vip_discount/10,1):$value['service_price'];
			//换算积分
			$increment_arr[$key]['service_points'] = round($increment_arr[$key]['service_price']*C('qscms_payment_rate'));
		}
		return $increment_arr;
    }
    /**
	 *  插入订单数据表数据
	 *	$params['order_type']
	 *	$params['amount']
	 *	$params['deductible']
	 *	$params['increment_name']
	 *	$params['payment_name']
	 *	$params['description']
	 *	$params['points']
	 *	$params['stemeal']
	 *	$params['params']
	 *	$params['discount']
	 *	$params['is_deductible']
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
        if($this->my_setmeal['is_free']==1){
           $params['discount'] = ''; 
        }
		$insert_id = D('Order')->add_order(C('visitor'),$oid,$params['order_type'],$params['amount'],$params['pay_amount'],$params['deductible'],$params['increment_name'],$params['payment'],$paymenttpye['byname'],$params['description'],$this->timestamp,1,$params['points'],$params['stemeal'],0,$params['params'],$params['discount']);
        write_members_log(C('visitor'),'order','创建订单【'.$params['increment_name'].'】（订单号：'.$oid.'），支付方式：'.$params['payment'],false,array('order_id'=>$insert_id));
		return D('Order')->find($insert_id);
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
}
?>