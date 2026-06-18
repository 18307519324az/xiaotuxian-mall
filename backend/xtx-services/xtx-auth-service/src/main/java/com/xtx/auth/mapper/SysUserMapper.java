package com.xtx.auth.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;


/**
 * 系统用户 Mapper
 * 提供系统用户表的基础 CRUD 及自定义查询方法
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据账号查询启用的用户
     *
     * @param account 登录账号
     * @return 启用的用户实体，若不存在返回 null
     */
    default SysUser selectEnabledByAccount(String account) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("account", account)
                        .eq("status", 1)
                        .limit(1)
        );
    }

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户实体，若不存在返回 null
     */
    default SysUser selectByMobile(String mobile) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("mobile", mobile)
                        .limit(1)
        );
    }
}
