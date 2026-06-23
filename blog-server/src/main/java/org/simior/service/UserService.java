package org.simior.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.simior.model.dto.UserCreateDTO;
import org.simior.model.dto.UserUpdateDTO;
import org.simior.model.entity.SysUser;
import org.simior.model.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<SysUser> {
    /**
     * 分页查询用户
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param username 用户名（可选）
     * @return 分页结果
     */
    Page<SysUser> pageUsers(Long pageNum, Long pageSize, String username);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getUserByUsername(String username);

    /**
     * 创建用户（管理员操作）
     *
     * @param dto 创建用户参数
     * @return 新用户ID
     */
    Long createUser(UserCreateDTO dto);

    /**
     * 更新用户信息（管理员操作）
     *
     * @param id  用户ID
     * @param dto 更新用户参数
     */
    void updateUser(Long id, UserUpdateDTO dto);

    /**
     * 分页查询用户VO列表
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param username 用户名（可选）
     * @return 分页结果（UserVO）
     */
    Page<UserVO> pageUserVOs(Long pageNum, Long pageSize, String username);

    /**
     * 查询所有用户VO列表
     *
     * @return 用户列表（UserVO）
     */
    List<UserVO> listAllUserVOs();

    /**
     * 根据ID查询用户VO
     *
     * @param id 用户ID
     * @return 用户信息（UserVO）
     */
    UserVO getUserVOById(Long id);

    /**
     * 根据用户名查询用户VO
     *
     * @param username 用户名
     * @return 用户信息（UserVO）
     */
    UserVO getUserVOByUsername(String username);
}
