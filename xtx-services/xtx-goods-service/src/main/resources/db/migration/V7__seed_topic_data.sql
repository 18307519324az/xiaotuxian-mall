-- ============================================
-- V7: 专题种子数据
--
-- 9 条专题数据，匹配 Mock home-special.json 数据
-- 封面图使用与 Mock 一致的 yanxuan 真实图片 URL
-- ============================================

INSERT INTO topic (id, title, summary, cover, collect_num, view_num, reply_num, lowest_price, status, sort_order) VALUES
('1482381924796334084', '清爽醒肤不紧绷 男士泡沫洗面奶 150ml', '代使用',
 'https://yanxuan-item.nosdn.127.net/209c6276109dde299a0df3c5c8ee94e0.jpg',
 55803, 83010, 9311, 59.00, 1, 10),

('1482381924729225219', '立地蒸汽挂烫机，精烫塑形，有板无皱', '很满意',
 'https://yanxuan-item.nosdn.127.net/222fbba4e4432d56b743f415cbe3fa52.jpg',
 112, 196, 15, 349.00, 1, 20),

('1482381924796334083', '鎏光金棕水润光泽气垫 13g*2 买一送替换装', '夸爆它，说不输大牌太笼统吧，就是真的像它的名字，自带水光的感觉，而且因为有孔，每次取粉量比较少，上脸后就很容易贴合超级细腻，刘亦菲们，省钱买到的好东西就是它了',
 'https://yanxuan-item.nosdn.127.net/84c10a0173126109b3d14a28146f9158.png',
 2454, 2774, 1712, 129.00, 1, 30),

('v0.9.7-topic-kitchen', '厨房粮油囤货 — 精选米面粮油与方便速食', '厨房好物一站购齐',
 'https://yanxuan-item.nosdn.127.net/b6e494ebe0ddf5519e99e65570ce9288.png',
 12500, 45600, 3200, 9.00, 1, 40),

('v0.9.7-topic-home-storage', '居家收纳焕新 — 收纳整理与家纺好物', '让家更整洁有序',
 'https://yanxuan-item.nosdn.127.net/5f8dd823b91fc33b7010fead4dbfd1b3.png',
 23200, 78900, 5600, 29.00, 1, 50),

('v0.9.7-topic-baby', '母婴柔软好物 — 宝宝成长精选', '呵护宝贝每一天',
 'https://yanxuan-item.nosdn.127.net/3b9e1c7dc5f0f9ddc6b53a0ac3d7df09.png',
 18900, 62300, 4100, 79.00, 1, 60),

('v0.9.7-topic-sport', '轻运动穿搭 — 运动服饰与健身装备', '活力运动轻松开始',
 'https://yanxuan-item.nosdn.127.net/5f8dd823b91fc33b7010fead4dbfd1b3.png',
 15600, 51200, 3800, 33.00, 1, 70),

('v0.9.7-topic-digital', '数码办公精选 — 数码配件与办公好物', '提升工作效率与生活品质',
 'https://yanxuan-item.nosdn.127.net/84c10a0173126109b3d14a28146f9158.png',
 20100, 67800, 4900, 11.00, 1, 80),

('v0.9.7-topic-guofeng', '国风服饰穿搭 — 新国潮与传统风尚', '穿出东方韵味',
 'https://yanxuan-item.nosdn.127.net/84c10a0173126109b3d14a28146f9158.png',
 9800, 34500, 2100, 31.00, 1, 90)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    summary = VALUES(summary),
    cover = VALUES(cover),
    collect_num = VALUES(collect_num),
    view_num = VALUES(view_num),
    reply_num = VALUES(reply_num),
    lowest_price = VALUES(lowest_price),
    status = VALUES(status),
    sort_order = VALUES(sort_order);
