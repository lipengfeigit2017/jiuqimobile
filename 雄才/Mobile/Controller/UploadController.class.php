<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
use Common\ORG\qiniu;
class UploadController extends MobileController{
	public function _initialize() {
		parent::_initialize();
		//访问者控制
		if (!$this->visitor->is_login) {
			IS_AJAX && $this->ajaxReturn(0, L('login_please'),'',1);
			//非ajax的跳转页面
			$this->redirect('members/login');
		}
	}
	/**
	 * [company_logo 企业logo]
	 */
	public function company_logo(){
		$config_params = array(
			'upload_ok'=>false,
			'save_path'=>'',
			'show_path'=>''
		);
		$company_id = I('post.company_id',0,'intval');
	    $uid = C('visitor.uid');
		$pic=base64_decode($_POST['base64_string']);
		if(C('qscms_qiniu_open')==1){
	   		$filename = md5($company_id.time()).'.jpg';
			$qiniu = new qiniu(array(
            	'stream'=>true
            ));
            $config_params['save_path'] = $config_params['show_path'] = $qiniu->uploadStream($pic,$filename);
            if($config_params['save_path']){
            	$config_params['upload_ok'] = true;
            }else{
            	$config_params['info'] = $qiniu->getError();
            }
		}else{
			//日期路径
	    	$date = date('ym/d/');
	    	$save_avatar=C('qscms_attach_path').'company_logo/'.$date;//图片存储路径
	    	if(!is_dir($save_avatar)){
		    	mkdir($save_avatar,0777,true);
		    }
	   		$filename = md5($company_id).'.jpg';
			file_put_contents($save_avatar.$filename,$pic);
			$config_params['save_path'] = $date.$filename;
			$config_params['show_path'] = attach($config_params['save_path'],'company_logo').'?'.time();
			$config_params['upload_ok'] = true;
		}
		if($config_params['upload_ok']){
        	$rst = M('CompanyProfile')->where(array('id'=>$company_id,'uid'=>C('visitor.uid')))->setfield('logo',$config_params['save_path']);
			$r = D('TaskLog')->do_task(C('visitor'),19);
			$data = array('path'=>$config_params['show_path'],'img'=>$config_params['save_path']);
			$this->ajaxReturn(1, L('upload_success'), $data);
        }else{
        	$this->ajaxReturn(0, $config_params['info']);
        }
	}
	public function company_img(){
		$config_params = array(
			'upload_ok'=>false,
			'save_path'=>'',
			'show_path'=>''
		);
		$company_id = I('post.company_id',0,'intval');
		$uid = C('visitor.uid');
		$savePicName = uniqid().'.jpg';
		$pic=base64_decode($_POST['base64_string']);
		if(C('qscms_qiniu_open')==1){
			$qiniu = new qiniu(array(
            	'stream'=>true
            ));
            $config_params['save_path'] = $config_params['show_path'] = $qiniu->uploadStream($pic,$savePicName);
            if($config_params['save_path']){
            	$config_params['upload_ok'] = true;
            }else{
            	$config_params['info'] = $qiniu->getError();
            }
		}else{
			$date = date('ym/d/');
	    	$save_avatar=C('qscms_attach_path').'company_img/'.$date;//图片存储路径
	    	if(!is_dir($save_avatar)){
		    	mkdir($save_avatar,0777,true);
		    }
			$filename = $save_avatar.$savePicName;
			file_put_contents($filename,$pic);
			$config_params['save_path'] = $date.$savePicName;
			$config_params['show_path'] = attach($config_params['save_path'],'company_img');
			$config_params['upload_ok'] = true;
		}
		if($config_params['upload_ok']){
        	$setsqlarr['uid']=C('visitor.uid');
			$setsqlarr['company_id']=$company_id;
			$setsqlarr['img']=$config_params['save_path'];
			$rst = D('CompanyImg')->add_company_img($setsqlarr,C('visitor'));
			!$rst['state'] && $this->ajaxReturn(0, $rst['error']);
			$r = D('TaskLog')->do_task(C('visitor'),20);
			$data = array('path'=>$config_params['show_path'],'img'=>$config_params['save_path']);
			$this->ajaxReturn(1, L('upload_success'), $data);
        }else{
        	$this->ajaxReturn(0, $config_params['info']);
        }
	}
	// 企业营业执照 上传
	public function certificate_img(){
		$config_params = array(
			'upload_ok'=>false,
			'save_path'=>'',
			'show_path'=>''
		);
		$pic=base64_decode($_POST['base64_string']);
	    $filename = md5(C('visitor.uid').time()).'.jpg';
	    $uid = C('visitor.uid');
		if(C('qscms_qiniu_open')==1){
			$qiniu = new qiniu(array(
            	'stream'=>true
            ));
            $config_params['save_path'] = $config_params['show_path'] = $qiniu->uploadStream($pic,$filename);
            if($config_params['save_path']){
            	$config_params['upload_ok'] = true;
            }else{
            	$config_params['info'] = $qiniu->getError();
            }
		}else{
			//日期路径
	    	$date = date('ym/d/');
	    	$save_avatar=C('qscms_attach_path').'certificate_img/'.$date;//图片存储路径
	    	if(!is_dir($save_avatar)){
		    	mkdir($save_avatar,0777,true);
		    }
			file_put_contents($save_avatar.$filename,$pic);
			$config_params['save_path'] = $date.$filename;
			$config_params['show_path'] = attach($config_params['save_path'],'certificate_img').'?'.time();
			$config_params['upload_ok'] = true;
		}
		if($config_params['upload_ok']){
			$data = array('path'=>$config_params['show_path'],'img'=>$config_params['save_path']);
			$this->ajaxReturn(1, L('upload_success'), $data);
        }else{
        	$this->ajaxReturn(0, $config_params['info']);
        }
	}
	/**
	 * [avatar 头像上传保存]
	 */
	public function avatar(){
		$config_params = array(
			'upload_ok'=>false,
			'save_path'=>'',
			'show_path'=>''
		);
		$uid = C('visitor.uid');
	    $savePicName = md5($uid.time()).'.jpg';
	    $pic=base64_decode($_POST['base64_string']);
	    $size = explode(',',C('qscms_avatar_size'));
		if(C('qscms_qiniu_open')==1){
			$qiniu = new qiniu(array(
            	'stream'=>true
            ));
            $config_params['save_path'] = $config_params['show_path'] = $qiniu->uploadStream($pic,$savePicName);
            if($config_params['save_path']){
            	foreach ($size as $val) {
            		$thumb_name = $qiniu->getThumbName($config_params['save_path'],$val,$val);
            		$qiniu->uploadStream($pic,$thumb_name,$val,$val,true);
				}
            	$config_params['upload_ok'] = true;
            }else{
            	$config_params['info'] = $qiniu->getError();
            }
		}else{
			//日期路径
	    	$date = date('ym/d/');
	    	$save_avatar=C('qscms_attach_path').'avatar/'.$date;//图片存储路径
	    	if(!is_dir($save_avatar)){
		    	mkdir($save_avatar,0777,true);
		    }
			$filename = $save_avatar.$savePicName;
			file_put_contents($filename,$pic);
			$image= new \Common\ORG\ThinkImage();
			foreach ($size as $val) {
				$image->open($filename)->thumb($val,$val,3)->save($filename."_".$val."x".$val.".jpg");
			}
			$config_params['save_path'] = $date.$savePicName;
			$config_params['show_path'] = attach($img,'avatar').'?'.time();
			$config_params['upload_ok'] = true;
		}
		if($config_params['upload_ok']){
			$setsqlarr['avatars']=$config_params['save_path'];
			$old_avatar = D('Members')->where(array('uid'=>$uid))->getfield('avatars');
			$photo = M('Resume')->field('photo_audit,photo_display')->where(array('uid'=>$uid,'def'=>1))->find();
			if($photo['photo_display'] == 1){
				$setsqlarr['photo'] = 1;
			}else{
				$setsqlarr['photo'] = 0;
			}
			if(true !== $reg = D('Members')->update_user_info($setsqlarr,C('visitor'))) $this->ajaxReturn(0,$reg);
			$user_resume_list = D('Resume')->where(array('uid'=>$uid))->select();
			foreach ($user_resume_list as $key => $value) {
				D('Resume')->check_resume($uid,$value['id']);//更新简历完成状态
			}
			D('TaskLog')->do_task(C('visitor'),5);
			write_members_log(C('visitor'),'','保存个人头像');
			$data = array('path'=>$config_params['show_path'],'img'=>$config_params['save_path']);
			$this->ajaxReturn(1, L('upload_success'), $data);
        }else{
        	$this->ajaxReturn(0, $config_params['info']);
        }
	}
	/**
	 * [resume_img 个人简历图片上传]
	 * @return [type] [description]
	 */
	public function resume_img(){
		$config_params = array(
			'upload_ok'=>false,
			'save_path'=>'',
			'show_path'=>''
		);
	    $filename = uniqid().'.jpg';
	    $uid = C('visitor.uid');
		$pic=base64_decode($_POST['base64_string']);
		$size = explode(',',C('qscms_resume_img_size'));
		if(C('qscms_qiniu_open')==1){
			$qiniu = new qiniu(array(
            	'stream'=>true
            ));
            $config_params['save_path'] = $config_params['show_path'] = $qiniu->uploadStream($pic,$filename);
            if($config_params['save_path']){
            	foreach ($size as $val) {
            		$thumb_name = $qiniu->getThumbName($config_params['save_path'],$val,$val);
            		$qiniu->uploadStream($pic,$thumb_name,$val,$val,true);
				}
            	$config_params['upload_ok'] = true;
            }else{
            	$config_params['info'] = $qiniu->getError();
            }
		}else{
			//日期路径
	    	$date = date('ym/d/');
	    	$save_avatar=C('qscms_attach_path').'resume_img/'.$date;//图片存储路径
	    	if(!is_dir($save_avatar)){
		    	mkdir($save_avatar,0777,true);
		    }
			file_put_contents($save_avatar.$filename,$pic);
			$image= new \Common\ORG\ThinkImage();
			$path = $save_avatar.$filename;
			foreach ($size as $val) {
				$image->open($path)->thumb($val,$val,3)->save("{$path}_{$val}x{$val}.jpg");
			}
			$config_params['save_path'] = $date.$filename;
			$config_params['show_path'] = attach($config_params['save_path'],'resume_img');
			$config_params['upload_ok'] = true;
		}
		if($config_params['upload_ok']){
			$data = array('path'=>$config_params['show_path'],'img'=>$config_params['save_path']);
			$this->ajaxReturn(1, L('upload_success'), $data);
        }else{
        	$this->ajaxReturn(0, $config_params['info']);
        }
	}
}
?>