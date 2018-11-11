<?php
/**
 * 友好时间
 */
function log_date($time) {
    if (!$time) return false;
    $fdate = '';
    $d = time() - intval($time);
    $ld = time() - mktime(0, 0, 0, 0, 0, date('Y')); //年
    $md = time() - mktime(0, 0, 0, date('m'), 0, date('Y')); //月
    $byd = time() - mktime(0, 0, 0, date('m'), date('d') - 2, date('Y')); //前天
    $yd = time() - mktime(0, 0, 0, date('m'), date('d') - 1, date('Y')); //昨天
    $dd = time() - mktime(0, 0, 0, date('m'), date('d'), date('Y')); //今天
    if ($d) {
        switch ($d) {
            case $d <= $dd:
                $fdate = '今天';
                break;
            case $d <= $yd:
                $fdate = '昨天';
                break;
            case $d <= $byd:
                $fdate = '前天';
                break;
            case $d < $md:
                $fdate = date('m-d', $time);
                break;
            case $d < $ld:
                $fdate = date('m-d', $time);
                break;
            default:
                $fdate = date('Y-m-d', $time);
                break;
        }
    }
    return $fdate;
}
?>