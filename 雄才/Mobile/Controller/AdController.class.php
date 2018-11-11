<?php
namespace Mobile\Controller;
use Common\Controller\BackendController;
use Common\ORG\qiniu;
class AdController extends BackendController{
	public function _initialize() {
        parent::_initialize();
        $this->_mod = D('AdMobile');
        $this->_name = 'AdMobile';
    }
    public function _before_index(){
        $this->order = 'show_order desc';
        $category_list = $this->_mod->category;
    	$this->assign('category_list',$category_list);
    }
    public function _before_add(){
        $category_list = $this->_mod->category;
        $this->assign('category_list',$category_list);
    }
    public function _before_insert($data){
		if($_POST['attach_file']){
            $data['content'] = I('post.attach_file','','trim');
		}else{
			$data['content'] = I('post.attach_path','','trim');
		}
    	return $data;
    }
    public function _before_edit(){
        $this->_before_add();
    }
    public function _before_update($data){
        return $this->_before_insert($data);
    }
    /**
     * [_before_search 查询条件]
     */
    public function _before_search($data){
        $key_type = I('request.key_type',0,'intval');
        $key = I('request.key','','trim');
        if($key_type && $key){
            switch ($key_type){
                case 1:
                    $data['title'] = array('like','%'.$key.'%');
                    break;
            }
        }
        return $data;
    }
}
?>