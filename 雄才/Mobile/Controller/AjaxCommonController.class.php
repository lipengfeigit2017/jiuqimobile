<?php
namespace Mobile\Controller;
use Mobile\Controller\MobileController;
class AjaxCommonController extends MobileController{
	public function _initialize() {
        parent::_initialize();
    }
    /**
     * 增加企业访客统计
     */
    public function company_statistics_add(){
        $data['comid'] = I('get.comid',0,'intval');
        $data['jobid'] = I('get.jobid',0,'intval');
        $data['uid'] = intval(C('visitor.uid'));
        $data['source'] = 2;
        $model = D('CompanyStatistics');
        $model->create($data);
        $model->add();
    }
    /**
     * 记录职位点击次数
     */
    public function jobs_click(){
        $id = I('id',0,'intval');
        !$id && $this->ajaxReturn(0,'请选择要查看的职位！');
        $where = array('id'=>$id);
        if($jobs = M('Jobs')->where($where)->find()){
            $mod = M('Jobs');
            M('JobsSearch')->where($where)->setInc('click',1);
            M('JobsSearchKey')->where($where)->setInc('click',1);
        }else{
            $mod = M('JobsTmp');
        }
        $mod->where($where)->setInc('click',1);
        $click = $mod->where($where)->getfield('click');
        $this->ajaxReturn(1,'查看次数',$click);
    }
    /**
     * [news_click 资讯查看次数加一]
     */
    public function news_click(){
        $id = I('id',0,'intval');
        !$id && $this->ajaxReturn(0,'请选择要查看的资讯！');
        $where = array('id'=>$id);
        M('Article')->where($where)->setInc('click',1);
        $click = M('Article')->where($where)->getfield('click');
        $this->ajaxReturn(1,'查看次数',$click);
    }
    /**
     * [notice_click 公告查看次数加一]
     */
    public function notice_click(){
        $id = I('id',0,'intval');
        !$id && $this->ajaxReturn(0,'请选择要查看的公告！');
        $where = array('id'=>$id);
        M('Notice')->where($where)->setInc('click',1);
        $click = M('Notice')->where($where)->getfield('click');
        $this->ajaxReturn(1,'查看次数',$click);
    }
    /**
     * [hotword 搜索关健字联想]
     */
    public function hotword(){
        $key = I('get.key','','trim');
        $key = urldecode(urldecode($key));
        !$key && $this->ajaxReturn(0,'请输入关健字！');
        $reg = D('Hotword')->get_hotword($key);
        if($reg) $this->ajaxReturn(1,'联想词获取成功！',$reg);
        $this->ajaxReturn(0);
    }
    /**
     * [hotword 搜索分站]
     */
    public function subsite_by_keyword(){
        $key = I('get.key','','trim');
        $key = urldecode(urldecode($key));
        !$key && $this->ajaxReturn(0,'请输入关健字！');
        $passport = $this->_user_server();
        if(!$passport->is_sitegroup()){
            if(C('apply.Subsite')){
                $reg = M('Subsite')->where(array('s_sitename'=>array('like',$key.'%')))->select();
            }
        }else{
            $sub = $passport->uc('sitegroup')->get_subsite();
            foreach($sub['subsite'] as $val){
                if(0 === mb_strpos($val['name'],$key)) $reg[] = $val;
            }
        }
        if($reg) $this->ajaxReturn(1,'分站列表获取成功！',$reg);
        $this->ajaxReturn(0);
    }
}
?>