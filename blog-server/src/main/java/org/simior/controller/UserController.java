package org.simior.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.simior.common.result.Result;
import org.simior.common.utils.PageUtils;
import org.simior.model.dto.UserCreateDTO;
import org.simior.model.dto.UserUpdateDTO;
import org.simior.model.vo.UserVO;
import org.simior.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器（仅管理员可访问）
 * <p>
 * 提供用户的增删改查功能，需要 admin 角色权限。
 * URL 前缀：/v1/users，遵循 RESTful 风格使用复数名词。
 * <p>
 * 所有接口均需 @SaCheckRole("admin") 权限校验。
 */
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@SaCheckRole("admin")
public class UserController {

    private final UserService userService;

    /**
     * 新增用户
     *
     * @param dto 创建用户参数
     * @return 新用户ID
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateDTO dto) {
        Long userId = userService.createUser(dto);
        return Result.success("新增成功", userId);
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除用户（逻辑删除）
     *
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        boolean success = userService.removeByIds(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 更新用户信息
     *
     * @param id  用户ID
     * @param dto 更新用户参数
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return Result.success("更新成功");
    }

    /**
     * 分页查询用户列表
     *
     * @param pageNum  当前页码，默认1
     * @param pageSize 每页条数，默认10
     * @param username 用户名（可选，模糊查询）
     * @return 用户分页数据（不含密码）
     */
    @GetMapping
    public Result<Page<UserVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String username) {
        pageNum = PageUtils.clamp(pageNum, 1, Long.MAX_VALUE);
        pageSize = PageUtils.clamp(pageSize, 1, 100);
        Page<UserVO> voPage = userService.pageUserVOs(pageNum, pageSize, username);
        return Result.success(voPage);
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表（不含密码）
     */
    @GetMapping("/all")
    public Result<List<UserVO>> list() {
        List<UserVO> list = userService.listAllUserVOs();
        return Result.success(list);
    }

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        UserVO vo = userService.getUserVOById(id);
        if (vo == null) {
            return Result.error("用户不存在");
        }
        return Result.success(vo);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public Result<UserVO> getByUsername(@PathVariable String username) {
        UserVO vo = userService.getUserVOByUsername(username);
        if (vo == null) {
            return Result.error("用户不存在");
        }
        return Result.success(vo);
    }
}
