package com.xtx.auth.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.auth.entity.SysSmsCode;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;


/**
 * 短信验证码 Mapper
 * 提供短信验证码的查询与校验方法
 */
@Mapper
public interface SysSmsCodeMapper extends BaseMapper<SysSmsCode> {

    /**
     * 查询指定手机号和类型的最新有效验证码
     * 条件：未使用、未过期，按创建时间倒序取最新一条
     *
     * @param mobile 手机号
     * @param type   验证码类型
     * @return 最新的有效验证码记录，若不存在返回 null
     */
    default SysSmsCode selectLatestValidCode(String mobile, String type) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("mobile", mobile)
                        .eq("type", type)
                        .eq("used", 0)
                        .gt("expire_time", LocalDateTime.now())
                        .orderBy("create_time", false)
                        .limit(1)
        );
    }
}
