package com.xtx.auth.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.auth.entity.SysUserSocial;
import org.apache.ibatis.annotations.Mapper;


/**
 * 系统用户社交绑定 Mapper
 * 提供第三方账号绑定关系的查询与操作
 */
@Mapper
public interface SysUserSocialMapper extends BaseMapper<SysUserSocial> {

    /**
     * 根据第三方平台唯一标识查询绑定记录
     *
     * @param unionId 第三方平台唯一标识
     * @return 绑定记录，若不存在返回 null
     */
    default SysUserSocial selectByUnionId(String unionId) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("union_id", unionId)
                        .limit(1)
        );
    }
}
