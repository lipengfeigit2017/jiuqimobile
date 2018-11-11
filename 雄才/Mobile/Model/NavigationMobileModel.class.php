<?php
namespace Mobile\Model;
use Think\Model;
class NavigationMobileModel extends Model{
	/**
	 * [nav_cache 读取页导航写入缓存]
	 * @return [array] [description]
	 */
	public function nav_cache(){
		$nav_list = $this->where(array('display'=>1))->order('ordid desc,id asc')->select();
		F('nav_mobile_list',$nav_list);
		return $nav_list;
	}
    public function get_nav(){
        if(false === $list = F('nav_mobile_list')){
            $list = $this->nav_cache();
        }
        foreach ($list as $key => $value) {
            $list[$key]['url'] = $value['url']?$value['url']:$this->get_url($value['alias']);
        }
        return $list;
    }
    protected function get_url($alias){
        switch($alias){
            case 'joblist':
                return url_rewrite('QS_jobslist');
                break;
            case 'resumelist':
                return url_rewrite('QS_resumelist');
                break;
            case 'news':
                return url_rewrite('QS_newslist');
                break;
            case 'hotjobs':
                return url_rewrite('QS_jobslist');
                break;
            case 'nearbyjobs':
                return url_rewrite('QS_jobslist',array('range'=>20));
                break;
            case 'jobfair':
                return url_rewrite('QS_jobfairlist');
                break;
            case 'mall':
                return url_rewrite('QS_mall_index');
                break;
            case 'publish_resume':
                return U('Personal/resume_add');
                break;
            case 'publish_job':
                return U('Company/jobs_add');
                break;
            case 'allowance':
                return url_rewrite('QS_jobslist',array('search_cont'=>'allowance'));
                break;
            case 'cloud':
                return 'http://yun.51lianzhi.cn';
                break;
            case 'parttime':
                return U('PartTime/index');
                break;
            case 'store':
                return U('Storerecruit/index');
                break;
            case 'house':
                return U('house/index');
                break;
            case 'gworker':
                return U('Gworker/index');
                break;
            default:
                return url_rewrite('QS_index');
                break;
        }
    }
	/**
     * 后台有更新则删除缓存
     */
    protected function _before_write($data, $options) {
        F('nav_mobile_list', NULL);
    }
    /**
     * 后台有删除也删除缓存
     */
    protected function _after_delete($data,$options){
        F('nav_mobile_list', NULL);
    }
}
?>