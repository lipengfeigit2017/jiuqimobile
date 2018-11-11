--
-- 表的结构 `qs_navigation_mobile`
--

DROP TABLE IF EXISTS `qs_navigation_mobile`;
CREATE TABLE `qs_navigation_mobile` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `sys_name` varchar(30) NOT NULL,
  `show_name` varchar(30) NOT NULL,
  `nav_img` varchar(255) NOT NULL,
  `display` tinyint(1) unsigned NOT NULL,
  `ordid` int(10) unsigned NOT NULL,
  `alias` varchar(30) NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `qs_ad_mobile`
--

DROP TABLE IF EXISTS `qs_ad_mobile`;
CREATE TABLE `qs_ad_mobile` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `alias` varchar(80) NOT NULL,
  `is_display` tinyint(1) NOT NULL DEFAULT '1',
  `title` varchar(100) NOT NULL,
  `note` varchar(230) NOT NULL,
  `show_order` int(10) unsigned NOT NULL DEFAULT '50',
  `addtime` int(10) unsigned NOT NULL,
  `starttime` int(10) unsigned NOT NULL,
  `deadline` int(11) NOT NULL DEFAULT '0',
  `content` text NOT NULL,
  `url` varchar(255) NOT NULL,
  `explain` varchar(255) NOT NULL,
  `uid` int(10) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `alias_starttime_deadline` (`alias`,`starttime`,`deadline`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
