<?php
namespace Common\Model;
use Think\Model;
class PageModel extends Model{
	protected $_validate = array(
		array('alias,pname,module,controller,action,rewrite,tag','identicalNull','',0,'callback'),
	);
	protected $_auto = array ( 
		array('systemclass',0),
		array('pagetpye',1),
		array('url',0),
		array('caching',0),
		array('type','Mobile'),
	);
	/**
	 * 读取系统参数生成缓存文件
	 */
	public function page_cache() {
		if(C('SUBSITE_VAL')){
			$where['subsite_id'] = C('SUBSITE_VAL.s_id');
			$cache = '_'.C('SUBSITE_VAL.s_id');
		}
		$where['type'] = 'Mobile';
		if(false === $pageList = F('mobile_page_list'.$cache)){
			$pageList = $this->where($where)->getfield('alias,id,pname,module,controller,action,rewrite,url,tag');
			foreach ($pageList as $key => $val) {
				$pageList[$key]['module'] = $pageList[$key]['module'];
				$pageList[$key]['controller'] = $pageList[$key]['controller'];
				$pageList[$key]['action'] = $pageList[$key]['action'];
			}
			F('Mobile_page_list'.$cache, $pageList);
		}
		return $pageList;
	}
	public function page_seo_cache($subsite_id = false){
		if($subsite_id !== false){
			$where['subsite_id'] = $subsite_id;
			$cache = '_'.$subsite_id;
		}
		$where['type'] = 'Mobile';
		$pageList = $this->where($where)->field('pname,module,controller,action,title,keywords,description')->select();
		foreach ($pageList as $key => $val) {
			$page[strtolower($val['module']).'_'.strtolower($val['controller']).'_'.strtolower($val['action'])] = array('pname'=>$val['pname'],'title'=>$val['title'],'keywords'=>$val['keywords'],'description'=>$val['description']);
		}
		F('Mobile_page_seo_list'.$cache, $page);
		return $page;
	}
	public function get_page(){
		if(C('SUBSITE_VAL')){
			$subsite_id = C('SUBSITE_VAL.s_id');
			$cache = '_'.$subsite_id;
		}
		if(false === $page_seo = F('Mobile_page_seo_list'.$cache)) $page_seo = $this->page_seo_cache($subsite_id);
		return $page_seo;
	}
	/**
	 * 检测调用名是否重复
	 */
	public function ck_page_alias($alias,$noid=NULL,$subsite_id = false){
		if ($noid) $map['id'] = array('neq',intval($noid));
		$map['alias'] = $alias;
		$map['type'] = 'Mobile';
		if(C('apply.Subsite') && $subsite_id !== false) {
			!is_array($subsite_id) && $subsite_id = array($subsite_id);
			$map['subsite_id'] = array('in',$subsite_id);
		}
		$info = $this->where($map)->find();
		if ($info) return true;
		return false;
	}
	/**
	 * 更改页面URL
	 */
	public function set_page_url($pid,$url,$norewrite)
	{
		if (!is_array($pid)) return false;
		$sqlin=implode(",",$pid);
		$noarray=array();
		if ($url=="1")
		{
		$noarray=$norewrite;
		}
		if (fieldRegex($sqlin,'in'))
		{
			$not_in='';
			$map = false;
			if (!empty($noarray))
			{
				foreach ($noarray as $s)
				{
					$not_in[] = $s;
				}
			}
			if(!empty($not_in)){
				$map['alias'] = array('not in',$not_in);
			}
			$map['id'] = array('in',$sqlin);
			$this->where($map)->setField('url',intval($url));
			return true;
		}
		return false;
	}
	//更改页面缓存
	public function set_page_caching($pid,$caching,$nocaching)
	{
		if (!is_array($pid)) return false;
		$sqlin=implode(",",$pid);
		if (fieldRegex($sqlin,'in'))
		{
			$not_in='';
			$map = false;
			foreach ($nocaching as $s)
			{
				$not_in[] = $s;
			}
			if(!empty($not_in)){
				$map['alias'] = array('not in',$not_in);
			}
			$map['id'] = array('in',$sqlin);
			$this->where($map)->setField('caching',intval($caching));
			return true;
		}
		return false;
	}
	/**
	 * 删除页面
	 */
	public function del_page($id){
		$return=0;
		if (!is_array($id))$id=array($id);
		$sqlin=implode(",",$id);
		if (fieldRegex($sqlin,'in'))
		{
			$return = $this->where(array('id'=>array('in',$sqlin),'systemclass'=>array('neq',1)))->delete();
		}
		return $return;
	}
	/**
     * 后台有更新则删除缓存
     */
    protected function _before_write($data, $options) {
    	if(C('apply.Subsite')){
    		$subsite_list = D('Subsite')->get_subsite_domain();
    		foreach ($subsite_list as $key => $val) {
    			F('Mobile_page_seo_list_'.$val['s_id'], NULL);
    			F('Mobile_page_list_'.$val['s_id'], NULL);
    		}
		}
        F('Mobile_page_list', NULL);
        F('Mobile_page_seo_list',NULL);
    }
    /**
     * 后台有删除也删除缓存
     */
    protected function _after_delete($data,$options){
        $this->_before_write();
    }
}
?>