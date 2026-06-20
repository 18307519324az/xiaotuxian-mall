-- ============================================
-- V6: 首页模块种子数据
-- 由 scripts/export-home-for-mysql.js 自动生成
-- 生成时间: 2026-06-10T04:48:15.680Z
-- ============================================

-- 1. 首页横幅表
INSERT INTO home_banner (id, img_url, href_url, type, sort, status) VALUES
(18, 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-15/1ba86bcc-ae71-42a3-bc3e-37b662f7f07e.jpg', '/category/1013001', '1', 10, 1),
(19, 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-15/6d202d8e-bb47-4f92-9523-f32ab65754f4.jpg', '/category/1013001', '1', 20, 1),
(20, 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-15/e83efb1b-309c-46f7-98a3-f1fefa694338.jpg', '/category/1005000', '1', 30, 1),
(17, 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-15/4a79180a-1a5a-4042-8a77-4db0b9c800a8.jpg', '/category/1019000', '1', 40, 1),
(16, 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-15/dfc11bb0-4af5-4e9b-9458-99f615cc685a.jpg', '/category/1005000', '1', 50, 1);

-- 2. 首页品牌表
INSERT INTO home_brand (id, name, picture, logo, sort, status) VALUES
(8, '硕华品质', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/a25d210a-cf3d-49f5-9006-5c1e7c563bb9.jpg', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/a25d210a-cf3d-49f5-9006-5c1e7c563bb9.jpg', 10, 1),
(6, 'CZ永在', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/b5fd7624-1d1e-4ed9-b739-a8f44f2d08fc.jpg', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/b5fd7624-1d1e-4ed9-b739-a8f44f2d08fc.jpg', 20, 1),
(1, '咏汉定制', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/fdbc5113-dcca-4b7f-b1d6-c51faf8e3de9.jpg', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/fdbc5113-dcca-4b7f-b1d6-c51faf8e3de9.jpg', 30, 1),
(2, 'ICCUG', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/cc95ddbf-f2f8-4c48-9845-0c5364557198.jpg', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/cc95ddbf-f2f8-4c48-9845-0c5364557198.jpg', 40, 1),
(3, '釉色美颜', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/cdeaf7eb-a68f-485c-9fc3-82358ebed83b.jpg', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/cdeaf7eb-a68f-485c-9fc3-82358ebed83b.jpg', 50, 1),
(4, '绿荫', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/bb0411c8-0407-460b-9db2-e1ca377d7227.jpg', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/bb0411c8-0407-460b-9db2-e1ca377d7227.jpg', 60, 1),
(5, '永久', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/b0941d16-a466-4f23-bbf4-90f818298abb.jpg', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/b0941d16-a466-4f23-bbf4-90f818298abb.jpg', 70, 1),
(7, 'DDAO', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/4f998a72-6c37-44fc-bb28-c017541868e8.jpg', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/4f998a72-6c37-44fc-bb28-c017541868e8.jpg', 90, 1),
(9, '黛儿', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/07b52b63-d128-491f-b55e-ad9192a6baeb.jpg', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/07b52b63-d128-491f-b55e-ad9192a6baeb.jpg', 100, 1);

-- 3. 首页专题表
INSERT INTO home_special (id, title, cover, summary, lowest_price, sort, status) VALUES
(1482381924796334084, '清爽醒肤不紧绷 男士泡沫洗面奶 150ml', 'https://yanxuan-item.nosdn.127.net/209c6276109dde299a0df3c5c8ee94e0.jpg', '代使用', 59, 10, 1),
(1482381924729225219, '立地蒸汽挂烫机，精烫塑形，有板无皱', 'https://yanxuan-item.nosdn.127.net/222fbba4e4432d56b743f415cbe3fa52.jpg', '很满意', 349, 20, 1),
(1482381924796334083, '鎏光金棕水润光泽气垫 13g*2 买一送替换装', 'https://yanxuan-item.nosdn.127.net/84c10a0173126109b3d14a28146f9158.png', '夸爆它，说不输大牌太笼统吧，就是真的像它的名字，自带水光的感觉，而且因为有孔，每次取粉量比较少，上脸后就很容易贴合超级细腻，刘亦菲们，省钱买到的好东西就是它了', 129, 30, 1);

-- 4. 专题商品关联表
-- (当前无专题商品关联数据，预留)

-- 5. 首页楼层表
INSERT INTO home_floor (id, category_id, category_name, picture, sale_info, sort, status) VALUES
(1, 1005000, '居家', 'https://yanxuan-item.nosdn.127.net/01227c93e9098342be591ea57b8953c7.jpg?quality=95&thumbnail=610x610&imageView', '', 10, 1),
(2, 1005002, '美食', 'https://yanxuan-item.nosdn.127.net/01227c93e9098342be591ea57b8953c7.jpg?quality=95&thumbnail=610x610&imageView', '', 20, 1),
(3, 1010000, '服饰', 'https://yjy-oss-files.oss-cn-zhangjiakou.aliyuncs.com/tuxian/home_goods_cover.jpg', '', 30, 1),
(4, 1011000, '母婴', 'https://yjy-oss-files.oss-cn-zhangjiakou.aliyuncs.com/tuxian/kitchen_goods_cover.jpg', '', 40, 1),
(5, 1013001, '个护', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-05-06/d38a73b8-cd03-48aa-a60b-e7c4e16667ed.png', '', 50, 1),
(6, 1019000, '严选', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-05-06/4b02f01f-a365-4b6c-9f7a-8b0f591dda02.png', '', 60, 1),
(7, 1043000, '数码', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-05-06/9660870d-6a59-4624-8064-b3a8cbf50d5c.png', '', 70, 1),
(8, 109243029, '运动', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-05-06/7d19752c-baff-49b6-bd02-5ece1d729214.png', '', 80, 1),
(9, 19999999, '杂项', 'http://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-05-06/4ff20b9e-8150-4bd3-87a3-0cd6766938dd.png', '', 90, 1);

