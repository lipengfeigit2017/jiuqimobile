--
-- 转存表中的数据 `qs_menu`
--

INSERT INTO `qs_menu` (`id`, `name`, `pid`, `spid`, `log_cn`, `module_name`, `controller_name`, `action_name`, `menu_type`, `is_parent`, `data`, `remark`, `often`, `ordid`, `display`, `stat`, `sys_set`, `mods`, `img`) VALUES
(251, '移动端', 0, '251|', '修改移动端设置', 'Mobile', 'Admin', 'index', 1, 1, '', '', 0, 4, 1, '', 1, 0, ''),
(729, '手机触屏', 251, '251|729|', '', 'Mobile', 'Admin', 'index', 1, 1, '', '', 0, 255, 1, '', 0, 0, '766.png'),
(730, '触屏配置', 729, '251|729|730|', '', 'Mobile', 'Admin', 'index', 1, 1, '', '', 0, 255, 1, '', 0, 0, ''),
(731, '修改配置', 730, '251|729|730|731|', '', 'Mobile', 'Admin', 'edit', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(732, '页面配置', 729, '251|729|732|', '', 'Mobile', 'Admin', 'page', 1, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(733, '新增页面', 732, '251|729|732|733|', '', 'Mobile', 'Admin', 'page_add', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(734, '修改页面', 732, '251|729|732|734|', '', 'Mobile', 'Admin', 'page_edit', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(735, '删除页面', 732, '251|729|732|735|', '', 'Mobile', 'Admin', 'page_delete', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(736, '设置链接', 732, '251|729|732|736|', '', 'Mobile', 'Admin', 'set_url', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(737, '设置缓存', 732, '251|729|732|737|', '', 'Mobile', 'Admin', 'set_caching', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(738, '模板切换', 729, '251|729|738|', '', 'Mobile', 'Admin', 'tpl', 1, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(739, '备份模板', 739, '251|729|739|739|', '', 'Mobile', 'Admin', 'backup', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(740, '更换模板', 739, '251|729|739|740|', '', 'Mobile', 'Admin', 'set', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(741, '设置链接弹窗', 732, '251|729|732|741|', '', 'Mobile', 'Admin', 'ajax_set_url', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(742, '设置缓存弹窗', 732, '251|729|732|742|', '', 'Mobile', 'Admin', 'ajax_set_cache', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(743, '导航设置', 729, '251|729|743|', '', 'Mobile', 'Admin', 'nav_list', 1, 1, '', '', 0, 255, 1, '', 0, 0, ''),
(744, '全部保存', 743, '251|729|743|744|', '', 'Mobile', 'Admin', 'nav_save_all', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(745, '修改导航', 743, '251|729|743|745|', '', 'Mobile', 'Admin', 'nav_edit', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(746, '广告管理', 729, '251|729|746|', '', 'Mobile', 'Ad', 'index', 1, 1, '', '', 0, 255, 1, '', 0, 0, ''),
(747, '添加广告', 746, '251|729|746|747|', '', 'Mobile', 'Ad', 'add', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(748, '修改广告', 746, '251|729|746|748|', '', 'Mobile', 'Ad', 'edit', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(749, '删除广告', 746, '251|729|746|749|', '', 'Mobile', 'Ad', 'delete', 0, 0, '', '', 0, 255, 1, '', 0, 0, ''),
(750, 'H5微信招聘', 729, '251|729|750|', '', 'Mobile', 'Admin', 'wzp', 1, 0, '', '', 0, 255, 1, '', 0, 0, '');

--
-- 转存表中的数据 `qs_config`
--

INSERT INTO `qs_config` (`id`, `name`, `value`, `note`, `type`) VALUES
(NULL, 'wap_domain', '', '触屏版域名', 'Mobile'),
(NULL, 'mobile_captcha_open', '0', '是否开启触屏端极验', 'Mobile'),
(NULL, 'mobile_captcha', 'a:2:{s:2:"id";s:32:"7c25da6fe21944cfe507d2f9876775a9";s:3:"key";s:32:"f5883f4ee3bd4fa8caec67941de1b903";}', '验证账号触屏设置', 'Mobile'),
(NULL, 'mobile_isclose', '0', '触屏端是否关闭', 'Mobile'),
(NULL, 'mobile_close_reason', '测试中', '触屏站点关闭原因', 'Mobile'),
(NULL, 'mobile_statistics', '', '触屏端站点统计', 'Mobile'),
(NULL, 'mobile_closereg', '0', '触屏端关闭注册', 'Mobile'),
(NULL, 'resume_base', '10000', '简功数量基值', 'Mobile'),
(NULL, 'jobs_base', '10000', '职位数量基值', 'Mobile'),
(NULL, 'footer_nav_status', '0', '触屏端底部导航上否开启', 'Mobile'),
(NULL, 'wap_captcha_config', 'a:3:{s:10:"varify_reg";s:1:"1";s:13:"varify_mobile";s:1:"1";s:10:"user_login";s:1:"3";}', '触屏极验配置', 'Mobile'),
(NULL, 'page_full_screen', '1', '页面是否全屏显示','Mobile'),
(NULL, 'mobile_subsite_choose_type', 'simple', '分站选择样式', 'Mobile'),
(NULL, 'm_template_dir', 'default', '触屏端模板切换', 'Mobile'),
(NULL, 'mobile_setmeal_discount_type', '0', '触屏版购买套餐优惠类型', 'Mobile'),
(NULL, 'mobile_setmeal_discount_value', '0', '触屏版购买套餐优惠', 'Mobile'),
(NULL, 'mobile_setmeal_increment_discount_type', '0', '触屏版购买增值服务优惠类型', 'Mobile'),
(NULL, 'mobile_setmeal_increment_discount_value', '0', '触屏版购买增值服务优惠', 'Mobile'),
(NULL, 'mobile_discount_qrcode_url', '', '优惠引导二维码url', 'Mobile'),
(NULL, 'refresh_jobs_more', '0', '触屏版刷新职位额外次数', 'Mobile'),
(NULL, 'logo_mobile', '', '触屏logo', 'Mobile'),
(NULL, 'mobile_top_color', '', '触屏顶部背景色', 'Mobile'),
(NULL, 'mobile_index_jobs_type', 'new', '首页职位推荐类型', 'Mobile'),
(NULL, 'mobile_index_login_recommend', '1', '登录开启千人千面', 'Mobile'),
(NULL, 'wzp_share_title', '', 'H5微信招聘分享标题', 'Mobile'),
(NULL, 'wzp_share_desc', '', 'H5微信招聘分享描述', 'Mobile'),
(NULL, 'wzp_share_img', '', 'H5微信招聘分享图片', 'Mobile');

--
-- 转存表中的数据 `qs_page`
--

INSERT INTO `qs_page` VALUES
(NULL, 1, 1, 'QS_index', '网站首页', 'Mobile', 'Index', 'index', '', 0, 0, 'Index', '国聘网高端人才系统(localhost)', '国聘CMS是基于PHP+MYSQL的免费网站管理系统，提供完善的人才招聘网站建设方案', '国聘网，74cms，骑士cms，人才网站源码，php人才网程序', '', 'Mobile'),
(NULL, 1, 2, 'QS_jobslist', '职位列表页', 'Mobile', 'Jobs', 'index', '', 0, 0, 'jobs', '职位列表页-{site_name}', '', '', 'a:3:{s:11:"jobcategory";s:12:"职位分类";s:12:"citycategory";s:12:"地区分类";s:3:"key";s:9:"关健字";}', 'Mobile'),
(NULL, 1, 3, 'QS_jobsshow', '招聘详细页', 'Mobile', 'Jobs', 'show', '', 1, 0, 'jobs', '{jobs_name} - {companyname} - {site_name}', '{companyname}招聘岗位,{jobs_name}', '{companyname},{jobs_name},{nature_cn},{category_cn},{district_cn}', 'a:8:{s:9:"jobs_name";s:12:"职位名称";s:11:"companyname";s:12:"公司名称";s:6:"sex_cn";s:6:"性别";s:9:"nature_cn";s:12:"工作性质";s:11:"category_cn";s:12:"职位类别";s:8:"trade_cn";s:6:"行业";s:11:"district_cn";s:6:"地区";s:12:"education_cn";s:6:"学历";}', 'Mobile'),
(NULL, 1, 3, 'QS_companyshow', '企业简介页', 'Mobile', 'Jobs', 'comshow', '', 0, 0, 'jobs', '{companyname} - {site_name}', '{contents},公司简介', '{companyname},公司简介', 'a:7:{s:11:"companyname";s:12:"公司名称";s:9:"nature_cn";s:12:"公司性质";s:11:"category_cn";s:12:"职位类别";s:8:"trade_cn";s:12:"公司行业";s:11:"district_cn";s:12:"所在地区";s:8:"scale_cn";s:12:"公司规模";s:8:"contents";s:12:"公司介绍";}', 'Mobile'),
(NULL, 1, 4, 'QS_goods_exchange', '兑换商品', 'Mobile', 'Mall', 'create_order', '', 0, 0, 'shop_list', '兑换商品 - {site_name}', '', '', '', 'Mobile'),
(NULL, 1, 1, 'QS_login', '会员登录', 'Mobile', 'Members', 'login', '', 0, 0, 'members', '会员登录', '', '', '', 'Mobile'),
(NULL, 1, 2, 'QS_resumelist', '简历列表', 'Mobile', 'Resume', 'index', '', 0, 0, 'Resume', '简历列表  - {site_name}', '', '', 'a:3:{s:11:"jobcategory";s:12:"职位分类";s:12:"citycategory";s:12:"地区分类";s:3:"key";s:9:"关健字";}', 'Mobile'),
(NULL, 1, 3, 'QS_resumeshow', '简历详细页', 'Mobile', 'Resume', 'show', '', 0, 0, 'Resume', '{fullname}的个人简历 -{site_name}', '{title},{fullname},{specialty}', '{fullname},{sex_cn},{nature_cn},{trade_cn},{district_cn},{wage_cn},{education_cn},{major_cn},{intention_jobs}', 'a:11:{s:5:"title";s:12:"简历标题";s:8:"fullname";s:12:"真实姓名";s:6:"sex_cn";s:6:"性别";s:9:"nature_cn";s:12:"工作性质";s:8:"trade_cn";s:12:"意向行业";s:11:"district_cn";s:12:"意向地区";s:7:"wage_cn";s:12:"期望薪资";s:12:"education_cn";s:6:"学历";s:8:"major_cn";s:6:"专业";s:14:"intention_jobs";s:12:"期望职位";s:9:"specialty";s:12:"自我描述";}', 'Mobile'),
(NULL, 1, 3, 'QS_goods_show', '积分商城详细页面', 'Mobile', 'Mall', 'show', '', 0, 0, 'shop_list', '积分商城 - {site_name}', '{content}', '{category_cn},{goods_title},{goods_brand}', 'a:4:{s:11:"category_cn";s:12:"分类名称";s:11:"goods_title";s:12:"商品名称";s:7:"content";s:12:"商品描述";s:11:"goods_brand";s:12:"商品品牌";}', 'Mobile'),
(NULL, 1, 2, 'QS_newslist', '资讯列表', 'Mobile', 'News', 'index', '', 0, 0, 'news', '资讯中心 -  {categoryname} -{site_name}', '', '', 'a:2:{s:3:"key";s:9:"关键字";s:12:"categoryname";s:12:"分类名称";}', 'Mobile'),
(NULL, 1, 3, 'QS_newsshow', '资讯详细页', 'Mobile', 'News', 'show', '', 0, 0, 'news', '{title} - {site_name}', '{seo_description}', '{seo_keywords}', 'a:3:{s:5:"title";s:12:"文章标题";s:12:"seo_keywords";s:18:"Seo优化关键字";s:15:"seo_description";s:16:"Seo 优化描述";}', 'Mobile'),
(NULL, 1, 1, 'QS_mall_index', '积分商城首页', 'Mobile', 'Mall', 'index', '', 0, 0, 'shop', '积分商城首页', '', '', '', 'Mobile'),
(NULL, 1, 2, 'QS_goods_list', '积分商城列表页', 'Mobile', 'Mall', 'plist', '', 0, 0, 'shop_list', '积分商城{key}-{site_name}', '', '', 'a:1:{s:3:"key";s:9:"关键字";}', 'Mobile'),
(NULL, 1, 3, 'QS_noticeshow', '公告详细页', 'Mobile', 'Notice', 'show', '', 0, 0, 'notice', '{title} - {site_name}', '{seo_description}', '{seo_keywords}', 'a:3:{s:5:"title";s:12:"文章标题";s:12:"seo_keywords";s:18:"Seo优化关键字";s:15:"seo_description";s:16:"Seo 优化描述";}', 'Mobile'),
(NULL, 1, 2, 'QS_jobfairexhibitors', '参会企业列表', 'Mobile', 'Jobfair', 'comlist', '', 0, 0, 'jobfair', '{title} - 参会企业 - 招聘会 - {site_name}', '{introduction},{address},{bus}', '{title},{address},{bus}', 'a:5:{s:5:"title";s:15:"招聘会标题";s:12:"introduction";s:15:"招聘会简介";s:7:"address";s:12:"举办地址";s:3:"bus";s:12:"乘车路线";s:6:"number";s:9:"展位数";}', 'Mobile'),
(NULL, 1, 3, 'QS_jobfairshow', '招聘会详细页', 'Mobile', 'Jobfair', 'show', '', 0, 0, 'jobfair', '{title} - 招聘会 - {site_name}', '{introduction},{address},{bus}', '{title},{address},{bus}', 'a:5:{s:5:"title";s:15:"招聘会标题";s:12:"introduction";s:15:"招聘会简介";s:7:"address";s:12:"举办地址";s:3:"bus";s:12:"乘车路线";s:6:"number";s:9:"展位数";}', 'Mobile'),
(NULL, 1, 2, 'QS_jobfairlist', '招聘会列表', 'Mobile', 'Jobfair', 'index', '', 0, 0, 'jobfair', '招聘会 - {site_name}', '', '', '', 'Mobile'),
(NULL, 1, 5, 'QS_mall_order_list', '兑换记录', 'Mobile', 'Mall', 'order_list', '', 0, 0, 'shop_list', '兑换记录 -{site_name}', '', '', '', 'Mobile'),
(NULL, 1, 6, 'QS_mall_order_show', '兑换详情', 'Mobile', 'Mall', 'order_show', '', 0, 0, 'shop_list', '兑换详情 -{site_name}', '', '', '', 'Mobile'),
(NULL, 1, 6, 'QS_jobfairreserve', '在线预定页', 'Mobile', 'Jobfair', 'reserve', '', 0, 0, 'jobfair', '{title} - 在线预定 - 招聘会 - {site_name}', '', '', '', 'Mobile');

--
-- 转存表中的数据 `qs_navigation`
--

INSERT INTO `qs_navigation_mobile` VALUES
(NULL, '找工作', '找工作', '', 1, 0, 'joblist', ''),
(NULL, '招人才', '招人才', '', 1, 0, 'resumelist', ''),
(NULL, '职场资讯', '职场资讯', '', 0, 0, 'news', ''),
(NULL, '附近工作', '附近工作', '', 0, 0, 'nearbyjobs', ''),
(NULL, '招聘会', '招聘会', '', 0, 0, 'jobfair', ''),
(NULL, '积分商城', '积分商城', '', 0, 0, 'mall', ''),
(NULL, '发布简历', '发布简历', '', 1, 0, 'publish_resume', ''),
(NULL, '发布职位', '发布职位', '', 1, 0, 'publish_job', ''),
(NULL, '红包职位', '红包职位', '', 0, 0, 'allowance', ''),
(NULL, '云端职位', '云端职位', '', 1, 0, 'cloud', 'http://yun.51lianzhi.cn');